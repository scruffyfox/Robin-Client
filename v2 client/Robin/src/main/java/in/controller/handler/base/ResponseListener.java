package in.controller.handler.base;

public interface ResponseListener<T>
{
	public void handleResponse(T response);
}
