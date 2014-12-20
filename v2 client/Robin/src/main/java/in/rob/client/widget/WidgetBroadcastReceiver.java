package in.rob.client.widget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import in.lib.Constants;
import in.rob.client.MainActivity;
import in.rob.client.ThreadActivity;
import in.rob.client.dialog.NewPostDialog;

public class WidgetBroadcastReceiver extends BroadcastReceiver
{
	@Override public void onReceive(Context context, Intent intent)
	{
		if (Constants.ACTION_INTENT_NEW_POST.equals(intent.getAction()))
		{
			createNewPost(context);
		}
		else if (Constants.ACTION_INTENT_OPEN_APP.equals(intent.getAction()))
		{
			openApp(context);
		}
		else if (Constants.ACTION_INTENT_THREAD.equals(intent.getAction()))
		{
			Intent thread = new Intent(context, ThreadActivity.class);

			if (intent.getExtras() != null)
			{
				thread.putExtras(intent.getExtras());
			}

			context.startActivity(thread);
		}
		else if (Constants.ACTION_INTENT_REFRESH.equals(intent.getAction()))
		{
			int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (id != -1)
			{
				refresh(context, id, intent.getStringExtra("stream"));
			}
		}
		else if (Constants.ACTION_INTENT_RELOAD.equals(intent.getAction()))
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

	}

	private void reloadWidget(Context context, int id)
	{
		Intent widgetUpdateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
		context.sendBroadcast(widgetUpdateIntent);
	}
}