package in.lib.thread;

import in.rob.client.page.base.StreamFragment;

public class StreamFragmentRunnable extends FragmentRunnable<StreamFragment>
{
	@Override public void run()
	{
		super.run();
	}

	/**
	 * Performs common tasks when finishing loading a stream.
	 *
	 * 1. sets if the stream has any more
	 * 2. resets the page index
	 * 3. refreshes the adapter
	 * 4. checks and removes any footers
	 * 5. hides the loading view
	 */
	public void setFinishedLoading(boolean append)
	{
		if (append)
		{
			getFragment().refreshAdapter();
		}
		else
		{
			getFragment().getAdapter().setLastPositionAnimated(getFragment().getAdapter().getLastPositionAnimated() + 10);
			getFragment().postRefreshAdapter();
		}

		getFragment().setLoading(false);
		getFragment().checkAdapterSizes();

		getFragment().writeToCache(getFragment().getAdapter().getStream());
	}
}