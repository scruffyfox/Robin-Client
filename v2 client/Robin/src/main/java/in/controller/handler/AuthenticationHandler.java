package in.controller.handler;

import android.app.Fragment;

import com.google.gson.JsonObject;

import in.controller.handler.base.ResponseHandler;
import in.controller.handler.base.ResponseListener;
import in.lib.utils.Debug;
import in.model.User;
import in.rob.client.fragment.AuthenticationFragment;
import lombok.Getter;

public class AuthenticationHandler extends ResponseHandler
{
	@Getter private User user;

	@Override public AuthenticationFragment getFragment()
	{
		return (AuthenticationFragment)super.getFragment();
	}

	@Override public void onSuccess()
	{
		if (getContent() != null)
		{
			JsonObject tokenObject = getContent().getAsJsonObject().get("token").getAsJsonObject();
			JsonObject userObject = tokenObject.get("user").getAsJsonObject();
			this.user = new User().createFrom(userObject);
			this.user.setToken(getContent().getAsJsonObject().get("access_token").getAsString());
		}
	}

	@Override public void onFinish(boolean failed)
	{
		if (failed)
		{
			Debug.out("Response failed");
			Debug.out(getConnectionInfo());
			Debug.out(getContent());

			if (getFragment() != null)
			{
				JsonObject errorObject = getContent().getAsJsonObject();
				getFragment().handleFailure(errorObject.get("error_slug").getAsString(), errorObject.get("error").getAsString());
				detachResponse();
			}
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
