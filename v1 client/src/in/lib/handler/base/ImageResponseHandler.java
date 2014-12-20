package in.lib.handler.base;

import in.lib.Debug;
import in.lib.exception.ExceptionHandler;
import in.lib.manager.ImageAPIManager.Provider;
import in.lib.manager.SettingsManager;
import in.obj.annotation.FileAnnotation;
import in.obj.annotation.ImageAnnotation;
import in.rob.client.MainApplication;
import in.rob.client.MainApplication.ApplicationType;
import in.rob.client.R;
import lombok.Getter;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;

import com.google.gson.JsonObject;

/**
 * Standard response handler for Dialog posting. Handles broadcasts on callback.
 */
public abstract class ImageResponseHandler extends JsonResponseHandler
{
	@Getter private ImageAnnotation image;
	@Getter protected Intent failIntent;
	@Getter protected int notificationId;
	@Getter protected CharSequence contentTitle;
	@Getter protected Context context;
	@Getter NotificationManager manager;

	public ImageResponseHandler(Context context, Intent failIntent, int sendNotificationId)
	{
		this.context = context;
		this.failIntent = failIntent;
		this.notificationId = sendNotificationId;
		this.contentTitle = context.getString(R.string.upload_image_fail);
		this.manager = ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE));
	}

	@Override public void onSuccess()
	{
		try
		{
			JsonObject response = getContent().getAsJsonObject();
			image = new ImageAnnotation();

			if (SettingsManager.getImageProvider() == Provider.BLIMS)
			{
				response = response.get("result").getAsJsonObject();
				image.setUrl(response.get("url").getAsString());
				image.setTextUrl(response.get("embeddable_url").getAsString());
				image.setEmbeddableUrl(response.get("embeddable_url").getAsString());
				image.setWidth(response.get("width").getAsInt());
				image.setHeight(response.get("height").getAsInt());
				image.setThumbUrl(response.get("thumbnail_url").getAsString());
				image.setThumbWidth(response.get("thumbnail_width").getAsInt());
				image.setThumbHeight(response.get("thumbnail_height").getAsInt());
			}
			else if (SettingsManager.getImageProvider() == Provider.IMGLY)
			{
				image.setUrl("http://img.ly/show/full/" + response.get("id").getAsString());
				image.setTextUrl(response.get("url").getAsString());
				image.setWidth(response.get("width").getAsInt());
				image.setHeight(response.get("height").getAsInt());
			}
			else if (SettingsManager.getImageProvider() == Provider.APPNET)
			{
				image = new FileAnnotation();
				((FileAnnotation)image).setFileId(response.get("data").getAsJsonObject().get("id").getAsString());
				((FileAnnotation)image).setFileToken(response.get("data").getAsJsonObject().get("file_token").getAsString());
			}

			onCallback();
		}
		catch (Exception e)
		{
			if (((MainApplication)context.getApplicationContext()).getApplicationType() == ApplicationType.BETA)
			{
				Exception test = new Exception(e.getMessage());
				ExceptionHandler.sendException(test);
			}

			Debug.out(e);
			onFailure();
		}
	}

	@Override public void onFailure()
	{
		if (((MainApplication)context.getApplicationContext()).getApplicationType() == ApplicationType.BETA)
		{
			try
			{
				Exception e = new Exception(getConnectionInfo() + "\n" + (getContent() == null ? "" : getContent()));
				ExceptionHandler.sendException(e);
				Debug.out(e.getMessage());
			}
			catch (Exception e){}
		}

		CharSequence contentText = getContext().getString(R.string.upload_failed);

		if (SettingsManager.getImageProvider() == Provider.APPNET)
		{
			try
			{
				if (getConnectionInfo().responseCode == 507)
				{
					contentText = context.getString(R.string.insufficient_storage);
				}
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		if (failIntent != null)
		{
			BigTextStyle style = new BigTextStyle();
			style.bigText(contentText);

			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, failIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			Notification notification = new NotificationCompat.Builder(getContext())
				.setTicker(contentText)
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

	/**
	 * Implement this callback
	 */
	public abstract void onCallback();
}