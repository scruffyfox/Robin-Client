package in.lib.handler.streams;

import in.lib.handler.base.UserStreamResponseHandler;
import android.content.Context;

public class PostRepostResponseHandler extends UserStreamResponseHandler
{
	public PostRepostResponseHandler(Context c, boolean append)
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