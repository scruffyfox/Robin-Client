package in.lib.handler.streams;

import in.lib.handler.base.PostStreamResponseHandler;
import android.content.Context;

public class StarredResponseHandler extends PostStreamResponseHandler
{
	public StarredResponseHandler(Context c, boolean append)
	{
		super(c, append);
	}

	@Override public void onCallback()
	{
		if (getFragment() != null)
		{
			getFragment().runOnUiThread(responseRunner);
		}
	}
}