package in.rob.client.page;

import in.lib.Constants;
import in.lib.event.DeletePostEvent;
import in.lib.event.NewPostEvent;
import in.lib.handler.streams.GlobalResponseHandler;
import in.lib.handler.streams.MissingPostsResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.model.Stream;
import in.model.base.NetObject;
import in.rob.client.page.base.PostStreamFragment;
import in.rob.client.page.base.StreamFragment;
import in.rob.client.widget.ScrollWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.squareup.otto.Subscribe;

/**
 * Fragment used for displaying posts in a fragment list
 * @author callumtaylor
 */
public class GlobalPage extends PostStreamFragment
{
	@Override public void initData()
	{
		if (getCacheFileName() != null && getCacheManager().fileExists(getCacheFileName()))
		{
			runOnUiThread(new Runnable()
			{
				@Override public void run()
				{
					new CacheLoader(getCacheFileName()).execute();
				}
			});
		}
	}

	@Override public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser)
		{
			runWhenReady(new Runnable()
			{
				@Override public void run()
				{
					if (getAdapter() != null && getAdapter().getCount() < SettingsManager.getPageSize())
					{
						onForceRefresh();
					}
				}
			});
		}
	}

	@Override public void onDataReady()
	{
		SharedPreferences prefs = getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
		int topPos = prefs.getInt(String.format(Constants.PREFS_GLOBAL_TOP_POSITION, UserManager.getUserId()), 0);
		int topPosY = prefs.getInt(String.format(Constants.PREFS_GLOBAL_TOP_POSITION_Y, UserManager.getUserId()), 0);
		getHeadedListView().setSelectionFromTop(topPos, topPosY);
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		showProgressLoader();

		GlobalResponseHandler handler = new GlobalResponseHandler(getApplicationContext(), append);
		handler.setResponseKey(Constants.RESPONSE_GLOBAL);
		ResponseHelper.getInstance().addResponse(Constants.RESPONSE_GLOBAL, handler, this);
		APIManager.getInstance().getGlobalTimeLine(lastId, handler);
	}

	@Override public void loadMissingItems(final NetObject o)
	{
		showProgressLoader();

		MissingPostsResponseHandler handler = new MissingPostsResponseHandler(getApplicationContext(), o);
		handler.setResponseKey(Constants.RESPONSE_GLOBAL_MISSING_POSTS);
		ResponseHelper.getInstance().addResponse(Constants.RESPONSE_GLOBAL_MISSING_POSTS, handler, this);

		APIManager.getInstance().getMissingGlobal(o.getId(), -SettingsManager.getPageSize(), handler);
	}

	@Override public void onDestroyView()
	{
		int[] pos = getLastViewPosition(null);

		SharedPreferences prefs = getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
		prefs
			.edit()
			.putInt(String.format(Constants.PREFS_GLOBAL_TOP_POSITION, UserManager.getUserId()), pos[0])
			.putInt(String.format(Constants.PREFS_GLOBAL_TOP_POSITION_Y, UserManager.getUserId()), pos[1])
		.apply();

		super.onDestroyView();
	}

	@Override public String getCacheFileName()
	{
		return Constants.CACHE_GLOBAL_LIST_NAME;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{Constants.RESPONSE_GLOBAL, Constants.RESPONSE_GLOBAL_MISSING_POSTS};
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

	@Subscribe @Override public void onPostRecieved(NewPostEvent event)
	{
		super.onPostRecieved(event);
	}

	@Subscribe @Override public void onPostDeleted(DeletePostEvent event)
	{
		super.onPostDeleted(event);
	}

	public class CacheLoader extends StreamFragment.CacheLoader
	{
		public CacheLoader(String s)
		{
			super(s);
		}

		@Override public void onPostExecute(Stream stream)
		{
			if (getActivity() == null)
			{
				return;
			}

			if (stream != null && stream.getObjects().size() > 0)
			{
				getAdapter().setStream(stream);
				getAdapter().notifyDataSetChanged();
				checkAdapterSizes();
			}

			stream = null;

			runWhenReady(new Runnable()
			{
				@Override public void run()
				{
					onDataReady();
				}
			});
		}
	}
}