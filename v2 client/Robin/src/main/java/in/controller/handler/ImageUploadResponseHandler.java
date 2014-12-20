package in.controller.handler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import in.controller.handler.base.ResponseHandler;
import in.data.annotation.FileAnnotation;
import in.lib.utils.Debug;
import in.rob.client.R;
import lombok.Getter;

public class ImageUploadResponseHandler extends ResponseHandler
{
	@Getter private FileAnnotation image;
	@Getter private Context context;
	private int notificationId;
	private Notification.Builder notification;
	private NotificationManager notificationManager;

	public ImageUploadResponseHandler(Context context, int notificationId)
	{
		this.context = context.getApplicationContext();
		this.notificationId = notificationId;
		this.notificationManager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override public void onSend()
	{
		notification = new Notification.Builder(getContext());
		notification.setContentTitle(getContext().getString(R.string.uploading_image_title));
		notification.setContentIntent(PendingIntent.getActivity(getContext(), 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT));
	}

	@Override public void onSuccess()
	{
		image = new FileAnnotation().createFrom(getContent());
	}

	@Override public void onFinish(boolean failed)
	{
		if (failed)
		{
			Debug.out(getConnectionInfo());
			Debug.out(getContent());
		}
	}

	public void sendNotification(String ticker)
	{
		notification.setContentText(ticker);
		notification.setTicker(ticker);
		notification.setSmallIcon(android.R.drawable.stat_sys_upload);
		notification.setProgress(0, 0, true);

		notificationManager.notify(notificationId, notification.getNotification());
	}

	private int lastProgress = -1;
	@Override public void onPublishedUploadProgressUI(long totalProcessed, long totalLength)
	{
		int progress = (int)(((double)totalProcessed / (double)totalLength) * 100.0d);
		if (progress > lastProgress)
		{
			notification.setProgress(100, progress, false);
			notificationManager.notify(notificationId, notification.getNotification());
			lastProgress = progress;
		}
	}
}
