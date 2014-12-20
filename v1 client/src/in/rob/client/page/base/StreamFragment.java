package in.rob.client.page.base;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.adapter.PostAdapter;
import in.lib.adapter.base.RobinAdapter;
import in.lib.adapter.base.RobinAdapter.OnPagerListener;
import in.lib.annotation.InjectView;
import in.lib.helper.ResponseHelper;
import in.lib.loader.base.Loader;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.thread.FragmentRunnable;
import in.lib.utils.Dimension;
import in.lib.utils.Views;
import in.lib.view.HeadedListView;
import in.lib.view.HeadedListView.OnScrollListener;
import in.lib.writer.CacheWriter.WriterListener;
import in.model.Post;
import in.model.Stream;
import in.model.base.Message;
import in.model.base.NetObject;
import in.rob.client.MainApplication;
import in.rob.client.R;
import in.rob.client.base.RobinListFragment;
import in.rob.client.base.RobinSlidingActivity;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.callumtaylor.swipetorefresh.helper.RefreshHelper;
import net.callumtaylor.swipetorefresh.helper.RefreshHelper.OnRefreshListener;
import net.callumtaylor.swipetorefresh.view.RefreshableScrollView;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public abstract class StreamFragment extends RobinListFragment implements OnScrollListener, WriterListener, OnRefreshListener, OnItemClickListener
{
	@Getter private RobinAdapter adapter;

	// views
	@Getter private View paddingView;
	@Getter private View loadMoreView;
	@Getter @InjectView(android.R.id.empty) public RefreshableScrollView emptyListView;
	@Getter @InjectView(R.id.progress_loader) public View progressLoader;
	@Getter @InjectView(R.id.new_posts_ticker) public TextView postTicker;

	@Getter @Setter private boolean allowPagination = true;
	@Getter private boolean loading = false;
	private boolean mForceRefresh = false;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.timeline_view, container, false);
		Views.inject(this, v);
		return v;
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		refreshHelper = RefreshHelper.wrapRefreshable(view, this);

		if (isLoading() && ((RobinSlidingActivity)getActivity()).getAdapter().getCurrentFragment() == this)
		{
			refreshHelper.setRefreshing(true);
			refreshHelper.showHelper();
		}

		addDefaultFooters();

		getHeadedListView().setOnItemClickListener(this);
		getHeadedListView().addOnScrollListener(this);

		checkPendingThenExecute();
	}

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getArguments() != null)
		{
			mForceRefresh = getArguments().getBoolean(Constants.EXTRA_FORCE_REFRESH, false);
			getArguments().remove(Constants.EXTRA_FORCE_REFRESH);
		}

		retrieveArguments(getArguments());
		initData();
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		setupAdapters();
		attachHandlers();

		if (getAdapter() != null)
		{
			getAdapter().setListView(getHeadedListView());
		}

		checkPendingThenExecute();
	}

	@Override public void onDestroyView()
	{
		Views.reset(this);
		setListAdapter(null);
		super.onDestroyView();
	}

	@Override public void onDestroy()
	{
		super.onDestroy();
		detachHandlers();
	}

	public HeadedListView getHeadedListView()
	{
		return ((HeadedListView)getListView());
	}

	/**
	 * Called just after retrieveArguments in onActivityCreated. This
	 * is where the cache loader is called and executed. If no cache is
	 * loaded, or not found {@link beginLoadFromApi()} is called.
	 */
	public void initData()
	{
		runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				new CacheLoader(getCacheFileName()).execute();
			}
		});
	}

	public void setTicker(int count)
	{
		if (count > 0)
		{
			AnimationSet anim = new AnimationSet(false);

			TranslateAnimation fwdAnim = new TranslateAnimation
			(
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, -1f,
				Animation.RELATIVE_TO_SELF, 0f
			);

			anim.setInterpolator(new LinearInterpolator());
			anim.setDuration(600);

			TranslateAnimation revAnim = new TranslateAnimation
			(
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, 0f,
				Animation.RELATIVE_TO_SELF, -1f
			);

			revAnim.setInterpolator(new LinearInterpolator());
			revAnim.setDuration(600);
			revAnim.setStartOffset(2500);

			revAnim.setAnimationListener(new AnimationListener()
			{
				@Override public void onAnimationStart(Animation animation){}
				@Override public void onAnimationRepeat(Animation animation){}

				@Override public void onAnimationEnd(Animation animation)
				{
					postTicker.setVisibility(View.GONE);
				}
			});

			anim.addAnimation(fwdAnim);
			anim.addAnimation(revAnim);

			postTicker.setVisibility(View.VISIBLE);
			postTicker.setText(String.valueOf(count) + " new posts");
			postTicker.startAnimation(anim);
		}
	}

	public void setLoading(boolean loading)
	{
		this.loading = loading;

		if (loading)
		{
			showProgressLoader();
		}
		else
		{
			hideProgressLoader();
		}
	}

	/**
	 * Gets the position data for the current view position in a list of new
	 * items from a stream
	 *
	 * @param items
	 *            The new list of items (to calculate the current view's new
	 *            position when this list is added to the adapter)
	 * @return An array of integer, {pos, y pos}
	 */
	public int[] getFirstViewPosition(List<? extends NetObject> items)
	{
		int pos = getListView().getFirstVisiblePosition();

		if (pos < 0)
		{
			return new int[]{0, 0, 0};
		}

		String currentPostId = getCurrentItemId(pos);
		View v = getListView().getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();
		boolean brokenStream = true;

		if (items != null)
		{
			int count = items.size();
			for (int index = 0; index < count; index++)
			{
				NetObject p = items.get(index);
				if (p.getId().equals(currentPostId))
				{
					pos = index;
				}

				if (getAdapter().getItemById(p.getId()) != null)
				{
					brokenStream = false;
				}
			}
		}

		return new int[]{pos, top, brokenStream ? 1 : 0};
	}

	public int[] getLastViewPosition(List<? extends NetObject> items)
	{
		int pos = getListView().getLastVisiblePosition();

		if (pos < 0)
		{
			return new int[]{0, 0, 0};
		}

		String currentPostId = getCurrentItemId(pos);
		View v = getListView().getChildAt(Math.max(0, getListView().getChildCount() - 1));
		int top = (v == null) ? 0 : v.getTop();
		boolean brokenStream = true;

		if (items != null)
		{
			int count = items.size();
			for (int index = 0; index < count; index++)
			{
				NetObject p = items.get(index);
				if (p.getId().equals(currentPostId))
				{
					pos = index;
				}

				if (getAdapter().getItemById(p.getId()) != null)
				{
					brokenStream = false;
				}
			}
		}

		return new int[]{pos, top, brokenStream ? 1 : 0};
	}

	/**
	 * Finds the first item in the list with faux set to false
	 * @param pos The positinon to start at
	 * @return
	 */
	public String getCurrentItemId(int pos)
	{
		NetObject item = getAdapter().getItem(pos);

		if (item instanceof Post && ((Post)item).isNewPost())
		{
			if (pos >= getAdapter().getCount())
			{
				return item == null ? "" : item.getId();
			}
			else
			{
				return getCurrentItemId(pos + 1);
			}
		}
		else
		{
			return item == null ? "" : item.getId();
		}
	}

	/**
	 * Adds the default padding and loading more footers in the order:
	 *
	 * 1. Padding
	 * 2. Loading More
	 */
	public void addDefaultFooters()
	{
		addLoadMoreView();
	}

	/**
	 * Attach all linked response handlers from the helper
	 */
	public void attachHandlers()
	{
		for (String s : getResponseKeys())
		{
			ResponseHelper.getInstance().reattach(s, this);
		}
	}

	/**
	 * Detach all linked response handlers from the helper.
	 * Only call this when the fragment is destroyed.
	 */
	public void detachHandlers()
	{
		//for (String s : getResponseKeys())
		{
			//ResponseHelper.getInstance().detach(s);
		}
	}

	@Getter OnPagerListener pageListener = new OnPagerListener()
	{
		@Override public void endReached()
		{
			if (allowPagination)
			{
				if (!isDetached()
				&& !loading
				&& getAdapter().getStream().getHasMore()
				&& ((MainApplication)getApplicationContext()).isConnected())
				{
					fetchStream(getAdapter().getStream().getMinId(), true);
				}
			}
		}

		@Override public void onBreakClicked(NetObject v)
		{
			loadMissingItems(v);
		};
	};

	public CacheManager getCacheManager()
	{
		return CacheManager.getInstance();
	}

	/**
	 * @param p The post to append the adapter with
	 */
	public void prependItem(NetObject p)
	{
		this.adapter.addItem(0, p);
		this.adapter.notifyDataSetChanged();
	}

	/**
	 * @param p The item to delete from the adapter
	 */
	public void deleteItem(NetObject p)
	{
		this.adapter.removeItem(p.getId());
		this.adapter.notifyDataSetChanged();
	}

	/**
	 * Creates a padding view for list views
	 * @return The padding view
	 */
	public View createPaddingView()
	{
		Dimension dimension = new Dimension(getContext());
		View padding = LayoutInflater.from(getContext()).inflate(R.layout.padding_view, null, false);
		padding.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, (int)(dimension.getScreenHeight() / 2.5f)));
		return padding;
	}

	public void addPaddingView()
	{
		this.paddingView = createPaddingView();
		getHeadedListView().addFooterView(this.paddingView, null, false);
	}

	/**
	 * Adds the loading more view into the list view
	 */
	public void addLoadMoreView()
	{
		this.loadMoreView = LayoutInflater.from(getContext()).inflate(R.layout.list_loading_footer, null);
		if (getAdapter() == null || (getAdapter() != null && !getAdapter().isEmpty() && getAdapter().getStream().getHasMore()))
		{
			getHeadedListView().addFooterView(this.loadMoreView, null, false);
		}
	}

	/**
	 * Removes the loading more view from the list view.
	 * NOTE: Only call this when there are no more items to load in the list
	 */
	public void removeLoadMoreView()
	{
		if (this.loadMoreView != null)
		{
			getHeadedListView().removeFooterView(this.loadMoreView);
			this.loadMoreView = null;
		}
	}

	/**
	 * Finds a view by the resource id in mRootView
	 *
	 * @param id The id of the view to look for
	 * @return The found view or null
	 */
	public View findViewById(int id)
	{
		return getView().findViewById(id);
	}

	/**
	 * Sets the adapter of the list view
	 * @param adapter
	 */
	public void setAdapter(RobinAdapter adapter)
	{
		this.adapter = adapter;
		setListAdapter(adapter);
		this.adapter.setOnPagerListener(pageListener);
	}

	/**
	 * Scrolls the listview to the top
	 */
	public void scrollToTop()
	{
		runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				getListView().setSelection(0);
			}
		});
	}

	/**
	 * Sets the drawable of the header
	 * @param res
	 */
	protected void setHeaderDrawable(int res)
	{
		getHeadedListView().setHeaderImage(res);
	}

	/**
	 * Sets the drawable of the header
	 * @param res
	 */
	protected void setHeaderUrl(String url)
	{
		getHeadedListView().setHeaderUrl(url);
	}

	@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){}

	/**
	 * Called when the pull to refresh trigger is called.
	 *
	 * This should <b>only</b> be for user refresh
	 */
	@Override public void onRefresh()
	{
		if (((MainApplication)getApplicationContext()).isConnected())
		{
			fetchStream("", false);
			showProgressLoader();
		}
		else
		{
			getRefreshHelper().setRefreshing(false);
			getRefreshHelper().finish();
		}
	}

	/**
	 * Call this to implicitly refresh the stream
	 * @param refreshView
	 */
	public void onForceRefresh()
	{
		if (((MainApplication)getApplicationContext()).isConnected())
		{
			getRefreshHelper().setRefreshing(true);
			getHeadedListView().startRefresh();

			if (!(((RobinSlidingActivity)getActivity()).getAdapter().getCurrentFragment() == this))
			{
				getRefreshHelper().hideHelper();
			}
		}
		else
		{
			getRefreshHelper().finish();
		}
	}

	/**
	 * This method is called in {@link #initData(Bundle)} on a fresh instantiation.
	 * Override this and remove the <b>super</b> to stop automatic loading from API.
	 */
	public void beginLoadFromApi()
	{
		onForceRefresh();
	}

	public void writeToCache(Stream s)
	{
		if (!TextUtils.isEmpty(getCacheFileName()))
		{
			CacheManager.getInstance().asyncWriteFile(getCacheFileName(), s, this);
		}
	}

	@Override public void onFinishedWriting()
	{
		finishedCacheWriting();
	}

	public void finishedCacheWriting()
	{

	}

	/**
	 * Checks the adapter sizes and removes the appropriate headers
	 */
	public void checkAdapterSizes()
	{
		Debug.out(getAdapter().getStream().getHasMore());
		try
		{
			if (!getAdapter().getStream().getHasMore())
			{
				removeLoadMoreView();
			}
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Use to fetch the stream data from cache/api
	 *
	 * @param lastId The last loaded Id
	 * @param append whether to append the list or not
	 */
	public abstract void fetchStream(String lastId, final boolean append);

	/**
	 * @return the cache file name associated to the page. return null if cache is not supported
	 */
	public abstract String getCacheFileName();

	/**
	 * Called at the beginning of onActivityCreated. Set up class attributes
	 * from getArguments() Note that this method is NOT
	 * supposed to make any change on the UI or launch whatever request. UI
	 * changes must be made in initData()
	 */
	public abstract void retrieveArguments(Bundle arguments);

	/**
	 * Called at the end of onActivityCreated or after the cache was loaded At
	 * this point, the data should be loaded and it should do only final
	 * treatments and UI updates
	 */
	public abstract void onDataReady();

	/**
	 * Called during onActivityCreated and onDestroy to re-attach and detach
	 * any response handlers in the fragment.
	 *
	 * @return String array of the response helper keys
	 */
	public abstract String[] getResponseKeys();

	/**
	 * Called during onActivityCreated, use this to set up the list adapters
	 * for the stream
	 */
	public abstract void setupAdapters();

	/**
	 * Override this to handle the loading of missing items in the list
	 * @param o The item to load from
	 */
	public void loadMissingItems(NetObject o){}

	/**
	 * Stub method
	 */
	@Override public void onItemClick(android.widget.AdapterView<?> arg0, View arg1, int arg2, long arg3) {};

	/**
	 * Hide the progress loader
	 */
	public void hideProgressLoader()
	{
		this.progressLoader.setVisibility(View.GONE);
	}

	/**
	 * Show the progress loader
	 */
	public void showProgressLoader()
	{
		this.progressLoader.setVisibility(View.VISIBLE);
	}

	/**
	 * Resets the list view to the pos position + top PX
	 * @param pos The pos of the list
	 * @param top The y px padding
	 */
	public void registerPositionReset(final int pos, final int top)
	{
		getListView().post(new Runnable()
		{
			@Override public void run()
			{
				getListView().setSelectionFromTop(pos, top);

				if (getAdapter() instanceof PostAdapter)
				{
					((PostAdapter)getAdapter()).setAnimationsEnabled(true);
				}
			}
		});
	}

	public void postRefreshAdapter()
	{
		getAdapter().notifyDataSetChanged();
		getListView().post(new Runnable()
		{
			@Override public void run()
			{
				getHeadedListView().setBlockLayoutChildren(false);
				getHeadedListView().requestLayout();
			}
		});
	}

	public void refreshAdapter()
	{
		getHeadedListView().setBlockLayoutChildren(false);
		getAdapter().notifyDataSetChanged();
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Constants.RESULT_REFRESH && data != null && adapter != null)
		{
			boolean refresh = false;

			if (data.hasExtra(Constants.EXTRA_REFRESH_TIMELINE)
			|| data.hasExtra(Constants.EXTRA_REFRESH_ANIMATIONS)
			|| data.hasExtra(Constants.EXTRA_REFRESH_INLINE)
			|| data.hasExtra(Constants.EXTRA_REFRESH_NAMES)
			|| data.hasExtra(Constants.EXTRA_REFRESH_ALL_DATA)
			|| data.hasExtra(Constants.EXTRA_REFRESH_LIST))
			{
				refresh = true;
			}

			if (data.hasExtra(Constants.EXTRA_REFRESH_TIMES))
			{
				List<NetObject> items = adapter.getItems();
				for (NetObject i : items)
				{
					if (i instanceof Message)
					{
						((Message)i).setDateStr(((Message)i).calculateDateString());
					}
				}

				refresh = true;
			}

			if (data.hasExtra(Constants.EXTRA_REFRESH_FONTS))
			{
				int[] pos = getLastViewPosition(null);
				this.adapter.refreshFontSizes();
				registerPositionReset(pos[0], pos[1]);
			}

			if (refresh)
			{
				getAdapter().notifyDataSetChanged();
			}
		}
	}

	public class CacheLoader extends Loader<Stream>
	{
		public CacheLoader(String filename)
		{
			super(filename);
		}

		@Override public Stream doInBackground()
		{
			if (getFilename() != null)
			{
				try
				{
					Stream stream = getCacheManager().readFileAsObject(getFilename(), Stream.class);
					return stream;
				}
				catch (Exception e)
				{
					getCacheManager().removeFile(getFilename());
					Debug.out(e);
				}
			}

			return null;
		}

		@Override public void onPostExecute(Stream stream)
		{
			super.onPostExecute(stream);

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

			if ((stream == null || stream.getObjects().size() < 1)
			|| (SettingsManager.getCacheTimeout() > 0 && getCacheManager().fileOlderThan(getFilename(), System.currentTimeMillis() - SettingsManager.getCacheTimeout()))
			|| mForceRefresh)
			{
				mForceRefresh = false;

				// ensure we load only when the views have been created
				runOnUiThread(new FragmentRunnable<StreamFragment>()
				{
					@Override public void run()
					{
						getFragment().beginLoadFromApi();
					}
				});
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

	/**********************************
	 * Unimplemented methods
	 **********************************/
	@Override public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){}
	@Override public void onScrollStateChanged(AbsListView view, int scrollState){}
}