package in.controller.handler;

import in.controller.handler.base.StreamResponseHandler;
import in.data.stream.FileStream;

public class FileStreamResponseHandler extends StreamResponseHandler<FileStream>
{
	public FileStreamResponseHandler(boolean append)
	{
		super(append);
	}

	@Override public void onSuccess()
	{
		if (getContent() != null)
		{
			setStream(new FileStream().createFrom(getContent()));
		}
	}
}
