package in.lib.manager;

import android.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import in.controller.handler.base.ResponseHandler;

/**
 * Used to manage all async http response handlers in fragments. Each API call
 * should have its own key set in Constants.class prefixed "RESPONSE_"
 *
 * @author callumtaylor
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ResponseManager
{
	public final HashMap<String, WeakReference<ResponseHandler>> responses = new HashMap<String, WeakReference<ResponseHandler>>();
	private static ResponseManager instance;

	public static ResponseManager getInstance()
	{
		if (instance == null)
		{
			synchronized (ResponseManager.class)
			{
				if (instance == null)
				{
					instance = new ResponseManager();
				}
			}
		}

		return instance;
	}

	public ResponseHandler getResponse(String key)
	{
		return responses.get(key) == null ? null : responses.get(key).get();
	}

	public void addResponse(String key, ResponseHandler response, Fragment fragment)
	{
		detach(key);
		responses.put(key, new WeakReference(response));
		response.setResponseKey(key);

		if (fragment != null)
		{
			response.attach(fragment);
		}
	}

	public boolean attach(String key, Fragment fragment)
	{
		if (responses.containsKey(key) && responses.get(key) != null && responses.get(key).get() != null)
		{
			responses.get(key).get().attach(fragment);
			return true;
		}

		return false;
	}

	public boolean detach(String key)
	{
		if (responses.containsKey(key) && responses.get(key) != null && responses.get(key).get() != null)
		{
			responses.get(key).get().detach();
			return true;
		}

		return false;
	}

	public void removeResponse(String key)
	{
		detach(key);
		responses.remove(key);
	}
}