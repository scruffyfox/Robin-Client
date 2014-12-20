package in.lib.handler.streams;

import in.lib.handler.base.ChannelStreamResponseHandler;
import in.rob.client.R;
import android.content.Context;

public class ChannelResponseHandler extends ChannelStreamResponseHandler
{
	public ChannelResponseHandler(Context c, boolean append)
	{
		super(c, append);
		setFailMessage(c.getString(R.string.channels_stream_fail));
	}

	@Override public void onCallback()
	{
		if (getFragment() != null)
		{
			getFragment().runOnUiThread(responseRunner);
		}
	}
}