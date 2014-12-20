package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.handler.base.ImageResponseHandler;
import in.lib.handler.dialogs.NewPostDialogResponseHandler;
import in.lib.handler.dialogs.NewPrivateMessageDialogResponseHandler;
import in.lib.manager.APIManager;
import in.lib.manager.ImageAPIManager;
import in.lib.manager.ImageAPIManager.Provider;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.model.DraftPost;
import in.obj.annotation.Annotation;
import in.obj.annotation.ChannelInviteAnnotation;
import in.obj.annotation.CrosspostAnnotation;
import in.obj.annotation.FileAnnotation;
import in.obj.annotation.ImageAnnotation;
import in.obj.entity.Entity;
import in.obj.entity.Entity.Type;
import in.obj.entity.LinkEntity;
import in.rob.client.R;
import in.rob.client.dialog.base.DialogBuilder;
import in.rob.client.dialog.base.PostDialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

/**
 * New post dialog for creating a channel message.
 *
 * Required arguments:
 * <ul>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_CHANNEL_ID}</b>: The id of the channel to message</li>
 * </ul>
 *
 * Possible extra arguments:
 * <ul>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_MENTION_NAME}</b>: The username (without @) of the user to mention</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_TAG_NAME}</b>: The tag (without #) of the tag to mention</li>
 * </ul>
 */
public class NewMessageDialog extends PostDialog
{
	private NotificationManager mNotificationManager;
	private Intent mFailIntent;
	private int mNotificationId;
	private boolean isPublic = false;
	private String channelName = "";
	protected String tempTitle = "";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		setMaxChars(SettingsManager.getMessageLength());
		mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationId = (int)System.currentTimeMillis();

