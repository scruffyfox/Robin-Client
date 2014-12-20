package in.rob.client.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;

import in.rob.client.R;

public class UpdateWidgetService extends Service
{
	@Override public int onStartCommand(Intent intent, int flags, int startId)
	{
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

		String type = intent.getStringExtra("type");
		int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		for (int appWidgetId : allWidgetIds)
		{
			if ("scroll".equals(type))
			{
				appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_post_feed_view);
			}
		}

		stopSelf();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override public IBinder onBind(Intent intent)
	{
		return null;
	}
} 