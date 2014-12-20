package in.lib.receiver;

import in.lib.Constants;
import in.lib.handler.UserFollowResponseHandler;
import in.lib.manager.APIManager;
import in.rob.client.R;
import in.rob.client.base.RobinDialogActivity;
import in.rob.client.dialog.base.ProgressBuilder;
import net.callumtaylor.asynchttp.AsyncHttpClient;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

public class FollowService extends RobinDialogActivity
{
	private int notificationId;
	private String userId;
	private String username;
	private String mode;
	private ProgressDialog progress;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getIntent().getExtras() != null)
		{
			notificationId = getIntent().getExtras().getInt(Constants.EXTRA_NOTIFICATION_ID);
			userId = getIntent().getExtras().getString(Constants.EXTRA_USER_ID);
			username = getIntent().getExtras().getString(Constants.EXTRA_USER_NAME);
			mode = getIntent().getExtras().getString(Constants.EXTRA_MODE);
		}

		progress = ProgressBuilder.create(getContext());
		progress.setMessage(getString(mode.equals("follow") ? R.string.following_user : R.string.unfollowing_user));
		progress.setCanceledOnTouchOutside(false);
		progress.show();

		getWindow().setBackgroundDrawable(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setVisible(false);

		AsyncHttpClient client;

		if (mode.equals("follow"))
		{
			client = APIManager.getInstance().followUser(userId, new UserFollowResponseHandler()
			{
				@Override public void onFinish(boolean failed)
				{
					if (!failed)
					{
						Toast.makeText(FollowService.this.getContext(), getString(R.string.follow_success, username), Toast.LENGTH_LONG).show();
					}

					finish();
				}
			});
		}
		else
		{
			client = APIManager.getInstance().unfollowUser(userId, new UserFollowResponseHandler()
			{
				@Override public void onFinish(boolean failed)
				{
					if (!failed)
					{
						Toast.makeText(FollowService.this.getContext(), getString(R.string.unfollow_success, username), Toast.LENGTH_LONG).show();
					}

					finish();
				}
			});
		}

		final AsyncHttpClient fClient = client;
		progress.setOnCancelListener(new OnCancelListener()
		{
			@Override public void onCancel(DialogInterface dialog)
			{
				fClient.cancel();
				finish();
			}
		});
	}

	@Override public void finish()
	{
		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(notificationId);

		super.finish();
	}

	@Override protected void onDestroy()
	{
		if (progress != null && progress.isShowing())
		{
			progress.dismiss();
		}

		super.onDestroy();
	}
}