		super.onCreate(savedInstanceState);
		setTitle(tempTitle);
	}

	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		tempTitle = getString(R.string.new_message);

		if (instances != null)
		{
			if (instances.containsKey(Constants.EXTRA_CHANNEL_ID))
			{
				getCurrentPost().setChannelId(instances.getString(Constants.EXTRA_CHANNEL_ID));
			}

			if (TextUtils.isEmpty(getCurrentPost().getChannelId()))
			{
				Toast.makeText(getContext(), "No channel id set", Toast.LENGTH_SHORT).show();
				finish();
			}

			if (instances.containsKey(Constants.EXTRA_IS_PUBLIC))
			{
				isPublic = instances.getBoolean(Constants.EXTRA_IS_PUBLIC);
			}

			if (instances.containsKey(Constants.EXTRA_CHANNEL_NAME))
			{
				channelName = instances.getString(Constants.EXTRA_CHANNEL_NAME);
			}

			if (instances.containsKey(Constants.EXTRA_MENTION_NAME))
			{
				String replyTo = instances.getString(Constants.EXTRA_MENTION_NAME);
				getCurrentPost().setPostText("@" + replyTo + " ");
				tempTitle = String.format(getString(R.string.mention_user), replyTo);
			}

			if (instances.containsKey(Constants.EXTRA_TAG_NAME))
			{
				String replyTo = "#" + instances.getString(Constants.EXTRA_TAG_NAME);
				getCurrentPost().setPostText(replyTo + " ");
			}

			if (instances.containsKey(Constants.EXTRA_RESEND))
			{
				positiveControl();
				finish();
			}
		}
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putString(Constants.EXTRA_CHANNEL_ID, getCurrentPost().getChannelId());
		outState.putBoolean(Constants.EXTRA_IS_PUBLIC, isPublic);
		outState.putString(Constants.EXTRA_CHANNEL_NAME, channelName);
		outState.putString(Constants.EXTRA_TITLE, tempTitle);
		super.onSaveInstanceState(outState);
	}

	@Override public void positiveControl()
	{
		/*if (isPublic && getCurrentPost().getPostText().length() < 256 - Math.max(channelName.length(), 3) - 1)
		{
			ArrayList<String> options = new ArrayList<String>();
			options.add(getString(R.string.post_message));
			options.add(getString(R.string.broadcast_message));

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				final PopupMenu moreoptions;
				moreoptions = new PopupMenu(getContext(), findViewById(R.id.post));

				for (int index = 0; index < options.size(); index++)
				{
					moreoptions.getMenu().add(0, index, 0, options.get(index));
				}

				moreoptions.setOnMenuItemClickListener(new OnMenuItemClickListener()
				{
					@Override public boolean onMenuItemClick(MenuItem item)
					{
						int index = item.getItemId();

						if (index == 0)
						{
							sendMessage();
							finish();
						}
						else
						{
							sendMessage();
							broadcastMessage();
							finish();
						}

						moreoptions.dismiss();
						return true;
					}
				});

				moreoptions.show();
			}
		}
		else*/
		if (TextUtils.isEmpty(getCurrentPost().getPostText()) && TextUtils.isEmpty(getCurrentPost().getImagePath()))
		{
			return;
		}
		else if (TextUtils.isEmpty(getCurrentPost().getPostText()) && !TextUtils.isEmpty(getCurrentPost().getImagePath()))
		{
			if (SettingsManager.getImageProvider() == Provider.APPNET)
			{
				return;
			}
		}

		sendMessage();
		finish();
	}

	public void broadcastMessage()
	{
		DraftPost broadcastPost = new DraftPost();
		broadcastPost.setAnnotations(getCurrentPost().getAnnotations());
		broadcastPost.setChannelId(getCurrentPost().getChannelId());
		broadcastPost.setImagePath(getCurrentPost().getImagePath());
		broadcastPost.setEntities(getCurrentPost().getEntities());
		broadcastPost.setPostText(getCurrentPost().getPostText());
		broadcastPost.setReplyId(getCurrentPost().getReplyId());

		CrosspostAnnotation crossPost = new CrosspostAnnotation();
		crossPost.setUrl("http://patter-app.net/room.html?channel=" + getCurrentPost().getChannelId());
		ChannelInviteAnnotation invite = new ChannelInviteAnnotation();
		invite.setChannelId(broadcastPost.getChannelId());

		broadcastPost.getAnnotations().add(invite);
		broadcastPost.getAnnotations().add(crossPost);

		LinkEntity link = new LinkEntity();
		if (!TextUtils.isEmpty(channelName))
		{
			broadcastPost.setPostText(broadcastPost.getPostText() + " " + channelName);
			link.setPos(broadcastPost.getPostText().length() - channelName.length());
			link.setLen(channelName.length());
			link.setUrl("http://patter-app.net/room.html?channel=" + broadcastPost.getChannelId());

			ArrayList<Entity> links = new ArrayList<Entity>();
			links.add(link);
			LinkedHashMap<Type, ArrayList<Entity>> postEntities = new LinkedHashMap<Entity.Type, ArrayList<Entity>>();
			postEntities.put(Entity.Type.LINK, links);
			broadcastPost.setEntities(postEntities);
		}
		else
		{
			broadcastPost.setPostText(broadcastPost.getPostText() + " <=>");
			link.setPos(broadcastPost.getPostText().length() - 3);
			link.setLen(3);
			link.setUrl("http://patter-app.net/room.html?channel=" + broadcastPost.getChannelId());

			ArrayList<Entity> links = new ArrayList<Entity>();
			links.add(link);
			LinkedHashMap<Type, ArrayList<Entity>> postEntities = new LinkedHashMap<Entity.Type, ArrayList<Entity>>();
			postEntities.put(Entity.Type.LINK, links);
			broadcastPost.setEntities(postEntities);
		}

		String token = UserManager.getAuths(getContext()).get(getSelectedUser().getId()).getAccessToken();
		APIManager.getInstance().postStatus
		(
			token,
			broadcastPost,
			new NewPostDialogResponseHandler(getContext(), null, -1)
		);
	}

	public void sendMessage()
	{
		if (TextUtils.isEmpty(getCurrentPost().getPostText()) && TextUtils.isEmpty(getCurrentPost().getImagePath()))
		{
			return;
		}

		mFailIntent = new Intent(getApplicationContext(), NewMessageDialog.class);
		mFailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		mFailIntent.putExtra(Constants.EXTRA_NEW_POST_DRAFT, getCurrentPost().serialize());

		if ((getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Constants.EXTRA_NEW_POST_SKIP_IMAGE, false)) || TextUtils.isEmpty(getCurrentPost().getImagePath()))
		{
			sendNotification(getString(R.string.sending_message_title), getString(R.string.sending_message));
			String token = UserManager.getAuths(getContext()).get(getSelectedUser().getId()).getAccessToken();

			if (TextUtils.isEmpty(getCurrentPost().getReplyId()))
			{
				APIManager.getInstance().postMessage
				(
					token,
					getCurrentPost(),
					new NewPrivateMessageDialogResponseHandler
					(
						getContext(),
						mFailIntent,
						mNotificationId
					)
				);
			}
			else
			{
				APIManager.getInstance().replyMessage
				(
					token,
					getCurrentPost(),
					new NewPrivateMessageDialogResponseHandler
					(
						getContext(),
						mFailIntent,
						mNotificationId
					)
				);
			}
		}
		else
		{
			sendNotification(getString(R.string.uploading_image_title), getString(R.string.uploading_image));
			final String token = UserManager.getAuths(getContext()).get(getSelectedUser().getId()).getAccessToken();
			ImageAPIManager.getInstance().uploadImage(getContext(), getCurrentPost(), getSelectedUser(), 0, new ImageResponseHandler(getApplicationContext(), mFailIntent, mNotificationId)
			{
				@Override public void onCallback()
				{
					ImageAnnotation image = getImage();

					ArrayList<Annotation> entities = new ArrayList<Annotation>();
					entities.add(image);

					if (!(getImage() instanceof FileAnnotation))
					{
						getCurrentPost().setPostText((getCurrentPost().getPostText().trim() + " " + getImage().getTextUrl()).trim());
					}

					getCurrentPost().getAnnotations().addAll(entities);
					sendNotification(getString(R.string.sending_message_title), getString(R.string.sending_message));

					mFailIntent = new Intent(getApplicationContext(), NewMessageDialog.class);
					mFailIntent.putExtra(Constants.EXTRA_RESEND, true);
					mFailIntent.putExtra(Constants.EXTRA_NEW_POST_DRAFT, getCurrentPost().serialize());
					mFailIntent.putExtra(Constants.EXTRA_NEW_POST_SKIP_IMAGE, true);

					if (TextUtils.isEmpty(getCurrentPost().getReplyId()))
					{
						APIManager.getInstance().postMessage
						(
							token,
							getCurrentPost(),
							new NewPrivateMessageDialogResponseHandler
							(
								getContext(),
								mFailIntent,
								mNotificationId
							)
						);
					}
					else
					{
						APIManager.getInstance().replyMessage
						(
							token,
							getCurrentPost(),
							new NewPrivateMessageDialogResponseHandler
							(
								getContext(),
								mFailIntent,
								mNotificationId
							)
						);
					}
				}

				@Override public void onPublishedUploadProgressUI(long totalProcessed, long totalLength)
				{
					notification.setProgress((int)totalLength, (int)totalProcessed, false);
					mNotificationManager.notify(mNotificationId, notification.build());
				}
			});
		}
	}

	@Override public void onBackPressed()
	{
		negativeControl();
	}

	boolean finish = false;
	@Override public void negativeControl()
	{
		if (finish || (TextUtils.isEmpty(getCurrentPost().getPostText()) && TextUtils.isEmpty(getCurrentPost().getImagePath())))
		{
			super.negativeControl();
			return;
		}

		DialogBuilder.create(getContext())
			.setTitle(R.string.confirm)
			.setMessage(R.string.discard_changes)
			.setPositiveButton(R.string.yes, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					finish = true;
					negativeControl();
				}
			})
			.setNegativeButton(R.string.no, null)
		.show();
	}

	NotificationCompat.Builder notification;
	public void sendNotification(String title, String content)
	{
		notification = new NotificationCompat.Builder(getContext());
		notification.setContentTitle(title);
		notification.setContentText(content);
		notification.setTicker(content);
		notification.setSmallIcon(R.drawable.notif);
		notification.setProgress(0, 0, true);

		Intent notificationIntent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		notification.setContentIntent(contentIntent);
		mNotificationManager.notify(mNotificationId, notification.build());
	}

	@Override public int getContentView()
	{
		return R.layout.new_post_dialog;
	}

	@Override public void controlsClick(View v)
	{
		updateDraftPost();

		if (v.getId() == R.id.cancel_post)
		{
			negativeControl();
		}
		else if (v.getId() == R.id.post)
		{
			positiveControl();
		}
	}
}