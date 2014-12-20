package in.lib.handler.streams;

import in.lib.handler.base.UserStreamResponseHandler;
import android.content.Context;

public class MutedResponseHandler extends UserStreamResponseHandler
{
	public MutedResponseHandler(Context c, boolean append)
	{
		super(c, append);
	}

	@Override public void onCallback()
	{
		if (getFragment() != null)
		{
			getFragment().runWhenReady(responseRunner);
		}
	}
}