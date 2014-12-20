package in.controller.handler;

import in.controller.handler.base.StreamResponseHandler;
import in.data.stream.PostStream;
import in.lib.writer.AutoCompleteWriter;

public class PostStreamResponseHandler extends StreamResponseHandler<PostStream>
{
	public PostStreamResponseHandler(boolean append)
	{
		super(append);
	}

	@Override public void onSuccess()
	{
		if (getContent() != null)
		{
			setStream(new PostStream().createFrom(getContent()));

			// write usernames autocomplete
			AutoCompleteWriter writer = new AutoCompleteWriter();
			writer.writeUsernames(getStream().getItems());
			writer.writeHashtags(getStream().getItems());
		}
	}
}
