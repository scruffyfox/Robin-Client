package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.handler.dialogs.NewPrivateMessageDialogResponseHandler;
import in.lib.manager.APIManager;
import in.model.SimpleUser;
import in.rob.client.R;
import in.rob.client.dialog.base.DialogBuilder;
import in.rob.client.dialog.base.PostDialog;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

/**
 * New post dialog for creating a channel.
 *
 * Required arguments:
 * <ul>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_USER_LIST}</b>: The list of users to include in the creation</li>
 * </ul>
 */
public class NewChannelDialog extends PostDialog
{
	private NotificationManager mNotificationManager;
	private int mNotificationId;
	private Intent mFailIntent;
	private ArrayList<SimpleUser> mUsers = new ArrayList<SimpleUser>();
	private StringBuilder title;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationId = (int)System.currentTimeMillis();
		super.onCreate(savedInstanceState);
		setTitle(title);
	}

	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		title = new StringBuilder();
		title.append(getString(R.string.new_message));

		if (instances != null)
		{
			if (instances.containsKey(Constants.EXTRA_USER_LIST))
			{
				mUsers = instances.getParcelableArrayList(Constants.EXTRA_USER_LIST);

				title.append(": ");
				int index = 0;
				for (SimpleUser user : mUsers)
				{
					if (user.isYou()) continue;

					if (index++ > 0)
					{
						title.append(", ");
					}

					title.append("@").append(user.getMentionName());
				}
			}

			if (instances.containsKey(Constants.EXTRA_RESEND))
			{
				positiveControl();
				finish();
			}
		}
	}

	@Override public void positiveControl()
	{
		if (TextUtils.isEmpty(getCurrentPost().getPostText()) && TextUtils.isEmpty(getCurrentPost().getImagePath()) && mUsers.size() > 0)
		{
			return;
		}

		mFailIntent = new Intent(getApplicationContext(), NewChannelDialog.class);
		mFailIntent.putExtra(Constants.EXTRA_NEW_POST_DRAFT, getCurrentPost().serialize());
		mFailIntent.putExtra(Constants.EXTRA_USER_LIST, mUsers);
		//mFailIntent.putExtra(Constants.EXTRA_RESEND, true);

		if (getIntent().getExtras() != null)
		{
			sendNotification(getString(R.string.sending_message_title), getString(R.string.sending_message));

			APIManager.getInstance().createChannelMessage
			(
				mUsers,
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
		return R.layout.new_channel_dialog;
	}
}