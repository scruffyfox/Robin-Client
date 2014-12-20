package in.lib.handler.streams;

import in.lib.adapter.PostAdapter;
import in.lib.adapter.base.RobinAdapter.Order;
import in.lib.exception.ExceptionHandler;
import in.lib.handler.base.PostStreamResponseHandler;
import in.lib.thread.StreamFragmentRunnable;
import in.model.Post;
import in.rob.client.R;
import in.rob.client.page.ThreadPage;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

public class ThreadResponseHandler extends PostStreamResponseHandler
{
	private String currentPostId = "";

	public ThreadResponseHandler(Context c)
	{
		super(c, false);
		setFailMessage(c.getString(R.string.thread_stream_fail));
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
		@Override public ThreadPage getFragment()
		{
			return (ThreadPage)super.getFragment();
		}

		@Override public void run()
		{
			if (getFragment() != null)
			{
				try
				{
					getFragment().getRefreshHelper().finish();
					getFragment().getHeadedListView().setBlockLayoutChildren(true);

					boolean initial = getFragment().getAdapter().getCount() <= 1;
					int pos = getFragment().getListView().getFirstVisiblePosition();
					((PostAdapter)getFragment().getAdapter()).setAnimationsEnabled(false);
					currentPostId = getFragment().getAdapter().getItem(pos) != null ? getFragment().getAdapter().getItem(pos).getId() : "";

					if (TextUtils.isEmpty(currentPostId))
					{
						if (getFragment().getCenterPost() != null)
						{
							currentPostId = getFragment().getCenterPost().getId();
						}
						else if (!TextUtils.isEmpty(getFragment().getThreadId()))
						{
							currentPostId = getFragment().getThreadId();
						}
					}

					// get the selected post's position so we can reposition the list
					// after loading the extra replies. We have to go backwards because
					// we are eventually reversing the list
					int postCount = getObjects().size();
					for (int index = postCount - 1, arrayIndex = 0; index > -1; index--, arrayIndex++)
					{
						Post p = (Post)getObjects().get(index);
						p.setMention(false);

						if (getFragment().getCenterPost() != null && p.getId().equals(getFragment().getCenterPost().getId()))
						{
							getFragment().setCenterPost(p);
						}

						if (p.getId().equals(currentPostId))
						{
							pos = arrayIndex + (initial ? getFragment().getListView().getHeaderViewsCount() : 0);
						}
					}

					if (((PostAdapter)getFragment().getAdapter()).getCenter() == null && getFragment().getCenterPost() == null)
					{
						((PostAdapter)getFragment().getAdapter()).setCenter((Post)getObjects().get(getObjects().size() - 1));
					}
					else
					{
						((PostAdapter)getFragment().getAdapter()).setCenter(getFragment().getCenterPost());
					}

					View v = getFragment().getListView().getChildAt(0);
					int top = (v == null) ? 0 : v.getTop();

					getFragment().getAdapter().setOrder(Order.ASC);
					getFragment().getAdapter().setItems(getObjects());
					((PostAdapter)getFragment().getAdapter()).setReady(true);
					getFragment().registerPositionReset(pos, top);

					getFragment().getAdapter().setHasMore(getHasMore());
					getFragment().getAdapter().setStreamMarker(getMarker());
					setFinishedLoading(false);
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