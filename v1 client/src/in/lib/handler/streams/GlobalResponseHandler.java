package in.lib.handler.streams;

import in.rob.client.R;
import android.content.Context;

public class GlobalResponseHandler extends TimelineResponseHandler
{
	public GlobalResponseHandler(Context c, boolean append)
	{
		super(c, append, 0);
		setFailMessage(c.getString(R.string.global_stream_fail));
	}
}