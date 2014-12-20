package in.rob.client.widget;

import in.lib.Constants;
import in.lib.Constants.StreamList;
import in.lib.handler.streams.WidgetPostStreamResponseHandler;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.rob.client.MainActivity;
import in.rob.client.R;
import in.rob.client.ThreadActivity;
import in.rob.client.dialog.NewPostDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class WidgetBroadcastReciever extends BroadcastReceiver
{
	@Override public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals(Constants.ACTION_INTENT_NEW_POST))
		{
			createNewPost(context);
		}
		else if (intent.getAction().equals(Constants.ACTION_INTENT_OPEN_APP))
		{
			openApp(context);
		}
		else if (intent.getAction().equals(Constants.ACTION_INTENT_THREAD))
		{
			Intent thread = new Intent(context, ThreadActivity.class);
			thread.putExtras(intent.getExtras());
			context.startActivity(thread);
		}
		else if (intent.getAction().equals(Constants.ACTION_INTENT_REFRESH))
		{
			int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (id != -1)
			{
				refresh(context, id, intent.getStringExtra("stream"));
			}
		}
		else if (intent.getAction().equals(Constants.ACTION_INTENT_RELOAD))
		{
			int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (id != -1)
			{
				reloadWidget(context, id);
			}
		}
	}

	private void openApp(Context context)
	{
		Intent openAppIntent = new Intent(context, MainActivity.class);
		openAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(openAppIntent);
	}

	private void createNewPost(Context context)
	{
		Intent postIntent = new Intent(context, NewPostDialog.class);
		postIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		context.startActivity(postIntent);
	}

	private void refresh(final Context context, final int id, String stream)
	{
		NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notif = new NotificationCompat.Builder(context)
			.setTicker(context.getString(R.string.ptr_refreshing))
			.setContentTitle("Refreshing widget")
			.setProgress(100, 1, true)
			.setSmallIcon(R.drawable.notif)
		.build();

		manager.notify(id, notif);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String streamStr = prefs.getString("scroller_stream_id_" + id, StreamList.TIMELINE.toString());
		String accountId = prefs.getString(Constants.PREFS_SCROLL_WIDGET_USER_ID + id, UserManager.getUserId());

		WidgetPostStreamResponseHandler response = new WidgetPostStreamResponseHandler(context, String.format(streamStr, accountId))
		{
			@Override public void onCallback()
			{
				reloadWidget(context, id);
			}
		};

		if (streamStr.equals(StreamList.TIMELINE.toString()))
		{
			if (SettingsManager.isUsingUnified())
			{
				APIManager.getInstance().getUnifiedTimeLine("", response);
			}
			else
			{
				APIManager.getInstance().getTimeLine("", response);
			}
		}
		else if (streamStr.equals(StreamList.MENTIONS.toString()))
		{
			APIManager.getInstance().getMentions(accountId, "", response);
		}
		else if (streamStr.equals(StreamList.GLOBAL.toString()))
		{
			APIManager.getInstance().getGlobalTimeLine("", response);
		}
	}

	private void reloadWidget(Context context, int id)
	{
		Intent widgetUpdateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
		context.sendBroadcast(widgetUpdateIntent);
	}
}