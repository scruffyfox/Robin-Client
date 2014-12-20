package in.rob.client.widget;

import in.lib.Constants;
import in.lib.Constants.StreamList;
import in.rob.client.R;
import in.rob.client.ThreadActivity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.RemoteViews;

public class ScrollWidgetProvider extends AppWidgetProvider
{
	@Override public void onEnabled(Context context)
	{
		super.onEnabled(context);
	}

	@Override public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);

		if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE))
		{
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

			int idToUpdate = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

			if (idToUpdate != -1)
			{
				// Perform data update
				appWidgetManager.notifyAppWidgetViewDataChanged(new int[]{idToUpdate}, R.id.widget_post_feed_view);

				updateAppWidget(context, appWidgetManager, idToUpdate);
			}
		}
	}

	@Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		for (int i = 0; i < appWidgetIds.length; i++)
		{
			updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
		}

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
	{
		// stream string
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String streamStr = prefs.getString("scroller_stream_id_" + appWidgetId, StreamList.TIMELINE.toString());

		Intent serviceIntent = new Intent(context, ScrollWidgetService.class);

		serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

		int layoutRes = prefs.getString("scroller_theme_" + appWidgetId, "Light").equals("Light") ? R.layout.light_scroll_widget : R.layout.dark_scroll_widget;

		RemoteViews widget = new RemoteViews(context.getPackageName(), layoutRes);
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

		if (!TextUtils.isEmpty(streamStr))
		{
			widget.setTextViewText(R.id.title, context.getString(StreamList.getStreamFromString(streamStr).getLabelRes()));
		}

		appWidgetManager.updateAppWidget(appWidgetId, widget);
	}
}