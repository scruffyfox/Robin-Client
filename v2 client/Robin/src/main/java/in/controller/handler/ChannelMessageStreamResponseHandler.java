package in.controller.handler;

import in.controller.handler.base.StreamResponseHandler;
import in.data.stream.ChannelMessageStream;
import in.lib.writer.AutoCompleteWriter;

public class ChannelMessageStreamResponseHandler extends StreamResponseHandler<ChannelMessageStream>
{
	public ChannelMessageStreamResponseHandler(boolean append)
	{
		super(append);
	}

	@Override public void onSuccess()
	{
		if (getContent() != null)
		{
			setStream(new ChannelMessageStream().createFrom(getContent()));

			// write usernames autocomplete
			new AutoCompleteWriter().writeUsernames(getStream().getItems());
		}
	}
}
