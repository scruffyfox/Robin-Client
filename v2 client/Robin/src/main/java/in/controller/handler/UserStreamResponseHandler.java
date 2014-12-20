package in.controller.handler;

import in.controller.handler.base.StreamResponseHandler;
import in.data.stream.UserStream;
import in.lib.writer.AutoCompleteWriter;

public class UserStreamResponseHandler extends StreamResponseHandler<UserStream>
{
	public UserStreamResponseHandler(boolean append)
	{
		super(append);
	}

	@Override public void onSuccess()
	{
		if (getContent() != null)
		{
			setStream(new UserStream().createFrom(getContent()));

			// write usernames autocomplete
			AutoCompleteWriter writer = new AutoCompleteWriter();
			writer.writeUsernames(getStream().getItems());
			writer.writeHashtags(getStream().getItems());
		}
	}
}
