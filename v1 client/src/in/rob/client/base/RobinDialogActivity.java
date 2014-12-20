package in.rob.client.base;

import in.lib.Constants;
import in.lib.manager.SettingsManager;
import in.rob.client.MainApplication;
import in.rob.client.R;
import lombok.Getter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.google.analytics.tracking.android.EasyTracker;

public class RobinDialogActivity extends Activity
{
	/**
	 * Application instance context. Use this for the most part
	 */
	@Getter private Context context;

	/**
	 * @return Gets the base 64 rehashable id of the device
	 */
	public String getDeviceId()
	{
		return ((MainApplication)getApplication()).getDeviceId();
	}

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.context = this;

		try
		{
			String styleName = SettingsManager.getThemeName();
			int styleRes = getResources().getIdentifier(styleName + ".Dialog", "style", getPackageName());
			setTheme(styleRes);
		}
		catch (Exception e)
		{
			setTheme(R.style.DefaultLight_Dialog);
		}
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