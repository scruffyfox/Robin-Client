package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.handler.dialogs.DeletePrivateMessageDialogResponseHandler;
import in.lib.manager.APIManager;
import in.lib.utils.Views;
import in.lib.view.LinkifiedTextView;
import in.model.PrivateMessage;
import in.rob.client.MainApplication;
import in.rob.client.R;
import in.rob.client.dialog.base.PostDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Dialog used to delete a post
 *
 * Possible extra arguments:
 * <ul>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_MESSAGE}</b>: The {@link PrivateMessage} object to delete</li>
 * </ul>
 */
public class DeleteMessageDialog extends PostDialog
{
	@InjectView(R.id.time) public TextView time;
	@InjectView(R.id.post_title) public TextView title;
	@InjectView(R.id.sub_title) public TextView subtitle;

	private PrivateMessage mRelatedMessage;
	private int mNotificationId;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle(R.string.confirm);
		mNotificationId = (int)System.currentTimeMillis();
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	}

	@Override protected void initDialog()
	{
		Views.inject(this);

		ImageLoader.getInstance().displayImage(mRelatedMessage.getPoster().getAvatarUrl() + "?avatar=1&id=" + mRelatedMessage.getPoster().getId(), ((ImageView)findViewById(R.id.avatar)), MainApplication.getAvatarImageOptions());

		((LinkifiedTextView)getInput()).setText(mRelatedMessage.getFormattedText());
		((LinkifiedTextView)getInput()).setLinkMovementMethod(null);

		time.setText(mRelatedMessage.getDateStr());
		title.setText(mRelatedMessage.getPoster().getFormattedMentionName()[0]);
		subtitle.setText(mRelatedMessage.getPoster().getFormattedMentionName()[1]);
	}


	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		if (instances.containsKey(Constants.EXTRA_MESSAGE))
		{
			mRelatedMessage = (PrivateMessage)instances.getParcelable(Constants.EXTRA_MESSAGE);
		}
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putParcelable(Constants.EXTRA_MESSAGE, mRelatedMessage);
		super.onSaveInstanceState(outState);
	}

	@Override public void positiveControl()
	{
		Intent mFailIntent = new Intent(getApplicationContext(), DeleteMessageDialog.class);
		mFailIntent.putExtra(Constants.EXTRA_MESSAGE, mRelatedMessage);

		sendNotification(getString(R.string.deleting_message_title), getString(R.string.deleting_message));
		APIManager.getInstance().deleteMessage(mRelatedMessage.getChannelId(), mRelatedMessage.getId(), new DeletePrivateMessageDialogResponseHandler(this, mFailIntent, mNotificationId));
	}

	public void sendNotification(String title, String content)
	{
		NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.notif, title, System.currentTimeMillis());

		CharSequence contentTitle = getString(R.string.app_name);
		CharSequence contentText = content;
		Intent notificationIntent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		//notification.flags |= Notification.FLAG_ONGOING_EVENT;

		notification.setLatestEventInfo(getContext(), contentTitle, contentText, contentIntent);
		mNotificationManager.notify(mNotificationId, notification);
	}

	@Override public int getContentView()
	{
		return R.layout.delete_post_dialog;
	}
}