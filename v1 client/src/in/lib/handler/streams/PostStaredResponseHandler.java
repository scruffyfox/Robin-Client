package in.lib.handler.streams;

import in.lib.handler.base.UserStreamResponseHandler;
import android.content.Context;

public class PostStaredResponseHandler extends UserStreamResponseHandler
{
	public PostStaredResponseHandler(Context c, boolean append)
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