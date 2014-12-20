package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.handler.dialogs.DeletePostDialogResponseHandler;
import in.lib.manager.APIManager;
import in.lib.manager.UserManager;
import in.lib.utils.Views;
import in.lib.view.LinkifiedTextView;
import in.model.Post;
import in.rob.client.MainApplication;
import in.rob.client.R;
import in.rob.client.dialog.base.PostDialog;

import java.util.Random;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Dialog used to delete a post
 *
 * Possible extra arguments:
 * <ul>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_POST}</b>: The {@link Post} object to delete</li>
 * </ul>
 */
public class DeletePostDialog extends PostDialog
{
	@InjectView(R.id.time) public TextView time;
	@InjectView(R.id.post_title) public TextView title;
	@InjectView(R.id.sub_title) public TextView subtitle;

	private Post mRelatedPost;
	private NotificationManager mNotificationManager;
	private int mNotificationId;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationId = new Random().nextInt();

		setTitle(R.string.confirm);
		getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override protected void initDialog()
	{
		Views.inject(this);

		ImageLoader.getInstance().displayImage(mRelatedPost.getPoster().getAvatarUrl() + "?avatar=1&id=" + mRelatedPost.getPoster().getId(), ((ImageView)findViewById(R.id.avatar)), MainApplication.getAvatarImageOptions());

		((LinkifiedTextView)getInput()).setText(mRelatedPost.getFormattedText());
		((LinkifiedTextView)getInput()).setLinkMovementMethod(null);

		time.setText(mRelatedPost.getDateStr());
		title.setText(mRelatedPost.getPoster().getFormattedMentionName()[0]);
		subtitle.setText(mRelatedPost.getPoster().getFormattedMentionName()[1]);
	}

	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		if (instances.containsKey(Constants.EXTRA_POST))
		{
			mRelatedPost = (Post)instances.getParcelable(Constants.EXTRA_POST);
		}
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putParcelable(Constants.EXTRA_POST, mRelatedPost);
		super.onSaveInstanceState(outState);
	}

	@Override public void positiveControl()
	{
		Intent mFailIntent = new Intent(getApplicationContext(), DeletePostDialog.class);
		mFailIntent.putExtra(Constants.EXTRA_POST, mRelatedPost);

		sendNotification(getString(R.string.deleting_post_title), getString(R.string.deleting_post));
		final String token = UserManager.getAuths(getContext()).get(mRelatedPost.getPoster().getId()).getAccessToken();
		APIManager.getInstance().deletePost(token, mRelatedPost.getId(), new DeletePostDialogResponseHandler(getContext(), mFailIntent, mNotificationId));
	}

	public void sendNotification(String title, String content)
	{
		NotificationCompat.Builder notification = new NotificationCompat.Builder(getContext());
		notification.setContentTitle(title);
		notification.setContentText(content);
		notification.setTicker(content);
		notification.setSmallIcon(R.drawable.notif);

		CharSequence contentTitle = getString(R.string.app_name);
		CharSequence contentText = content;
		Intent notificationIntent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		//notification.setOngoing(true);
		notification.setContentIntent(contentIntent);
		mNotificationManager.notify(mNotificationId, notification.build());
	}

	@Override public int getContentView()
	{
		return R.layout.delete_post_dialog;
	}
}