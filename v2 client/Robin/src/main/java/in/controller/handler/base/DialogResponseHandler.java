package in.controller.handler.base;

import android.content.Context;

import in.lib.utils.Debug;
import in.model.AdnModel;
import lombok.Getter;
import lombok.Setter;

public abstract class DialogResponseHandler<T extends AdnModel> extends ResponseHandler
{
	@Getter @Setter private T response;
	@Getter private Context context;

	public DialogResponseHandler(Context context)
	{
		this.context = context.getApplicationContext();
	}

	@Override public void onFinish(boolean failed)
	{
		if (context != null)
		{
			Debug.out("sent");
			Debug.out(getResponse());
		}
		else
		{
			Debug.out("fragment is null in " + this);
		}

		if (failed)
		{
			Debug.out("Response failed");
			Debug.out(getConnectionInfo());
			Debug.out(getContent());
		}
	}
}
