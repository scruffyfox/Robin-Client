package in.rob.client.base;

import in.lib.Constants;
import in.lib.manager.SettingsManager;
import in.rob.client.MainApplication;
import in.rob.client.R;

import java.util.Locale;

import lombok.Getter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class RobinActivity extends ActionBarActivity
{
	/**
	 * Application instance context. Use this for the most part
	 */
	@Getter private Context context;

	protected static void initActionBar(final ActionBarActivity ctx)
	{
		ctx.getSupportActionBar().setCustomView(R.layout.action_bar);
		ctx.getSupportActionBar().setDisplayShowCustomEnabled(true);
		ctx.getSupportActionBar().setDisplayShowHomeEnabled(false);

		// sliding menu custom toggle
		ImageButton upBtn = (ImageButton)ctx.getSupportActionBar().getCustomView().findViewById(R.id.up_button);
		upBtn.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				if (v.getId() == R.id.up_button)
				{
					ctx.finish();
					return;
				}
			}
		});
	}

	/**
	 * @return Gets the base 64 rehashable id of the device
	 */
	public String getDeviceId()
	{
		return ((MainApplication)getApplication()).getDeviceId();
	}

	@Override public void setTitle(CharSequence title)
	{
		((TextView)getSupportActionBar().getCustomView().findViewById(R.id.title)).setText(title);
	}

	// common stuff shared between all activities (eg Refresh after settings)
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.context = this;

		String styleName = SettingsManager.getThemeName();
		int styleRes = getResources().getIdentifier(styleName, "style", getPackageName());
		try
		{
			setTheme(styleRes);
		}
		catch (Exception e)
		{
			setTheme(R.style.DefaultLight);
		}

		setLocale();

		RobinActivity.initActionBar(this);
	}

	public void setLocale()
	{
		String languageToLoad = SettingsManager.getLocale();

		if (TextUtils.isEmpty(languageToLoad))
		{
			languageToLoad = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(Constants.PREFS_DEFAULT_LOCALE, "en");
		}

		Locale locale = new Locale(languageToLoad);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Constants.RESULT_REFRESH)
		{
			setResult(Constants.RESULT_REFRESH, data);
		}
	}

	@Override public boolean onKeyLongPress(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			//startActivity(new Intent(this, SearchActivity.class));
			return true;
		}

		return super.onKeyLongPress(keyCode, event);
	}

	@Override public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			event.startTracking();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override protected void onStart()
	{
		super.onStart();

		if (SettingsManager.isAnalyticsEnabled())
		{
			EasyTracker.getInstance().activityStart(this);
		}
	}

	@Override protected void onStop()
	{
		super.onStop();

		if (SettingsManager.isAnalyticsEnabled())
		{
			EasyTracker.getInstance().activityStop(this);
		}
	}
}