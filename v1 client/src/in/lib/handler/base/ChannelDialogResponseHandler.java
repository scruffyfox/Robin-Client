package in.lib.handler.base;

import in.lib.Debug;
import in.lib.event.NewChannelEvent;
import in.lib.helper.BusHelper;
import in.model.Channel;
import in.rob.client.R;
import lombok.Getter;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Standard response handler for Dialog posting. Handles broadcasts on callback.
 */
public class ChannelDialogResponseHandler extends JsonResponseHandler
{
	@Getter private Channel channel;
	protected Intent mFailIntent;
	protected CharSequence contentTitle;
	protected Context mContext;
	protected int mNotificationId;

	public ChannelDialogResponseHandler(Context context)
	{
		this(context, null, -1);
	}

	public ChannelDialogResponseHandler(Context context, Intent failIntent, int sendNotificationId)
	{
		mContext = context;
		mFailIntent = failIntent;
		mNotificationId = sendNotificationId;
		contentTitle = context.getString(R.string.send_message_fail);
	}

	@Override public void onSuccess()
	{
		try
		{
			NotificationManager manager = ((NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE));
			manager.cancel(mNotificationId);

			JsonElement elements = getContent();
			JsonObject jChannel = elements.getAsJsonObject().get("data").getAsJsonObject();
			channel = new Channel().createFrom(jChannel);
			mContext = null;
		}
		catch (Exception e)
		{
			Debug.out(e);
			onFailure();
		}
	}

	@Override public void onFinish(boolean failed)
	{
		if (!failed)
		{
			BusHelper.getInstance().post(new NewChannelEvent(getChannel()));
		}
	}

	@Override public void onFailure()
	{
		NotificationManager manager = ((NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE));
		Notification notification = new Notification(R.drawable.notif, mContext.getString(R.string.vague_error), System.currentTimeMillis());

		manager.cancel(mNotificationId);

		if (mFailIntent == null) return;

		CharSequence contentText = mContext.getString(R.string.tap_to_retry);

		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 1, mFailIntent, 0);
		notification.flags |= Notification.FLAG_HIGH_PRIORITY;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
		manager.notify(1, notification);

		return;
	}
}