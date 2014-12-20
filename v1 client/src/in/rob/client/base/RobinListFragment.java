package in.rob.client.base;

import in.lib.helper.BusHelper;
import in.lib.thread.FragmentRunnable;
import in.rob.client.MainApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import net.callumtaylor.swipetorefresh.helper.RefreshHelper;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.MenuItem;

public class RobinListFragment extends ListFragment
{
	/**
	 * Application instance context. Use this for the most part
	 */
	@Getter private Context context;
	@Getter private Context applicationContext;
	@Getter protected RefreshHelper refreshHelper;

	@Getter private boolean ready = false;
	private List<Runnable> pending = Collections.synchronizedList(new ArrayList<Runnable>());

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.context = getActivity();
		this.applicationContext = getActivity().getApplicationContext();

		BusHelper.getInstance().register(this);
	}

	@Override public void onDestroyView()
	{
		super.onDestroyView();

		synchronized (pending)
		{
			ready = false;
		}
	}

	@Override public void onDestroy()
	{
		if (refreshHelper != null)
		{
			refreshHelper.onReset();
		}

		super.onDestroy();
		BusHelper.getInstance().unregister(this);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}

	public void checkPendingThenExecute()
	{
		synchronized (pending)
		{
			//if (!ready)
			{
				if (getListView() != null)
				{
					ready = true;
					executePending();
				}
				else
				{
					throw new IllegalAccessError("Failed to resume any runnables because listView is null");
				}
			}
		}
	}

	@Override public void onDetach()
	{
		super.onDetach();

		synchronized (pending)
		{
			ready = false;
		}
	}

	public void executePending()
	{
		int pendingCallbacks = pending.size();
		while (pendingCallbacks-- > 0)
		{
			runOnUiThread(pending.remove(0));
		}
	}

	public void runWhenReady(Runnable runnable)
	{
		runOnUiThread(runnable);
	}

	public void runOnUiThread(Runnable runnable)
	{
		//synchronized (pending)
		{
			if (!ready)
			{
				pending.add(runnable);
			}
			else
			{
				//synchronized (pending)
				{
					if (getActivity() != null)
					{
						if (runnable instanceof FragmentRunnable)
						{
							((FragmentRunnable)runnable).setFragment(this);
						}

						getActivity().runOnUiThread(runnable);
					}
					else
					{
						pending.add(runnable);
					}
				}
			}
		}
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		return false;
	}

	/**
	 * @return Gets the base 64 rehashable id of the device
	 */
	public String getDeviceId()
	{
		return ((MainApplication)getActivity().getApplication()).getDeviceId();
	}
}