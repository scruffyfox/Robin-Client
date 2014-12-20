package in.lib.handler.dialogs;

import in.lib.event.NewRepostEvent;
import in.lib.handler.base.DialogResponseHandler;
import in.lib.helper.BusHelper;
import in.rob.client.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class RepostDialogResponseHandler extends DialogResponseHandler
{
	public RepostDialogResponseHandler(Context context, Intent failIntent, int sendNotificationId)
	{
		super(context, failIntent, sendNotificationId);
	}

	@Override public String getFailText()
	{
		return getContext().getString(R.string.vague_error);
	}

	@Override public void onFinish(boolean failed)
	{
		if (!failed)
		{
			BusHelper.getInstance().post(new NewRepostEvent(getPost()));

			PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);

			Notification n = new NotificationCompat.Builder(getContext())
				.setContentIntent(contentIntent)
				.setTicker(getContext().getString(R.string.repost_success))
				.setSmallIcon(R.drawable.notif)
				.setWhen(System.currentTimeMillis())
			.build();

			getManager().notify(getNotificationId(), n);
			getManager().cancel(getNotificationId());
		}

		super.onFinish(failed);
	}
}