package in.controller.handler;

import in.controller.handler.base.StreamResponseHandler;
import in.data.stream.ChannelStream;

public class ChannelStreamResponseHandler extends StreamResponseHandler<ChannelStream>
{
	public ChannelStreamResponseHandler(boolean append)
	{
		super(append);
	}

	@Override public void onSuccess()
	{
		if (getContent() != null)
		{
			setStream(new ChannelStream().createFrom(getContent()));
		}
	}
}
