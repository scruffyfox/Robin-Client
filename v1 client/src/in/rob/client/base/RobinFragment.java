package in.rob.client.base;

import in.rob.client.MainApplication;
import lombok.Getter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

public class RobinFragment extends Fragment
{
	/**
	 * Application instance context. Use this for the most part
	 */
	@Getter private Context context;
	@Getter private Context applicationContext;

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.context = getActivity();
		this.applicationContext = getActivity().getApplicationContext();
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}

	public void runDelayed(final Runnable runnable, long delay)
	{
		new Handler().postDelayed(new Runnable()
		{
			@Override public void run()
			{
				runWhenReady(runnable);
			}
		}, delay);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		return false;
	}

	public void runWhenReady(Runnable runnable)
	{
		runNow(runnable);
	}

	public void runNow(Runnable runnable)
	{
		getActivity().runOnUiThread(runnable);
	}

	/**
	 * @return Gets the base 64 rehashable id of the device
	 */
	public String getDeviceId()
	{
		return ((MainApplication)getActivity().getApplication()).getDeviceId();
	}
}