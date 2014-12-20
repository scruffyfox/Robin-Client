package in.rob.client.fragment.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import in.lib.manager.ResponseManager;
import lombok.Getter;

public class BaseFragment extends Fragment
{
	@Getter private Context context;

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}

	@Override public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		context = activity;
	}

	@Override public void onDetach()
	{
		super.onDetach();

		detachResponses();
	}

	public void attachResponses()
	{
		String[] keys = getResponseKeys();
		if (keys != null)
		{
			for (String key : keys)
			{
				ResponseManager.getInstance().attach(key, this);
			}
		}
	}

	public void detachResponses()
	{
		String[] keys = getResponseKeys();
		if (keys != null)
		{
			for (String key : keys)
			{
				ResponseManager.getInstance().detach(key);
			}
		}
	}

	public String[] getResponseKeys()
	{
		return null;
	}
}
