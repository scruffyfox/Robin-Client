package in.controller.handler.base;

import in.data.stream.base.Stream;

public interface StreamResponseListener
{
	public void handleResponse(Stream stream, boolean append);
}
