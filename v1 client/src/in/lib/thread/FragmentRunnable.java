package in.lib.thread;

import java.lang.ref.WeakReference;

public abstract class FragmentRunnable<T> implements Runnable
{
	private WeakReference<T> fragment;

	public T getFragment()
	{
		return fragment.get();
	}

	public void setFragment(T fragment)
	{
		this.fragment = new WeakReference<T>(fragment);
	}

	@Override public void run()
	{
		destroy();
	}

	protected void destroy()
	{
		fragment = null;
	}
}