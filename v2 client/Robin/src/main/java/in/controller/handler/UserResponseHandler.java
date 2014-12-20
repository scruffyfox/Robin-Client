package in.controller.handler;

import android.app.Fragment;

import com.google.gson.JsonObject;

import in.controller.handler.base.ResponseHandler;
import in.controller.handler.base.ResponseListener;
import in.lib.utils.Debug;
import in.model.User;
import lombok.Getter;
import lombok.Setter;

public class UserResponseHandler extends ResponseHandler
{
	@Getter @Setter private User user;

	@Override public void onSuccess()
	{
		if (getContent() != null)
		{
			JsonObject userObject = getContent().getAsJsonObject().get("data").getAsJsonObject();
			this.user = new User().createFrom(userObject);
		}
	}

	@Override public void onFinish(boolean failed)
	{
		if (failed || !(getFragment() instanceof ResponseListener))
		{
			Debug.out("Response failed");
			Debug.out(getConnectionInfo());
			Debug.out(getContent());
			detachResponse();
		}
		else
		{
			if (getFragment() != null)
			{
				((ResponseListener)getFragment()).handleResponse(getUser());
				detachResponse();
			}
			else
			{
				Debug.out("Waiting for fragment to reattach");
				setOnFragmentAttachedListener(new OnFragmentAttachedListener()
				{
					@Override public void onFragmentAttached(Fragment fragment)
					{
						((ResponseListener)getFragment()).handleResponse(getUser());
						setOnFragmentAttachedListener(null);
						detachResponse();
					}
				});
			}
		}
	}
}
