package in.controller.handler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import in.controller.handler.base.DialogResponseHandler;
import in.model.ChannelMessage;
import in.rob.client.R;

public class ChannelMessageDialogResponseHandler extends DialogResponseHandler<ChannelMessage>
{
	private int notificationId;

	public ChannelMessageDialogResponseHandler(Context context, int notificationId)
	{
		super(context);
		this.notificationId = notificationId;
	}

	@Override public void onSend()
	{
		Notification.Builder notification;
		notification = new Notification.Builder(getContext());
		notification.setContentTitle(getContext().getString(R.string.sending_message_title));
		notification.setContentText(getContext().getString(R.string.sending_message));
		notification.setContentIntent(PendingIntent.getActivity(getContext(), 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT));
		notification.setTicker(getContext().getString(R.string.sending_message));
		notification.setSmallIcon(R.drawable.ic_launcher);
		notification.setProgress(0, 0, true);

		NotificationManager notificationManager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notificationId, notification.build());
	}

	@Override public void onSuccess()
	{
		if (getContent() != null)
		{
			setResponse(new ChannelMessage().createFrom(getContent()));
		}
	}

	@Override public void onFinish(boolean failed)
	{
		if (getContext() != null)
		{
			NotificationManager notificationManager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);

			Notification.Builder notification = new Notification.Builder(getContext());
			notification.setTicker(getContext().getString(R.string.message_success));
			notification.setContentIntent(PendingIntent.getActivity(getContext(), 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT));
			notification.setSmallIcon(R.drawable.ic_launcher);
			notification.setWhen(System.currentTimeMillis());

			notificationManager.notify(notificationId, notification.getNotification());
			notificationManager.cancel(notificationId);
		}
	}
}
