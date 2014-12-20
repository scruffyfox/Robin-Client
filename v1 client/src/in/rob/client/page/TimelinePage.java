package in.rob.client.page;

import in.lib.Constants;
import in.lib.event.DeletePostEvent;
import in.lib.event.NewPostEvent;
import in.lib.event.ProfileUpdatedEvent;
import in.lib.handler.streams.MissingPostsResponseHandler;
import in.lib.handler.streams.TimelineResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.model.Stream;
import in.model.base.NetObject;
import in.obj.StreamMarker;
import in.rob.client.R;
import in.rob.client.page.base.PostStreamFragment;
import in.rob.client.widget.ScrollWidgetProvider;

import java.util.Date;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.squareup.otto.Subscribe;

/**
 * Fragment used for displaying posts in a fragment list
 * @author callumtaylor
 */
public class TimelinePage extends PostStreamFragment
{
	// aux variables
	private String mCoverUrl = "";
	private long mLastRefreshed = 0;
	private final int forceRefreshCount = 0;

	// views
	private TextView mLastRefreshedTv;

	@Override public void onDataReady()
	{
		SharedPreferences prefs = getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
		int topPos = prefs.getInt(String.format(Constants.PREFS_TIMELINE_TOP_POSITION, UserManager.getUserId()), 0);
		int topPosY = prefs.getInt(String.format(Constants.PREFS_TIMELINE_TOP_POSITION_Y, UserManager.getUserId()), 0);
		getListView().setSelectionFromTop(topPos, topPosY);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		if (SettingsManager.getShowTimelineCover())
		{
			//if (getAdapter() == null)
			{
				getHeadedListView().setHeaderView(R.layout.timeline_header_view);
				mLastRefreshedTv = (TextView)getHeadedListView().findViewById(R.id.last_refreshed);
				mLastRefreshed = getCacheManager().fileModifiedDate(getCacheFileName());

				if (mLastRefreshed > 0)
				{
					setLastRefreshed();
				}

				if (UserManager.getUser() != null)
				{
					mCoverUrl = UserManager.getUser().getCoverUrl();

					if (!UserManager.getUser().isCoverDefault())
					{
						setHeaderUrl(mCoverUrl);
					}
				}
			}
		}
	}

	@Override public void onResume()
	{
		super.onResume();

		if (!isLoading()
		&& SettingsManager.getCacheTimeout() > 0
		&& getCacheManager().fileOlderThan(getCacheFileName(), System.currentTimeMillis() - SettingsManager.getCacheTimeout())
		&& getAdapter().getBreakPosition() < 0)
		{
			loadFromApi();
		}
	}

	@Override public void onForceRefresh()
	{
		if (getListView().getLastVisiblePosition() > SettingsManager.getPageSize()) return;
		super.onForceRefresh();
	}

