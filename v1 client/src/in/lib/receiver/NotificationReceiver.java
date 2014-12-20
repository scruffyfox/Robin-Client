package in.lib.receiver;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.exception.ExceptionHandler;
import in.lib.manager.APIManager;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UpdateManager;
import in.lib.manager.UserManager;
import in.lib.type.FIFOArrayList;
import in.lib.utils.IntegerUtils;
import in.lib.utils.StringUtils;
import in.model.User;
import in.rob.client.MainActivity;
import in.rob.client.MainApplication;
import in.rob.client.MainApplication.ApplicationType;
import in.rob.client.MessagesActivity;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.dialog.ReplyMessageDialog;
import in.rob.client.dialog.ReplyPostDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.obj.entity.JsonEntity;
import net.callumtaylor.asynchttp.response.AsyncHttpResponseHandler;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class NotificationReceiver extends BroadcastReceiver
{
	static int ATTEMPTS = 0;
	static final FIFOArrayList<String> recievedIds;
	static
	{
		recievedIds = new FIFOArrayList<String>(100);
	}

	@Override public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals("notification.delete"))
		{
			SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
			prefs.edit()
				.remove(Constants.PREFS_NOTIFICATION_ID + intent.getExtras().getString(Constants.EXTRA_USER_ID))
				.remove(Constants.PREFS_NOTIFICATION_COUNT + intent.getExtras().getString(Constants.EXTRA_USER_ID))
			.apply();
			CacheManager.getInstance().removeFile(Constants.PREFS_NOTIFICATION_PREVIEW_LINES + intent.getExtras().getString(Constants.EXTRA_USER_ID));
		}
		else if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION"))
		{
			handleRegistration(context, intent);
		}
		else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE"))
		{
			if (intent.getExtras() == null
			|| (intent.getExtras() != null
			&& !intent.getExtras().containsKey("message")
			&& !intent.getExtras().containsKey("type")))
			{
				return;
			}

			String msg = intent.getExtras().getString("message");
			String type = intent.getExtras().getString("type");

			try
			{
				if (!SettingsManager.isNotificationsEnabled()) return;

				if (type.equals("mention") || type.equals("reply"))
				{
					handleMention(context, type, msg);
				}
				else if (type.equals("message"))
				{
					handleMessage(context, type, msg);
				}
				else if (type.equals("patter_message"))
				{
					handlePatterMention(context, type, msg);
				}
				else if (type.equals("follow"))
				{
					handleFollow(context, type, msg);
				}
				else if (type.equals("beta.update"))
				{
					handleUpdate(context, msg);
				}
				else if (type.equals("beta.disable"))
				{
					SharedPreferences.Editor editor = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit();
					editor.putBoolean(Constants.PREFS_HAS_BETA, false).apply();
				}
			}
			catch (Exception e)
			{
				Exception e2 = new Exception(type + "\n" + msg + "\n" + StringUtils.join(UserManager.getLinkedUserIds(context), ",") + "\n" + e.getMessage());
				ExceptionHandler.sendException(e);
				ExceptionHandler.sendException(e2);
			}
		}
	}

	private void handlePatterMention(Context context, String type, String msg)
	{
		JsonObject message = new JsonParser().parse(msg).getAsJsonObject();
		JsonArray mentions = message.get("messages").getAsJsonArray();
		JsonObject mention = mentions.get(0).getAsJsonObject();

		String postId = mention.get("message_id").getAsString();
		String channelId = mention.get("channel_id").getAsString();
		String username = mention.get("username").isJsonNull() ? "" : mention.get("username").getAsString();
		String accountId = mention.get("account_id").getAsString();
		String userId = mention.get("user_id").getAsString();
		String contentTitle = String.format(context.getString(R.string.notif_patter_message_from), username);
		String contentText = mention.get("text").getAsString();
		CharSequence notificationPreview = Html.fromHtml(context.getString(R.string.notif_new_patter_message_from, username) + "<br />" + contentText);

		if (!UserManager.getLinkedUserIds(context).contains(accountId)) return;

		Intent notificationIntent = new Intent(context, MessagesActivity.class);
		notificationIntent.putExtra(Constants.EXTRA_CHANNEL_ID, channelId);
		notificationIntent.putExtra(Constants.EXTRA_FORCE_REFRESH, true);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		notificationIntent.putExtra(Constants.EXTRA_SELECT_USER, accountId);

		Intent delete = new Intent(context, NotificationReceiver.class);
		delete.putExtra(Constants.EXTRA_USER_ID, accountId);
		delete.setAction("notification.patter_message.delete");

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent deleteIntent = PendingIntent.getBroadcast(context, 0, delete, PendingIntent.FLAG_CANCEL_CURRENT);
		User forAccount = User.loadUser(accountId);

		Notification notification;
		Builder builder = new NotificationCompat.Builder(context)
			.setContentIntent(contentIntent)
			.setDeleteIntent(deleteIntent)
			.setTicker(notificationPreview)
			.setContentTitle(contentTitle)
			.setContentText(contentText)
			.setSmallIcon(R.drawable.notif_patter)
			.setAutoCancel(true);

		boolean imageLoaded = false;
		if (User.userSaved(userId))
		{
			Bitmap b = User.loadAvatar(context, userId);

			if (b != null)
			{
				builder.setLargeIcon(b);
				imageLoaded = true;
			}
			else
			{
				ImageLoader.getInstance().loadImage(String.format(APIManager.API_FULL_USER_AVATAR, userId), MainApplication.getAvatarImageOptions(), null);
			}
		}

		if (!imageLoaded)
		{
			builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		}

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
			style.bigText(contentText);

			Intent reply = new Intent(context, ReplyMessageDialog.class);
			reply.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			reply.putExtra(Constants.EXTRA_MESSAGE_ID, postId);
			reply.putExtra(Constants.EXTRA_CHANNEL_ID, channelId);
			reply.putExtra(Constants.EXTRA_REPLY_TO_EXTRA, username);

			if (forAccount != null)
			{
				reply.putExtra(Constants.EXTRA_USER, forAccount);
			}

			Intent profile = new Intent(context, ProfileActivity.class);
			profile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			profile.putExtra(Constants.EXTRA_USER_ID, userId);

			PendingIntent replyIntent = PendingIntent.getActivity(context, 0, reply, PendingIntent.FLAG_CANCEL_CURRENT);
			PendingIntent profileIntent = PendingIntent.getActivity(context, 0, profile, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.addAction(R.drawable.ic_notif_reply, context.getString(R.string.reply_to_message), replyIntent);
			builder.addAction(R.drawable.ic_notif_profile, context.getString(R.string.profile), profileIntent);

			notification = style.build();
		}
		else
		{
			notification = builder.build();
		}

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		setQuietHours(context, notification);
		sendNotification(IntegerUtils.parseInt(channelId), context, notification);
	}

	private void handleUpdate(Context context, String message)
	{
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		String contentTitle = context.getString(R.string.new_update);
		long when = System.currentTimeMillis();

		Notification notification = new Notification(R.drawable.notif, contentTitle, when);
		notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent notificationIntent = new Intent(context, UpdateManager.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		notification.setLatestEventInfo(context, contentTitle, message, contentIntent);
		notificationManager.notify(65834, notification);
	}

	private void handleMessage(Context context, String type, String msg)
	{
		JsonObject message = new JsonParser().parse(msg).getAsJsonObject();
		JsonArray mentions = message.get("messages").getAsJsonArray();
		JsonObject mention = mentions.get(0).getAsJsonObject();

		String postId = mention.get("message_id").getAsString();
		String channelId = mention.get("channel_id").getAsString();
		String username = mention.get("username").isJsonNull() ? "" : mention.get("username").getAsString();
		String accountId = mention.get("account_id").getAsString();
		String userId = mention.get("user_id").getAsString();
		String contentTitle = String.format(context.getString(R.string.notif_message_from), username);
		String contentText = mention.get("text").getAsString();
		CharSequence notificationPreview = Html.fromHtml(context.getString(R.string.notif_new_message_from, username) + "<br />" + contentText);

		if (!UserManager.getLinkedUserIds(context).contains(accountId)) return;

		Intent notificationIntent = new Intent(context, MessagesActivity.class);
		notificationIntent.putExtra(Constants.EXTRA_CHANNEL_ID, channelId);
		notificationIntent.putExtra(Constants.EXTRA_FORCE_REFRESH, true);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		notificationIntent.putExtra(Constants.EXTRA_SELECT_USER, accountId);

		Intent delete = new Intent(context, NotificationReceiver.class);
		delete.putExtra(Constants.EXTRA_USER_ID, accountId);
		delete.setAction("notification.message.delete");

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent deleteIntent = PendingIntent.getBroadcast(context, 0, delete, PendingIntent.FLAG_CANCEL_CURRENT);
		User forAccount = User.loadUser(accountId);

		Notification notification;
		Builder builder = new NotificationCompat.Builder(context)
			.setContentIntent(contentIntent)
			.setDeleteIntent(deleteIntent)
			.setTicker(notificationPreview)
			.setContentTitle(contentTitle)
			.setContentText(contentText)
			.setSmallIcon(R.drawable.notif_message)
			.setAutoCancel(true);

		boolean imageLoaded = false;
		if (User.userSaved(userId))
		{
			Bitmap b = User.loadAvatar(context, userId);

			if (b != null)
			{
				builder.setLargeIcon(b);
				imageLoaded = true;
			}
			else
			{
				ImageLoader.getInstance().loadImage(String.format(APIManager.API_FULL_USER_AVATAR, userId), MainApplication.getAvatarImageOptions(), null);
			}
		}

		if (!imageLoaded)
		{
			builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		}

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
			style.bigText(contentText);

			Intent reply = new Intent(context, ReplyMessageDialog.class);
			reply.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			reply.putExtra(Constants.EXTRA_MESSAGE_ID, postId);
			reply.putExtra(Constants.EXTRA_CHANNEL_ID, channelId);
			reply.putExtra(Constants.EXTRA_REPLY_TO_EXTRA, username);

			if (forAccount != null)
			{
				reply.putExtra(Constants.EXTRA_USER, forAccount);
			}

			Intent profile = new Intent(context, ProfileActivity.class);
			profile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			profile.putExtra(Constants.EXTRA_USER_ID, userId);

			PendingIntent replyIntent = PendingIntent.getActivity(context, 0, reply, PendingIntent.FLAG_CANCEL_CURRENT);
			PendingIntent profileIntent = PendingIntent.getActivity(context, 0, profile, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.addAction(R.drawable.ic_notif_reply, context.getString(R.string.reply_to_message), replyIntent);
			builder.addAction(R.drawable.ic_notif_profile, context.getString(R.string.profile), profileIntent);

			notification = style.build();
		}
		else
		{
			notification = builder.build();
		}

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		setQuietHours(context, notification);
		sendNotification(IntegerUtils.parseInt(channelId), context, notification);
	}

	private void handleMention(Context context, String type, String msg)
	{
		JsonObject message = new JsonParser().parse(msg).getAsJsonObject();
		JsonArray mentions = message.get("mentions").getAsJsonArray();
		JsonObject mention = mentions.get(0).getAsJsonObject();
		int count = mentions.size();

		String postId = mention.get("post_id").getAsString();

		if (recievedIds.contains(postId)
		|| (SettingsManager.getSwarmProtectionIndex() > 0 && mention.get("users_size").getAsInt() - 1 >= SettingsManager.getSwarmProtectionIndex() + 1))
		{
			return;
		}

		recievedIds.add(postId);
		String username = mention.get("username").isJsonNull() ? "" : mention.get("username").getAsString();
		String accountId = mention.get("account_id").getAsString();
		String threadId = mention.get("thread_id").getAsString();
		String userId = mention.get("user_id").getAsString();
		String contentTitle = String.format(context.getString(R.string.mentioned_by), username);
		String contentText = mention.get("text").getAsString();
		CharSequence notificationPreview = Html.fromHtml("New " + type + " from @" + username + "<br />" + contentText);

		if (type.equals("reply"))
		{
			contentTitle = context.getString(R.string.reply_from, username);
		}

		if (SettingsManager.isThreadMuted(threadId) || !UserManager.getLinkedUserIds(context).contains(accountId)) return;
		if (!mention.get("you_follow").getAsBoolean() && SettingsManager.isNotificationsOnlyFollowing()) return;

		if (count > 1)
		{
			contentTitle = String.format(context.getString(R.string.new_mentions_counter), mentions.size());
			notificationPreview = contentTitle;
			contentText = "";
			type = "mention";

			for (JsonElement m : mentions)
			{
				String uname = m.getAsJsonObject().get("username").isJsonNull() ? "" : m.getAsJsonObject().get("username").getAsString();
				String user = context.getString(R.string.at) + uname + ", ";
				if (!contentText.contains(user))
				{
					contentText += user;
				}
			}

			contentText = contentText.substring(0, contentText.length() - 2);
		}

		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		int notificationCount = 1;
		if (prefs.contains(Constants.PREFS_NOTIFICATION_ID + accountId))
		{
			notificationCount = prefs.getInt(Constants.PREFS_NOTIFICATION_COUNT + accountId, 0) + count;
			type = "mention";
		}

		Intent notificationIntent = new Intent(context, MainActivity.class);
		if (type.equals("mention"))
		{
			notificationIntent.putExtra(Constants.EXTRA_START_PAGE, 1);
			notificationIntent.putExtra(Constants.EXTRA_FORCE_REFRESH, true);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		else if (type.equals("reply"))
		{
			notificationIntent.putExtra(Constants.EXTRA_OPEN_THREAD, true);
			notificationIntent.putExtra(Constants.EXTRA_POST_ID, threadId);
			notificationIntent.putExtra(Constants.EXTRA_CENTER_POST_ID, postId);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		}

		notificationIntent.putExtra(Constants.EXTRA_SELECT_USER, accountId);

		Intent delete = new Intent(context, NotificationReceiver.class);
		delete.putExtra(Constants.EXTRA_USER_ID, accountId);
		delete.setAction("notification.delete");

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent deleteIntent = PendingIntent.getBroadcast(context, 0, delete, PendingIntent.FLAG_CANCEL_CURRENT);
		User forAccount = User.loadUser(accountId);

		/** Build the notification **/
		if (notificationCount > 1)
		{
			contentText = "+ " + (notificationCount - count) + " " + context.getString(R.string.other_mentions);

			if (forAccount != null)
			{
				contentText += " " + context.getString(R.string.notification_for, forAccount.getMentionName());
			}
		}

		Notification notification;
		Builder builder = new NotificationCompat.Builder(context)
			.setContentIntent(contentIntent)
			.setDeleteIntent(deleteIntent)
			.setTicker(notificationPreview)
			.setContentTitle(contentTitle)
			.setContentText(contentText)
			.setSmallIcon(R.drawable.notif)
			.setAutoCancel(true);

		boolean imageLoaded = false;
		if (User.userSaved(userId))
		{
			Bitmap b = User.loadAvatar(context, userId);

			if (b != null)
			{
				builder.setLargeIcon(b);
				imageLoaded = true;
			}
			else
			{
				ImageLoader.getInstance().loadImage(String.format(APIManager.API_FULL_USER_AVATAR, userId), MainApplication.getAvatarImageOptions(), null);
			}
		}

		if (!imageLoaded)
		{
			builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		}

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			int menSize = mentions.size();
			List<String> lines = CacheManager.getInstance().readFileAsObject(Constants.PREFS_NOTIFICATION_PREVIEW_LINES + accountId, new ArrayList<String>());
			NotificationCompat.Style style = null;

			for (int index = menSize - 1; index > -1; index--)
			{
				lines.add(mentions.get(index).getAsJsonObject().get("text").getAsString());
			}

			int size = lines.size();

			if (size == 1)
			{
				style = new NotificationCompat.BigTextStyle(builder);
				((BigTextStyle)style).bigText(lines.get(0));
			}
			else
			{
				style = new NotificationCompat.InboxStyle(builder);

				for (int index = size - 1, counter = 0; index > -1; index--, counter++)
				{
					if (counter > 5) break;
					((InboxStyle)style).addLine(lines.get(index));
				}

				if (forAccount != null)
				{
					((InboxStyle)style).setSummaryText("@" + forAccount.getMentionName());
				}
			}

			Intent reply = new Intent(context, ReplyPostDialog.class);
			reply.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			reply.putExtra(Constants.EXTRA_POST_ID, postId);
			reply.putExtra(Constants.EXTRA_REPLY_TO_EXTRA, username);

			if (forAccount != null)
			{
				reply.putExtra(Constants.EXTRA_USER, forAccount);
			}

			Intent profile = new Intent(context, ProfileActivity.class);
			profile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			profile.putExtra(Constants.EXTRA_USER_ID, userId);

			PendingIntent replyIntent = PendingIntent.getActivity(context, 0, reply, PendingIntent.FLAG_CANCEL_CURRENT);
			PendingIntent profileIntent = PendingIntent.getActivity(context, 0, profile, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.addAction(R.drawable.ic_notif_reply, context.getString(R.string.reply_last_post), replyIntent);
			builder.addAction(R.drawable.ic_notif_profile, context.getString(R.string.profile), profileIntent);

			builder.setNumber(size);

			CacheManager.getInstance().writeFile(Constants.PREFS_NOTIFICATION_PREVIEW_LINES + accountId, lines);
			notification = style.build();
		}
		else
		{
			notification = builder.build();
		}

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		setQuietHours(context, notification);
		sendNotification(IntegerUtils.parseInt(accountId), context, notification);

		prefs.edit()
			.putInt(Constants.PREFS_NOTIFICATION_ID + accountId, IntegerUtils.parseInt(accountId))
			.putInt(Constants.PREFS_NOTIFICATION_COUNT + accountId, prefs.getInt(Constants.PREFS_NOTIFICATION_COUNT + accountId, 0) + count)
		.apply();
	}

	private void handleFollow(final Context context, String type, String msg)
	{
		JsonObject followObj = new JsonParser().parse(msg).getAsJsonObject();
		JsonArray follows = followObj.get("follows").getAsJsonArray();
		JsonObject follow = follows.get(0).getAsJsonObject();

		String mentionName = follow.get("mention_name").isJsonNull() ? "" : follow.get("mention_name").getAsString();
		String accountId = follow.get("account_id").getAsString();
		boolean youFollow = follow.get("you_follow").getAsBoolean();
		final String userId = follow.get("user_id").getAsString();

		if (!UserManager.getLinkedUserIds(context).contains(accountId)) return;

		String contentTitle = context.getString(R.string.notif_follow);
		String contentText = String.format(context.getString(R.string.notif_follow_from), mentionName);
		CharSequence notificationPreview = contentText;

		Intent notificationIntent = new Intent(context, ProfileActivity.class);
		notificationIntent.putExtra(Constants.EXTRA_USER_ID, userId);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		notificationIntent.putExtra(Constants.EXTRA_SELECT_USER, accountId);
		PendingIntent notifIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Notification notification;
		final Builder builder = new NotificationCompat.Builder(context)
			.setContentIntent(notifIntent)
			.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
			.setTicker(notificationPreview)
			.setContentTitle(contentTitle)
			.setContentText(contentText)
			.setSmallIcon(R.drawable.notif_follow)
			.setAutoCancel(true);

		final int id = new Random().nextInt();

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			Intent followIntent = new Intent(context, FollowService.class);
			followIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID, id);
			followIntent.putExtra(Constants.EXTRA_MODE, youFollow ? "unfollow" : "follow");
			followIntent.putExtra(Constants.EXTRA_USER_NAME, mentionName);
			followIntent.putExtra(Constants.EXTRA_USER_ID, userId);
			PendingIntent followPendingIntent = PendingIntent.getActivity(context, 0, followIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			builder.addAction(youFollow ? R.drawable.ic_notif_unfollow : R.drawable.ic_notif_follow, youFollow ? context.getString(R.string.unfollow) : context.getString(R.string.follow), followPendingIntent);
		}

		if (User.userSaved(userId))
		{
			Bitmap b = User.loadAvatar(context, userId);

			if (b != null)
			{
				builder.setLargeIcon(b);
			}

			notification = builder.build();
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			setQuietHours(context, notification);
			sendNotification(id, context, notification);
		}
		else
		{
			ImageLoader.getInstance().loadImage(String.format(APIManager.API_FULL_USER_AVATAR, userId), MainApplication.getAvatarImageOptions(), new SimpleImageLoadingListener()
			{
				@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
				{
					super.onLoadingComplete(imageUri, view, loadedImage);

					Notification notification;
					builder.setLargeIcon(User.loadAvatar(context, userId));
					notification = builder.build();
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					setQuietHours(context, notification);
					sendNotification(id, context, notification);
				}

				@Override public void onLoadingFailed(String imageUri, View view, FailReason failReason)
				{
					super.onLoadingFailed(imageUri, view, failReason);

					Notification notification;
					notification = builder.build();
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					setQuietHours(context, notification);
					sendNotification(id, context, notification);
				}
			});
		}
	}

	public void sendNotification(int id, Context context, Notification notification)
	{
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, notification);
	}

	/**
	 * Sets the modes on the notification intent if quiet hours is set
	 * @param context
	 * @param notification
	 */
	public void setQuietHours(Context context, Notification notification)
	{
		GregorianCalendar d = new GregorianCalendar();
		d.set(1970, 0, 1);

		GregorianCalendar dateAfter = new GregorianCalendar();
		dateAfter.setTimeInMillis(SettingsManager.getQuietModeFrom());
		GregorianCalendar dateBefore = new GregorianCalendar();
		dateBefore.setTimeInMillis(SettingsManager.getQuietModeTo());

		boolean isQuiet = false;
		if (dateBefore.get(Calendar.DAY_OF_MONTH) > dateAfter.get(Calendar.DAY_OF_MONTH))
		{
			int currentTime = (d.get(Calendar.HOUR_OF_DAY) * 60 * 60) + (d.get(Calendar.MINUTE) * 60);
			int afterTime = (dateAfter.get(Calendar.HOUR_OF_DAY) * 60 * 60) + (dateAfter.get(Calendar.MINUTE) * 60);

			if (currentTime >= 0 && currentTime < afterTime)
			{
				d.add(Calendar.DAY_OF_MONTH, 1);

				if (d.before(dateBefore))
				{
					isQuiet = true;
				}
			}
			else
			{
				isQuiet = true;
			}
		}
		else if (d.after(dateAfter) && d.before(dateBefore))
		{
			isQuiet = true;
		}

		if (SettingsManager.isQuietModeEnabled() && isQuiet)
		{
			notification.defaults = 0;
		}
		else
		{
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.ledOnMS = 700;
			notification.ledOffMS = 1400;

			if (SettingsManager.isNotificationLedEnabled())
			{
				notification.ledARGB = 0xffff0000;
			}

			if (SettingsManager.isNotificationsSoundEnabled())
			{
				try
				{
					if (!TextUtils.isEmpty(SettingsManager.getNotificationTone()))
					{
						notification.sound = getContentUri(context, SettingsManager.getNotificationTone());
					}
					else
					{
						notification.defaults |= Notification.DEFAULT_SOUND;
					}
				}
				catch (Exception e)
				{
					notification.defaults |= Notification.DEFAULT_SOUND;
					Debug.out(e);
				}
			}

			if (SettingsManager.isNotificationsVibrateEnabled())
			{
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}
		}
	}

	public Uri getContentUri(Context context, String path)
	{
		String filePath = null;
		Uri uri = Uri.parse(path);
		if (uri != null && "content".equals(uri.getScheme()))
		{
			Cursor cursor = context.getContentResolver().query(uri, new String[]{android.provider.MediaStore.Audio.AudioColumns.DATA}, null, null, null);
			cursor.moveToFirst();
			filePath = cursor.getString(0);
			cursor.close();
		}
		else
		{
			filePath = uri.getPath();
		}

		return Uri.parse(filePath);
	}

	public void registerForPush(Context ctx)
	{
		try
		{
			Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
			registrationIntent.putExtra("app", PendingIntent.getBroadcast(ctx, 0, new Intent(), 0));
			registrationIntent.putExtra("sender", "112896297912");
			ctx.startService(registrationIntent);
		}
		catch (Exception e)
		{
			// User can't register for notifications
		}
	}

	public void registerUserForPush(final Context ctx)
	{
		try
		{
			AsyncHttpClient registerPush = new AsyncHttpClient(Constants.API_NOTIFICATION_URL + Constants.API_NOTIFICATION_VERSION);

			JsonObject data = new JsonObject();
			data.addProperty("id", UserManager.getUserId());
			data.addProperty("access_token", UserManager.getAccessToken());
			registerPush.post("/users", new JsonEntity(data), new AsyncHttpResponseHandler()
			{
				@Override public Object getContent()
				{
					return null;
				}

				@Override public void onSuccess()
				{
					registerForPush(ctx);
				}
			});
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	protected void handleRegistration(final Context context, Intent intent)
	{
		final String registration = intent.getStringExtra("registration_id");
		if (intent.getStringExtra("error") != null)
		{
			Debug.out(intent.getStringExtra("error"));
		}
		else if (intent.getStringExtra("unregistered") != null)
		{
			Debug.out(intent.getStringExtra("unregistered"));
		}
		else if (registration != null)
		{
			AsyncHttpClient client = new AsyncHttpClient(Constants.API_NOTIFICATION_URL + Constants.API_NOTIFICATION_VERSION);
			MainApplication application = (MainApplication)context.getApplicationContext();
			String userId = UserManager.getUserId();

			if (userId == null)
			{
				return;
			}

			JsonObject postjson = new JsonObject();
			postjson.addProperty("id", application.getDeviceId());
			postjson.addProperty("push_id", registration);
			postjson.addProperty("user_id", userId);
			postjson.addProperty("enabled", SettingsManager.getNotifications());
			postjson.addProperty("follow_enabled", SettingsManager.isNotificationsOnlyFollowing());

			try
			{
				client.post("users/" + userId + "/devices", new JsonEntity(postjson.toString()), new JsonResponseHandler()
				{
					@Override public void onFailure()
					{
						try
						{
							JsonElement content = getContent();

							if (content != null)
							{
								JsonObject resp = content.getAsJsonObject();

								// if the user does not exist in the notification table, re-register
								if (resp.get("error").getAsJsonObject().get("response_code").getAsInt() == 2 && ATTEMPTS++ < 2)
								{
									registerUserForPush(context);
								}
							}
						}
						catch (Exception e)
						{
							Debug.out(e);
						}
					}

					@Override public void onSuccess()
					{

					}
				});

				if (application.getApplicationType() == ApplicationType.BETA)
				{
					final AsyncHttpClient checker = new AsyncHttpClient(Constants.API_BETA_URL);
					JsonObject data = new JsonObject();
					data.addProperty("username", UserManager.getUser().getMentionName());
					data.addProperty("push_token", registration);
					data.addProperty("version", context.getString(R.string.app_version));

					checker.post(Constants.API_BETA_CHECK, new JsonEntity(data.toString()), null);
				}
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}
		else
		{
			Debug.out(intent.getExtras().keySet());
		}
	}
}