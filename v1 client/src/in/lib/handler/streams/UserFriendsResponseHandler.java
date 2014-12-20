package in.lib.handler.streams;

import in.lib.exception.ExceptionHandler;
import in.lib.handler.base.UserStreamResponseHandler;
import in.lib.thread.StreamFragmentRunnable;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

public class UserFriendsResponseHandler extends UserStreamResponseHandler
{
	public UserFriendsResponseHandler(Context c, boolean append)
	{
		super(c, append);
	}

	@Override public void onCallback()
	{
		if (getFragment() != null)
		{
			getFragment().runOnUiThread(responseRunner);
		}

		if (getFragment() != null)
		{
			getFragment().extractUsersAndTags(getObjects());
		}
	}

	private StreamFragmentRunnable responseRunner = new StreamFragmentRunnable()
	{
		@Override public void run()
		{
			if (getFragment() != null)
			{
				try
				{
					getFragment().getRefreshHelper().finish();
					getFragment().getHeadedListView().setBlockLayoutChildren(true);

					if (!TextUtils.isEmpty(getLastId()))
					{
						getFragment().getAdapter().setLastId(getLastId());
					}

					if (getAppend())
					{
						getFragment().getAdapter().addItems(getObjects());
					}
					else
					{
						getFragment().getAdapter().setFirstId(getFirstId());
						getFragment().getAdapter().setItems(getObjects());
					}

					getFragment().getAdapter().setHasMore(getHasMore());
					getFragment().getAdapter().setStreamMarker(getMarker());
					setFinishedLoading(getAppend());

					if (!getHasMore() && getFragment().getLoadMoreView() != null)
					{
						getFragment().getLoadMoreView().setVisibility(View.INVISIBLE);
					}
				}
				catch (Exception e)
				{
					ExceptionHandler.sendException(e);
				}
			}

			super.run();
		}
	};
}