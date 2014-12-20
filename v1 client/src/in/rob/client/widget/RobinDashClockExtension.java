package in.rob.client.widget;

import in.lib.Constants;
import in.lib.manager.CacheManager;
import in.lib.manager.UserManager;
import in.rob.client.MainActivity;
import in.rob.client.R;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class RobinDashClockExtension extends DashClockExtension
{
	@Override protected void onInitialize(boolean isReconnect)
	{
		super.onInitialize(isReconnect);
		setUpdateWhenScreenOn(true);
	}

	@Override protected void onUpdateData(int reason)
	{
		SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		String user = prefs.getString(Constants.PREFS_DASH_USER_ID, UserManager.getUserId());
		int count = prefs.getInt(Constants.PREFS_NOTIFICATION_COUNT + user, 0);

		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(Constants.EXTRA_START_PAGE, 1);
		intent.putExtra(Constants.EXTRA_FORCE_REFRESH, true);
		intent.putExtra(Constants.EXTRA_SELECT_USER, user);
		intent.putExtra(Constants.EXTRA_CLEAR_DASH, true);

		ArrayList<String> lines = CacheManager.getInstance().readFileAsObject(Constants.PREFS_NOTIFICATION_PREVIEW_LINES + user, ArrayList.class);

		if (count > 0 && lines != null && lines.size() > 0)
		{
			// Publish the extension data update.
			publishUpdate(new ExtensionData()
				.visible(true)
				.icon(R.drawable.notif)
				.status("" + count)
				.expandedTitle(count + " new")
				.expandedBody(lines.get(lines.size() - 1))
				.clickIntent(intent)
			);
		}
		else
		{
			publishUpdate(null);
		}
	}
}