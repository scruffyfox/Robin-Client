package in.lib.handler.base;

import in.lib.Debug;
import lombok.Getter;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import com.google.gson.JsonObject;

/**
 * Standard response handler for logging in a user
 */
public abstract class LoginResponseHandler extends JsonResponseHandler
{
	@Getter private String accessToken;
	@Getter private int userId;

	@Override public void onSuccess()
	{
		try
		{
			JsonObject returnedData = getContent().getAsJsonObject();
			accessToken = returnedData.get("access_token").getAsString();
			userId = returnedData.get("user_id").getAsInt();
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		onCallback();
	}

	/**
	 * Implement this callback
	 */
	public abstract void onCallback();
}
