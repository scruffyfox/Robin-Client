package in.controller.handler;

import in.controller.handler.base.StreamResponseHandler;
import in.data.stream.InteractionStream;

public class InteractionStreamResponseHandler extends StreamResponseHandler<InteractionStream>
{
	public InteractionStreamResponseHandler(boolean append)
	{
		super(append);
	}

	@Override public void onSuccess()
	{
		if (getContent() != null)
		{
			setStream(new InteractionStream().createFrom(getContent()));
		}
	}
}
