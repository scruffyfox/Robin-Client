package in.lib.handler.base;

import in.lib.Debug;
import in.lib.exception.ExceptionHandler;
import in.model.PrivateMessage;
import in.rob.client.MainApplication;
import in.rob.client.MainApplication.ApplicationType;
import in.rob.client.R;
import lombok.Getter;
import android.content.Context;
import android.content.Intent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Standard response handler for Dialog posting. Handles broadcasts on callback.
 */
public class PrivateMessageDialogResponseHandler extends DialogResponseHandler
{
	@Getter private PrivateMessage message;

	public PrivateMessageDialogResponseHandler(Context context, Intent failIntent, int sendNotificationId)
	{
		super(context, failIntent, sendNotificationId);
		setContentTitle(context.getString(R.string.send_message_fail));
	}

	@Override public void onSuccess()
	{
		try
		{
			JsonElement elements = getContent();
			JsonObject post = elements.getAsJsonObject().get("data").getAsJsonObject();
			message = new PrivateMessage().createFrom(post, true);
		}
		catch (Exception e)
		{
			if (((MainApplication)getContext().getApplicationContext()).getApplicationType() == ApplicationType.BETA)
			{
				ExceptionHandler.sendException(e);
			}

			Debug.out(e);
			onFailure();
		}
	}

	@Override public String getFailText()
	{
		return getContext().getString(R.string.vague_error);
	}
}