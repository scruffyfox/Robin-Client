package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.event.NewPostDraftEvent;
import in.lib.event.UpdatedPostDraftEvent;
import in.lib.handler.base.ImageResponseHandler;
import in.lib.handler.dialogs.ImagePostDialogResponseHandler;
import in.lib.handler.dialogs.NewPostDialogResponseHandler;
import in.lib.helper.BusHelper;
import in.lib.manager.APIManager;
import in.lib.manager.CacheManager;
import in.lib.manager.ImageAPIManager;
import in.lib.manager.UserManager;
import in.obj.annotation.Annotation;
import in.obj.annotation.FileAnnotation;
import in.rob.client.AuthenticateActivity;
import in.rob.client.R;
import in.rob.client.dialog.base.DialogBuilder;
import in.rob.client.dialog.base.PostDialog;

import java.util.ArrayList;
import java.util.Random;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

/**
 * New post dialog for creating a straight up new post.
 *
 * Possible extra arguments:
 * <ul>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_USER}</b>: The selected user to post from</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_TEXT}</b>: The text to prefill the dialog with</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_MENTION_NAME}</b>: The username (without @) of the user to mention</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_TAG_NAME}</b>: The tag (without #) of the tag to mention</li>
 * </ul>
 */
public class NewPostDialog extends PostDialog
{
	private NotificationManager mNotificationManager;
	private Intent mFailIntent;
	private int mNotificationId;
	protected String tempTitle = "";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationId = new Random().nextInt();

		super.onCreate(savedInstanceState);

		if (!UserManager.isLoggedIn())
		{
			Intent auth = new Intent(getContext(), AuthenticateActivity.class);
			auth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			auth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			auth.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			auth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(auth);
			finish();
			return;
		}

