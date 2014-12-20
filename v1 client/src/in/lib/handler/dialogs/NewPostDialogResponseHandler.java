package in.lib.handler.dialogs;

import in.lib.Constants;
import in.lib.event.DeletePostDraftEvent;
import in.lib.event.NewPostDraftEvent;
import in.lib.event.NewPostEvent;
import in.lib.handler.base.DialogResponseHandler;
import in.lib.helper.BusHelper;
import in.lib.manager.CacheManager;
import in.model.DraftPost;
import in.rob.client.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class NewPostDialogResponseHandler extends DialogResponseHandler
{
	public NewPostDialogResponseHandler(Context context, Intent failIntent, int sendNotificationId)
	{
		super(context, failIntent, sendNotificationId);
	}

	@Override public String getFailText()
	{
		return getContext().getString(R.string.post_fail);
	}

	@Override public void onFinish(boolean failed)
	{
		if (!failed)
		{
			BusHelper.getInstance().post(new NewPostEvent(getPost()));

			if (getFailIntent().hasExtra(Constants.EXTRA_NEW_POST_DRAFT))
			{
				DraftPost post = DraftPost.deserialize(getFailIntent().getByteArrayExtra(Constants.EXTRA_NEW_POST_DRAFT));
				BusHelper.getInstance().post(new DeletePostDraftEvent(post));
				CacheManager.getInstance().removeFile(String.format(Constants.CACHE_DRAFT_POST, post.getSelectedAccountId(), post.getDate()));
			}

			PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);

			Notification n = new NotificationCompat.Builder(getContext())
				.setContentIntent(contentIntent)
				.setTicker(getContext().getString(R.string.post_success))
				.setSmallIcon(R.drawable.notif)
				.setWhen(System.currentTimeMillis())
			.build();

			getManager().notify(getNotificationId(), n);
			getManager().cancel(getNotificationId());
		}
		else
		{
			if (getFailIntent().hasExtra(Constants.EXTRA_NEW_POST_DRAFT))
			{
				DraftPost post = DraftPost.deserialize(getFailIntent().getByteArrayExtra(Constants.EXTRA_NEW_POST_DRAFT));
				BusHelper.getInstance().post(new NewPostDraftEvent(post));
			}
		}

		super.onFinish(failed);
	}
}