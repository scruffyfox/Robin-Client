package in.lib.handler.streams;

import in.lib.adapter.PostAdapter;
import in.lib.exception.ExceptionHandler;
import in.lib.handler.base.PostStreamResponseHandler;
import in.lib.manager.SettingsManager;
import in.lib.thread.StreamFragmentRunnable;
import in.model.base.NetObject;
import in.obj.StreamMarker;
import in.rob.client.R;
import in.rob.client.page.TimelinePage;
import android.content.Context;

public class TimelineResponseHandler extends PostStreamResponseHandler
{
	private final int forceRefreshCount;

	public TimelineResponseHandler(Context c, boolean append, int forceRefreshCount)
	{
		super(c, append);
		this.forceRefreshCount = forceRefreshCount;
		setFailMessage(c.getString(R.string.timeline_stream_fail));
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
					StreamMarker currentMarker = getFragment().getAdapter().getStreamMarker();
					getFragment().getRefreshHelper().finish();
					getFragment().getHeadedListView().setBlockLayoutChildren(true);
					getFragment().getAdapter().setStreamMarker(getMarker());
					getFragment().getAdapter().setHasMore(getHasMore());

					if (getAppend())
					{
						getFragment().getAdapter().setLastId(getLastId());
						getFragment().getAdapter().addItems(getObjects());
					}
					else
					{
						if (SettingsManager.isStreamMarkerEnabled())
						{
							boolean syncMarker = true;
							String lastReadId = getMarker().getId();

							if (currentMarker != null)
							{
								if (!SettingsManager.isStreamMarkerPastEnabled())
								{
									try
									{
										long markerId = Long.parseLong(currentMarker.getId());
										long postId = Long.parseLong(lastReadId);

										if (postId < markerId)
										{
											syncMarker = false;
										}
									}
									catch (Exception e){}
								}
							}

							if (syncMarker)
							{
								int[] d = getFragment().getFirstViewPosition(getObjects());
								boolean brokenStream = d[2] == 1;

								if (getFragment().getAdapter().getCount() > 0 && brokenStream)
								{
									((PostAdapter)getFragment().getAdapter()).setBreakPosition(getObjects().size() - 1);
									int[] d2 = getFragment().getLastViewPosition(null);
									d2[0] += getObjects().size();

									getFragment().getAdapter().setFirstId(getFirstId());
									getFragment().getAdapter().prependItems(getObjects());
									getFragment().getAdapter().setLastPositionAnimated(getFragment().getAdapter().getLastPositionAnimated() + 20);

									setNewPostCount(getObjects().size());
								}
								else
								{
									int[] d2 = getFragment().getLastViewPosition(getObjects());
									//calculateNewPostCount();

									getFragment().getAdapter().setLastId(getLastId());
									getFragment().getAdapter().setFirstId(getFirstId());
									getFragment().getAdapter().setItems(getObjects());
									((PostAdapter)getFragment().getAdapter()).setBreakPosition(-2);
								}

								int pos = getFragment().getAdapter().indexOf(getFragment().getAdapter().getItemById(lastReadId));
								pos += getFragment().getListView().getHeaderViewsCount();
								getFragment().registerPositionReset(pos, 0);
							}
						}
						else
						{
							int[] d = getFragment().getFirstViewPosition(getObjects());
							boolean brokenStream = d[2] == 1;

							if (getFragment().getAdapter().getCount() > 0 && !getAppend() && brokenStream && forceRefreshCount < 1 && SettingsManager.isTimelineBreakEnabled())
							{
								((PostAdapter)getFragment().getAdapter()).setBreakPosition(getObjects().size() - 1);
								int[] d2 = getFragment().getLastViewPosition(null);
								d2[0] += getObjects().size();

								getFragment().getAdapter().setFirstId(getFirstId());
								getFragment().getAdapter().prependItems(getObjects());
								getFragment().getAdapter().setLastPositionAnimated(getFragment().getAdapter().getLastPositionAnimated() + 20);
								getFragment().registerPositionReset(d2[0], d2[1]);

								setNewPostCount(getObjects().size());
							}
							else
							{
								int[] d2 = getFragment().getLastViewPosition(getObjects());
								calculateNewPostCount();

								getFragment().getAdapter().setLastId(getLastId());
								getFragment().getAdapter().setFirstId(getFirstId());
								getFragment().getAdapter().setItems(getObjects());
								((PostAdapter)getFragment().getAdapter()).setBreakPosition(-2);
								getFragment().registerPositionReset(d2[0], d2[1]);
							}
						}
					}

					if (!getAppend())
					{
						getFragment().setTicker(getNewPostCount());
					}

					setFinishedLoading(getAppend());

					if (getFragment() instanceof TimelinePage)
					{
						((TimelinePage)getFragment()).setLastRefreshed(System.currentTimeMillis());
					}
				}
				catch (Exception e)
				{
					ExceptionHandler.sendException(e);
				}
			}

			super.run();
		}

		private void calculateNewPostCount()
		{
			int newPostCount = 0;
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

			if (tryNewCount)
			{
				for (NetObject p : getObjects())
				{
					if (p.getId().equals(firstId))
					{
						break;
					}

					newPostCount++;
				}

				setNewPostCount(newPostCount);
			}
		}
	};
}