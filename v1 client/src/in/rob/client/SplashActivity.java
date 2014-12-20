package in.rob.client;

import in.lib.Constants;
import in.lib.manager.CacheManager;
import in.lib.manager.UserManager;
import in.rob.client.MainApplication.ApplicationType;
import in.rob.client.dialog.base.DialogBuilder;

import java.util.Locale;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

/**
 * Entry point of the application.
 *
 * Default preferences and cache removal is done here. Checks for active user session.
 */
public class SplashActivity extends ActionBarActivity
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// sets the default settings
		PreferenceManager.setDefaultValues(getApplicationContext(), Constants.PREFS_SETTINGS_KEY, Context.MODE_PRIVATE, R.xml.prefs, false);
		SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

		if (!prefs.contains(Constants.PREFS_DEFAULT_LOCALE))
		{
			prefs.edit().putString(Constants.PREFS_DEFAULT_LOCALE, Locale.getDefault().getLanguage());
		}

		if (CacheManager.getInstance().fileExists("update.apk"))
		{
			CacheManager.getInstance().removeFile("update.apk");
		}

		if (((MainApplication)getApplication()).getApplicationType() == ApplicationType.BETA)
		{
			// check the prefs
			if (!prefs.getBoolean(Constants.PREFS_HAS_BETA, false) && UserManager.isLoggedIn())
			{
				UserManager.logout(this);
			}
		}
		else if (((MainApplication)getApplication()).getApplicationType() == ApplicationType.CD_KEY)
		{
			if (prefs.getBoolean(Constants.PREFS_KEY_BLACK_LISTED, false) && UserManager.isLoggedIn())
			{
				UserManager.logout(this);

				DialogBuilder.create(this)
					.setTitle(R.string.thank_you)
					.setMessage(R.string.key_black_listed)
					.setPositiveButton(R.string.close, null)
					.setOnDismissListener(new OnDismissListener()
					{
						@Override public void onDismiss(DialogInterface dialog)
						{
							try
							{
								Intent market = new Intent(Intent.ACTION_VIEW);
								market.setData(Uri.parse("market://details?id=" + getPackageName()));
								startActivity(market);
							}
							catch (Exception e)
							{
								Intent market = new Intent(Intent.ACTION_VIEW);
								market.setData(Uri.parse("https://robinapp.net/buy.html"));
								startActivity(market);
							}

							finish();
						}
					})
				.show();

				return;
			}
		}

		startMain();
	}

	/**
	 * Checks if a user is logged in and begins MainActivity, or shows the splash view
	 */
	public void startMain()
	{
		if (UserManager.isLoggedIn())
		{
			startMainActivity();
		}
		else
		{
			showSplash();
		}
	}

	public void showSplash()
	{
		Intent main = new Intent(this, MainChoiceActivity.class);
		main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		main.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		main.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(main);
		finish();
	}

	public void startMainActivity()
	{
		Intent main = new Intent(this, MainActivity.class);
		main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(main);
		finish();
	}
}