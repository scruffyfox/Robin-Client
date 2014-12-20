package in.lib.handler.streams;

import in.lib.exception.ExceptionHandler;
import in.lib.handler.base.PostStreamResponseHandler;
import in.lib.thread.StreamFragmentRunnable;
import in.model.base.Message;
import in.model.base.NetObject;
import in.rob.client.R;
import android.content.Context;
import android.text.TextUtils;

public class MentionsResponseHandler extends PostStreamResponseHandler
{
	public MentionsResponseHandler(Context c, boolean append)
	{
		super(c, append);
		setFailMessage(c.getString(R.string.mentions_stream_fail));
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

					int newPostCount = 0;
					boolean foundStart = false;

					String firstId = getFragment().getCurrentItemId(0);
					boolean tryNewCount = true;

					try
					{
						long firstIdLong = Long.valueOf(firstId);
						long firstNewIdLong = Long.valueOf(getFirstId());

						if (firstNewIdLong <= firstIdLong)
						{
							tryNewCount = false;
						}
					}
					catch (Exception e){}

					if (getObjects() != null)
					{
						for (NetObject p : getObjects())
						{
							((Message)p).setMention(true);

							if (tryNewCount)
							{
								if (p.getId().equals(firstId))
								{
									foundStart = true;
								}

								if (!foundStart)
								{
									newPostCount++;
								}
							}
						}
					}

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
						setNewPostCount(newPostCount);
						getFragment().getAdapter().setFirstId(getFirstId());
						getFragment().getAdapter().setItems(getObjects());
					}

					getFragment().getAdapter().setHasMore(getHasMore());
					getFragment().getAdapter().setStreamMarker(getMarker());
					setFinishedLoading(getAppend());
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