		setTitle(tempTitle);
	}

	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		tempTitle = getString(R.string.new_post);

		if (instances != null)
		{
			// Get intent, action and MIME type
			if (getIntent() != null)
			{
				Intent intent = getIntent();
				String action = intent.getAction();
				String type = intent.getType();

				if (Intent.ACTION_SEND.equals(action) && type != null)
				{
					if ("text/plain".equals(type))
					{
						String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
						if (sharedText != null)
						{
							getCurrentPost().setPostText(sharedText);
						}
					}
					else if (type.startsWith("image/"))
					{
						Uri uri = (Uri)intent.getExtras().get(Intent.EXTRA_STREAM);
						getCurrentPost().setImagePath(uri.toString());
					}
				}
			}

			if (instances.containsKey(Constants.EXTRA_MENTION_NAME))
			{
				String replyTo = instances.getString(Constants.EXTRA_MENTION_NAME);
				getCurrentPost().setPostText("@" + replyTo + " ");
				tempTitle = String.format(getString(R.string.mention_user), replyTo);
			}

			if (instances.containsKey(Constants.EXTRA_TAG_NAME))
			{
				String replyTo = instances.getString(Constants.EXTRA_TAG_NAME);
				getCurrentPost().setPostText(replyTo + " ");
			}

			if (instances.containsKey(Constants.EXTRA_TEXT))
			{
				getCurrentPost().setPostText(instances.getString(Constants.EXTRA_TEXT));
			}

			if (instances.containsKey(Constants.EXTRA_RESEND) && instances.getBoolean(Constants.EXTRA_RESEND) == true)
			{
				positiveControl();
				finish();
			}
		}
	}

	@Override public void positiveControl()
	{
		if (TextUtils.isEmpty(getCurrentPost().getPostText()) && TextUtils.isEmpty(getCurrentPost().getImagePath()))
		{
			return;
		}

		// save the post to drafts in case it fails.
		CacheManager.getInstance().writeFile(String.format(Constants.CACHE_DRAFT_POST, getCurrentPost().getSelectedAccountId(), getCurrentPost().getDate()), getCurrentPost());

		mFailIntent = new Intent(getApplicationContext(), NewPostDialog.class);
		mFailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		mFailIntent.putExtra(Constants.EXTRA_NEW_POST_DRAFT, getCurrentPost().serialize());

		if ((getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Constants.EXTRA_NEW_POST_SKIP_IMAGE, false)) || TextUtils.isEmpty(getCurrentPost().getImagePath()))
		{
			sendNotification(getString(R.string.sending_post_title), getString(R.string.sending_post));
			String token = UserManager.getAuths(getContext()).get(getSelectedUser().getId()).getAccessToken();

			if (TextUtils.isEmpty(getCurrentPost().getReplyId()))
			{
				APIManager.getInstance().postStatus(token, getCurrentPost(), new NewPostDialogResponseHandler(getContext(), mFailIntent, mNotificationId));
			}
			else
			{
				APIManager.getInstance().replyPost(token, getCurrentPost(), new NewPostDialogResponseHandler(getContext(), mFailIntent, mNotificationId));
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
					Annotation image = getImage();
					ArrayList<Annotation> annotations = new ArrayList<Annotation>();
					annotations.add(image);

					if (getImage() instanceof FileAnnotation)
					{
						getCurrentPost().setPostText((getCurrentPost().getPostText().trim() + " - photos.app.net/{post_id}/1").trim());
					}
					else
					{
						getCurrentPost().setPostText((getCurrentPost().getPostText().trim() + " " + getImage().getTextUrl()).trim());
					}

					getCurrentPost().getAnnotations().addAll(annotations);
					sendNotification(getString(R.string.sending_post_title), getString(R.string.sending_post));

					mFailIntent = new Intent(getApplicationContext(), NewPostDialog.class);
					mFailIntent.putExtra(Constants.EXTRA_NEW_POST_DRAFT, getCurrentPost().serialize());
					mFailIntent.putExtra(Constants.EXTRA_NEW_POST_SKIP_IMAGE, true);

					if (TextUtils.isEmpty(getCurrentPost().getReplyId()))
					{
						APIManager.getInstance().postStatus(token, getCurrentPost(), new ImagePostDialogResponseHandler(getApplicationContext(), mFailIntent, mNotificationId));
					}
					else
					{
						APIManager.getInstance().replyPost(token, getCurrentPost(), new ImagePostDialogResponseHandler(getApplicationContext(), mFailIntent, mNotificationId));
					}
				}

				@Override public void onPublishedUploadProgressUI(long totalProcessed, long totalLength)
				{
					notification.setProgress((int)totalLength, (int)totalProcessed, false);
					mNotificationManager.notify(getNotificationId(), notification.build());
				}
			});
		}
	}

	@Override public void onBackPressed()
	{
		updateDraftPost();
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
			.setMessage(R.string.save_to_drafts)
			.setPositiveButton(R.string.yes, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					String file = String.format(Constants.CACHE_DRAFT_POST, getCurrentPost().getSelectedAccountId(), getCurrentPost().getDate());

					if (CacheManager.getInstance().fileExists(file))
					{
						BusHelper.getInstance().post(new UpdatedPostDraftEvent(getCurrentPost()));
					}
					else
					{
						BusHelper.getInstance().post(new NewPostDraftEvent(getCurrentPost()));
					}

					CacheManager.getInstance().writeFile(file, getCurrentPost());
					finish = true;
					negativeControl();
				}
			})
			.setNegativeButton(R.string.no, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					finish = true;
					negativeControl();
				}
			})
			.setNeutralButton(R.string.cancel, null)
		.show();
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putString(Constants.EXTRA_TITLE, tempTitle);
		super.onSaveInstanceState(outState);
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

		CharSequence contentTitle = getString(R.string.app_name);
		CharSequence contentText = content;
		Intent notificationIntent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		notification.setContentIntent(contentIntent);
		mNotificationManager.notify(mNotificationId, notification.build());
	}

	@Override public int getContentView()
	{
		return R.layout.new_post_dialog;
	}
}