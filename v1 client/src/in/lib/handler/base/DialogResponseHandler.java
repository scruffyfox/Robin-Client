package in.lib.handler.base;

import in.lib.Debug;
import in.lib.exception.ExceptionHandler;
import in.lib.manager.SettingsManager;
import in.model.Post;
import in.rob.client.R;
import lombok.Getter;
import lombok.Setter;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Standard response handler for Dialog posting. Handles broadcasts on callback.
 */
public class DialogResponseHandler extends ResponseHandler
{
	@Getter private Post post;
	@Getter private Context context;
	@Getter private Intent failIntent;
	@Getter private int notificationId;
	@Setter private CharSequence contentTitle;
	@Getter NotificationManager manager;

	public DialogResponseHandler()
	{
		throw new IllegalAccessError("You should not be using this constructor");
	}

	public DialogResponseHandler(Context context)
	{
		this(context, null, -1);
	}

	public DialogResponseHandler(Context context, Intent failIntent, int sendNotificationId)
	{
		this.context = context;
		this.failIntent = failIntent;
		this.notificationId = sendNotificationId;
		this.manager = ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE));
		contentTitle = context.getString(R.string.send_post_fail);
	}

	@Override public void onSuccess()
	{
		try
		{
			JsonElement elements = getContent();
			JsonObject jPost = elements.getAsJsonObject().get("data").getAsJsonObject();
			post = new Post().createFrom(jPost, true);
			post.setNewPost(true);
		}
		catch (Exception e)
		{
			Debug.out(e);
			if (SettingsManager.isCrashReportEnabled())
			{
				ExceptionHandler.sendException(e);
			}

			onFailure();
		}
	}

	public String getFailText()
	{
		return context.getString(R.string.vague_error);
	}

	@Override public void onFailure()
	{
		Debug.out(getConnectionInfo());

		if (failIntent != null)
		{
			CharSequence contentText = context.getString(R.string.tap_to_retry);
			BigTextStyle style = new BigTextStyle();
			style.bigText(contentText);

			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, failIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			Notification notification = new NotificationCompat.Builder(getContext())
				.setTicker(getFailText())
				.setContentTitle(contentTitle)
				.setStyle(style)
				.setContentText(contentText)
				.setSmallIcon(R.drawable.notif)
				.setWhen(System.currentTimeMillis())
				.setAutoCancel(true)
				.setContentIntent(contentIntent)
			.build();

			manager.notify(notificationId, notification);
		}
	}

	@Override public void onFinish(boolean failed)
	{
		super.onFinish(failed);
		context = null;
	}
}