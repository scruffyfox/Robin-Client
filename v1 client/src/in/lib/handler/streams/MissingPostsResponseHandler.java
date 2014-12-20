package in.lib.handler.streams;

import in.lib.Debug;
import in.lib.adapter.PostAdapter;
import in.lib.exception.ExceptionHandler;
import in.lib.handler.base.PostStreamResponseHandler;
import in.lib.thread.StreamFragmentRunnable;
import in.model.Post;
import in.model.base.NetObject;

import java.util.List;

import android.content.Context;
import android.view.View;

public class MissingPostsResponseHandler extends PostStreamResponseHandler
{
	private final NetObject breakObject;

	@Override public void onSend()
	{
		super.onSend();
		Debug.out(getConnectionInfo());
	}

	public MissingPostsResponseHandler(Context c, NetObject obj)
	{
		super(c, false);
		breakObject = obj;
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

	@Override public void onFailure()
	{
		super.onFailure();

		if (getFragment() != null)
		{
			((PostAdapter)getFragment().getAdapter()).resetBreak();
			((PostAdapter)getFragment().getAdapter()).notifyDataSetChanged();
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
					((PostAdapter)getFragment().getAdapter()).setAnimationsEnabled(false);

					View v = getFragment().getListView().getChildAt(getFragment().getListView().getChildCount() - 1);
					int currentPos = getFragment().getListView().getFirstVisiblePosition();
					int bottomPos = getFragment().getListView().getLastVisiblePosition();
					int top = (v == null) ? 0 : v.getTop();

					int breakPos = ((PostAdapter)getFragment().getAdapter()).getBreakPosition();
					int pos = bottomPos;

					if (pos < breakPos)
					{
						pos = currentPos;
					}

					// our saved pos isnt in the refreshed list.
					// switch to append and add a break view
					if (getFragment().getAdapter().getCount() > 0)
					{
						// loop through and see if we've loaded past the old stream id
						boolean allLoaded = false;
						int index = 0;

						for (int i = 0; i < getObjects().size(); i++)
						{
							Post p = (Post)getObjects().get(i);

							if (p.equals(getFragment().getAdapter().getItem(getFragment().getAdapter().getBreakPosition() - 1)))
							{
								allLoaded = true;
								break;
							}

							index++;
						}

						if (!allLoaded && index == getObjects().size())
						{
							index = 0;
						}

						List<NetObject> posts = getObjects().subList(index, getObjects().size());
						int insertPos = getFragment().getAdapter().indexOf(breakObject);

						if (insertPos > -1)
						{
							getFragment().getAdapter().addItems(insertPos, posts);
						}

						if (allLoaded)
						{
							((PostAdapter)getFragment().getAdapter()).setBreakPosition(-2);
							getFragment().getAdapter().setFirstId(((Post)getFragment().getAdapter().getItem(0)).getOriginalId());
						}
						else
						{
							((PostAdapter)getFragment().getAdapter()).setBreakPosition(insertPos - 1);
							((PostAdapter)getFragment().getAdapter()).resetBreak();
						}

						if (insertPos < pos) pos += posts.size();
						((PostAdapter)getFragment().getAdapter()).setLastPositionAnimated(pos + 10);
					}

					getFragment().registerPositionReset(pos, top);
					setFinishedLoading(getAppend());
				}
				catch (Exception e)
				{
					ExceptionHandler.sendException(e);
					e.printStackTrace();
				}
			}

			super.run();
		}
	};
}