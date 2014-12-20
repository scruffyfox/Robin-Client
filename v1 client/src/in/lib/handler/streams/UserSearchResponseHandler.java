package in.lib.handler.streams;

import in.lib.handler.base.UserStreamResponseHandler;
import android.content.Context;

public class UserSearchResponseHandler extends UserStreamResponseHandler
{
	public UserSearchResponseHandler(Context c, boolean append)
	{
		super(c, append);
	}

	@Override public void onCallback()
	{
		if (getFragment() != null)
		{
			getFragment().runOnUiThread(responseRunner);
		}

		if (getFragment() != null)
		{
			getFragment().extractUsersAndTags(getObjects());
		}
	}
}