	/**
	 * Replacement for beginLoadFromApi() to allow us to call it
	 * from onResume(). Removes any chance of load duplication
	 */
	public void loadFromApi()
	{
		onForceRefresh();
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == Constants.REQUEST_SETTINGS)
		{
			if (resultCode == Constants.RESULT_REFRESH)
			{
				if (data.getExtras().containsKey(Constants.EXTRA_REFRESH_TIMELINE))
				{
					onForceRefresh();
					return;
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override public void loadMissingItems(final NetObject o)
	{
		showProgressLoader();

		MissingPostsResponseHandler handler = new MissingPostsResponseHandler(getApplicationContext(), o);
		ResponseHelper.getInstance().addResponse(getResponseKeys()[1], handler, this);

		if (SettingsManager.isUsingUnified())
		{
			APIManager.getInstance().getMissingUnifiedTimeLine(o.getId(), -30, handler);
		}
		else
		{
			APIManager.getInstance().getMissingTimeLine(o.getId(), -30, handler);
		}
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		showProgressLoader();

		TimelineResponseHandler handler = new TimelineResponseHandler(getApplicationContext(), append, forceRefreshCount);
		ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);

		if (SettingsManager.isUsingUnified())
		{
			APIManager.getInstance().getUnifiedTimeLine(lastId, handler);
		}
		else
		{
			APIManager.getInstance().getTimeLine(lastId, handler);
		}
	}

	/**
	 * Sets the last refreshed time in the cover header
	 */
	public void setLastRefreshed(long time)
	{
		mLastRefreshed = time;
		setLastRefreshed();
	}

	/**
	 * Sets the last refreshed time in the cover header
	 */
	public void setLastRefreshed()
	{
		String date = SettingsManager.getDateFormat().format(new Date(mLastRefreshed));
		String time = SettingsManager.getTimeFormat().format(new Date(mLastRefreshed));

		if (mLastRefreshedTv != null)
		{
			mLastRefreshedTv.setText(String.format(getString(R.string.last_refreshed), date, time));
		}
	}

	@Override public void onStop()
	{
		if (SettingsManager.isStreamMarkerEnabled())
		{
			int pos = Math.max(0, getListView().getFirstVisiblePosition() - getListView().getHeaderViewsCount());
			NetObject item = getAdapter().getItem(pos);

			if (item != null && getAdapter() != null && getAdapter().getStreamMarker() != null)
			{
				String markerName = getAdapter().getStreamMarker().getName();

				if (TextUtils.isEmpty(markerName))
				{
					markerName = "my_stream";
				}

				String id = item.getId();
				boolean syncMarker = true;

				if (!SettingsManager.isStreamMarkerPastEnabled())
				{
					try
					{
						long markerId = Long.parseLong(getAdapter().getStreamMarker().getId());
						long postId = Long.parseLong(id);

						if (postId < markerId)
						{
							syncMarker = false;
						}
					}
					catch (Exception e){}
				}

				if (syncMarker)
				{
					APIManager.getInstance().updateMarker(id, markerName, new JsonResponseHandler()
					{
						@Override public void onSuccess()
						{
							JsonElement resp = getContent();

							if (resp != null)
							{
								resp.getAsJsonObject().add("marker", resp.getAsJsonObject().get("data"));

								StreamMarker marker = StreamMarker.fromObject(resp.getAsJsonObject());
								Stream stream = CacheManager.getInstance().readFileAsObject(getCacheFileName(), Stream.class);

								if (stream != null)
								{
									stream.setMarker(marker);
									CacheManager.getInstance().writeFile(getCacheFileName(), stream);
								}
							}
						}
					});
				}
			}
		}

		super.onStop();
	}

	@Override public void onDestroyView()
	{
		int[] pos = getLastViewPosition(null);

		SharedPreferences prefs = getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
		prefs
			.edit()
			.putInt(String.format(Constants.PREFS_TIMELINE_TOP_POSITION, UserManager.getUserId()), pos[0])
			.putInt(String.format(Constants.PREFS_TIMELINE_TOP_POSITION_Y, UserManager.getUserId()), pos[1])
		.apply();

		super.onDestroyView();
	}

	@Override public void onFinishedWriting()
	{
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			Context context = getApplicationContext();
			ComponentName name = new ComponentName(context, ScrollWidgetProvider.class);
			int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);

			for (int index = 0; index < ids.length; index++)
			{
				Intent refresh = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				refresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, ids[index]);
				getContext().sendBroadcast(refresh);
			}
		}
	}

	@Override public String getCacheFileName()
	{
		return String.format(Constants.CACHE_TIMELINE_LIST_NAME, UserManager.getUserId());
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			String.format(Constants.RESPONSE_TIMELINE, UserManager.getUserId()),
			String.format(Constants.RESPONSE_TIMELINE_MISSING_POSTS, UserManager.getUserId())
		};
	}

	@Subscribe public void onProfileUpdated(ProfileUpdatedEvent event)
	{
		setHeaderUrl(event.getUser().getCoverUrl());
	}

	@Subscribe @Override public void onPostRecieved(NewPostEvent event)
	{
		super.onPostRecieved(event);
		writeToCache(getAdapter().getStream());
	}

	@Subscribe @Override public void onPostDeleted(DeletePostEvent event)
	{
		super.onPostDeleted(event);
	}

	@Override public void beginLoadFromApi(){}
}