package in.controller.handler.base;

import android.app.Fragment;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import in.lib.manager.ResponseManager;
import lombok.Getter;
import lombok.Setter;

public abstract class ResponseHandler extends JsonResponseHandler
{
	public static interface OnFragmentAttachedListener
	{
		public void onFragmentAttached(Fragment fragment);
	}

	@Getter @Setter private OnFragmentAttachedListener onFragmentAttachedListener;
	@Getter @Setter private String responseKey;
	@Getter private Fragment fragment;

	@Override public void generateContent()
	{
		try
		{
			super.generateContent();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void attach(Fragment fragment)
	{
		this.fragment = fragment;

		if (onFragmentAttachedListener != null)
		{
			onFragmentAttachedListener.onFragmentAttached(fragment);
		}
	}

	public void detach()
	{
		this.fragment = null;
	}

	public void detachResponse()
	{
		ResponseManager.getInstance().removeResponse(responseKey);
	}
}
