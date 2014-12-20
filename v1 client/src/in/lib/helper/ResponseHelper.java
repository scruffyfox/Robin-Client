package in.lib.helper;

import in.lib.handler.base.RobinResponseHandler;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.support.v4.app.Fragment;

/**
 * Used to manage all async http response handlers in fragments. Each API call
 * should have its own key set in Constants.class prefixed "RESPONSE_"
 *
 * @author callumtaylor
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ResponseHelper
{
	public final HashMap<String, WeakReference<RobinResponseHandler>> responses = new HashMap<String, WeakReference<RobinResponseHandler>>();
	private static ResponseHelper instance;

	public static ResponseHelper getInstance()
	{
		if (instance == null)
		{
			instance = new ResponseHelper();
		}

		System.gc();
		return instance;
	}

	public RobinResponseHandler getResponse(String key)
	{
		return responses.get(key) == null ? null : responses.get(key).get();
	}

	public void addResponse(String key, RobinResponseHandler response, Fragment fragment)
	{
		detach(key);
		responses.put(key, new WeakReference(response));
		response.setResponseKey(key);

		if (fragment != null)
		{
			response.attach(fragment);
		}
	}

	public boolean reattach(String key, Fragment fragment)
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
		System.gc();
	}
}