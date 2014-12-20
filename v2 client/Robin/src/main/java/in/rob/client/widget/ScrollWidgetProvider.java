package in.rob.client.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import in.lib.Constants;
import in.rob.client.R;
import in.rob.client.ThreadActivity;

public class ScrollWidgetProvider extends AppWidgetProvider
{
	@Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		ComponentName thisWidget = new ComponentName(context, ScrollWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		for (int appWidgetId : allWidgetIds)
		{
			Intent serviceIntent = new Intent(context, ScrollWidgetService.class);
			serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

			RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.scroll_widget_light);
			widget.setRemoteAdapter(R.id.widget_post_feed_view, serviceIntent);

			Intent threadIntent = new Intent(context, ThreadActivity.class);
			PendingIntent threadPendingIntent = PendingIntent.getActivity(context, 0, threadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setPendingIntentTemplate(R.id.widget_post_feed_view, threadPendingIntent);

			Intent newPostIntent = new Intent(Constants.ACTION_INTENT_NEW_POST);
			PendingIntent pendingNewPostIntent = PendingIntent.getBroadcast(context, 0, newPostIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickPendingIntent(R.id.widget_new_post, pendingNewPostIntent);

			Intent refreshIntent = new Intent(Constants.ACTION_INTENT_REFRESH);
			refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			PendingIntent pendingRefreshIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickPendingIntent(R.id.widget_refresh, pendingRefreshIntent);

			Intent openAppIntent = new Intent(Constants.ACTION_INTENT_OPEN_APP);
			PendingIntent pendingOpenAppIntent = PendingIntent.getBroadcast(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setOnClickPendingIntent(R.id.widget_logo, pendingOpenAppIntent);

			appWidgetManager.updateAppWidget(appWidgetId, widget);
		}

		// Build the intent to call the service
//		Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
//		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
//		intent.putExtra("type", "scroll");
//
//		final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		service = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//		Debug.out("SETTING EXACT");
//		m.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, service);
	}
}