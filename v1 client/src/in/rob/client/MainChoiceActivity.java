package in.rob.client;

import in.lib.handler.base.UserResponseHandler;
import in.lib.manager.APIManager;
import in.lib.manager.CacheManager;
import in.lib.manager.UserManager;
import in.lib.receiver.NotificationReceiver;
import in.model.User;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * Entry point of the application.
 *
 * Default preferences and cache removal is done here. Checks for active user session.
 */
public class MainChoiceActivity extends Activity implements OnClickListener
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		startMain();
	}

	@Override public void onClick(View v)
	{
		if (v.getId() == R.id.login_button)
		{
			startActivity(new Intent(this, AuthenticateActivity.class));
			finish();
		}
		else if (v.getId() == R.id.signup_button)
		{
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.join_url)));
			startActivity(browserIntent);
		}
	}

	/**
	 * Checks if a user is logged in and begins MainActivity, or shows the splash view
	 */
	public void startMain()
	{
		if (UserManager.isLoggedIn())
		{
			startMainActivity();
			return;
		}

		showSplash();

		if (UserManager.getUser() == null && !TextUtils.isEmpty(UserManager.getUserId()))
		{
			final ProgressDialog progress = new ProgressDialog(this);
			File f;

			if (getFilesDir() != null && (f = new File(getFilesDir().getAbsolutePath() + "user_" + UserManager.getUserId())).exists())
			{
				User u = CacheManager.getInstance().readFileAsObject(getFilesDir().getAbsolutePath() + "user_" + UserManager.getUserId(), User.class);

				if (u != null)
				{
					u.save();
					UserManager.setUser(u, this);
					new NotificationReceiver().registerUserForPush(this);
					startMainActivity();
					return;
				}
			}

			progress.setMessage(getString(R.string.logging_in));
			progress.setCanceledOnTouchOutside(false);
			progress.setOnCancelListener(new OnCancelListener()
			{
				@Override public void onCancel(DialogInterface dialog)
				{

				}
			});
			progress.show();

			APIManager.getInstance().getUserDetails(UserManager.getUserId(), new UserResponseHandler(this)
			{
				@Override public void onCallback()
				{
					if (getFilesDir() != null)
					{
						CacheManager.getInstance().writeFile(getFilesDir().getAbsolutePath() + "user_" + UserManager.getUserId(), getUser());
					}

					getUser().save();
					UserManager.setUser(getUser(), getContext());
					new NotificationReceiver().registerUserForPush(getContext());
				}

				@Override public void onFinish(boolean failed)
				{
					if (progress.isShowing())
					{
						progress.dismiss();

						if (failed)
						{
							Toast.makeText(getContext(), getContext().getString(R.string.user_fail), Toast.LENGTH_SHORT).show();
						}
						else
						{
							startMainActivity();
						}
					}
				}
			});
		}
	}

	public void showSplash()
	{
		setContentView(R.layout.splash_view);
		findViewById(R.id.login_button).setOnClickListener(this);
		findViewById(R.id.signup_button).setOnClickListener(this);
	}

	public void startMainActivity()
	{
		Intent main = new Intent(this, MainActivity.class);
		main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		main.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		main.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(main);
		finish();
	}
}