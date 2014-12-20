package in.rob.client.fragment.base;

import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.callumtaylor.swipetorefresh.helper.RefreshHelper;
import net.callumtaylor.swipetorefresh.helper.RefreshHelper.OnRefreshListener;
import net.callumtaylor.swipetorefresh.view.RefreshableListView;

import java.util.List;

import in.controller.adapter.base.RobinAdapter;
import in.controller.adapter.base.RobinAdapter.PageListener;
import in.controller.handler.base.StreamResponseListener;
import in.data.stream.base.Stream;
import in.lib.Constants;
import in.lib.loader.CacheLoader;
import in.lib.loader.Loader.OnFileLoadedListener;
import in.lib.utils.Debug;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.writer.CacheWriter;
import in.model.AdnModel;
import in.rob.client.R;
import in.rob.client.base.BaseActivity;
import lombok.Getter;
import lombok.Setter;

@Injectable
public abstract class StreamFragment extends BaseFragment implements PageListener, OnRefreshListener, OnItemClickListener, StreamResponseListener
{
	private static final String INSTANCE_KEY_LOADING = "loading";

	@Getter @InjectView private ListView listView;
	@Getter @InjectView private View empty;
	@Getter @InjectView private ProgressBar progress;
	@Getter @Setter private RobinAdapter adapter;
	@Getter @Setter private RefreshHelper refreshHelper;
	@Getter private boolean loading = false;

	@Getter private View loadMoreFooter;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.stream_layout, container, false);
		Views.inject(this, view);

		listView.setEmptyView(empty);

		return view;
	}

	@Override public void onCreate(Bundle savedInstanceState)
	{
		retrieveArguments(getArguments());
		super.onCreate(savedInstanceState);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		setupHeaders();
		setupFooters();
		setupAdapter();

		if (adapter != null)
		{
			adapter.setPageListener(this);
			adapter.setListView(listView);
			listView.setAdapter(adapter);
		}

		listView.setOnItemClickListener(this);
		refreshHelper = RefreshHelper.wrapRefreshable(this, this);

		if (savedInstanceState != null)
		{
			retrieveArguments(savedInstanceState);
			onDataReady();
		}
		else
		{
			initData();
		}

		if (isLoading())
		{
			refreshHelper.setRefreshing(true);
			progress.setVisibility(View.VISIBLE);
		}

		attachResponses();
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		if (getAdapter() != null)
		{
			outState.putParcelable(Constants.EXTRA_ADAPTER_LIST, getAdapter().getStream());
		}

		outState.putBoolean(INSTANCE_KEY_LOADING, isLoading());
	}

	@Override public void onEndReached()
	{
		if (getAdapter().getStream().getMeta().isMoreAvailable() && !isLoading())
		{
			fetchStream(getAdapter().getStream().getMeta().getMinId(), true);
		}
	}

	public void setLoading(boolean loading)
	{
		this.loading = loading;
	}

	public void initData()
	{
		progress.setVisibility(View.VISIBLE);

		if (!TextUtils.isEmpty(getCacheKey()))
		{
			CacheLoader<Stream> loader = new CacheLoader<Stream>(getCacheKey());
			loader.setOnFileLoadedListener(new OnFileLoadedListener<Stream>()
			{
				@Override public void onFileLoaded(Stream data, long age)
				{
					if (data != null)
					{
						progress.setVisibility(View.GONE);
						getAdapter().setStream(data);
						getAdapter().notifyDataSetChanged();
					}

					if (data == null
					|| data.getItems().size() < 1
					|| age > getCacheTimeout())
					{
						getRefreshHelper().refresh();
					}

					onDataReady();
				}
			});
			loader.load(getCacheClass());
		}
		else
		{
			getRefreshHelper().refresh();
		}
	}

	public void retrieveArguments(Bundle arguments)
	{
		if (arguments != null)
		{
			if (arguments.containsKey(Constants.EXTRA_ADAPTER_LIST))
			{
				getAdapter().setStream((Stream)arguments.getParcelable(Constants.EXTRA_ADAPTER_LIST));
				getAdapter().notifyDataSetChanged();
				arguments.remove(Constants.EXTRA_ADAPTER_LIST);
			}

			if (arguments.containsKey(INSTANCE_KEY_LOADING))
			{
				this.loading = arguments.getBoolean(INSTANCE_KEY_LOADING);
			}
		}
	}

	@Override public void onRefresh()
	{
		if (((BaseActivity)getActivity()) != null)
		{
			if (!(((BaseActivity)getActivity()).getPageAdapter().getCurrentFragment() == this))
			{
				getRefreshHelper().hideHelper();
			}
		}

		setLoading(true);
		fetchStream("", false);
	}

	@Override public void onDestroyView()
	{
		Views.reset(this);
		adapter = null;

		super.onDestroyView();
	}

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		AdnModel model = getAdapter().getItem(position - getListView().getHeaderViewsCount());
		onListItemClick(model);
	}

	public void onListItemClick(AdnModel item)
	{

	}

	/**
	 * Called at the end of onActivityCreated (if savedInstanceState is not null) or after the cache
	 * was loaded At this point, the data should be loaded and it should do only final
	 * treatments and UI updates
	 */
	public void onDataReady()
	{
	}

	/**
	 * Called to set up the stream page's list headers
	 */
	public void setupHeaders(){}

	/**
	 * Called to set up the stream page's list footers
	 */
	public void setupFooters()
	{
		loadMoreFooter = LayoutInflater.from(getContext()).inflate(R.layout.loading_more_footer, getListView(), false);
		getListView().addFooterView(loadMoreFooter, null, false);
	}

	/**
	 * Called to get the class for loading cache
	 * @return
	 */
	public abstract Class<Stream> getCacheClass();

	/**
	 * Called to set up the stream page's adapter
	 */
	public abstract void setupAdapter();

	/**
	 * Fetches the stream. Called when refreshed, or a new page is scrolled
	 * @param lastId The last ID to load
	 * @param append True if the stream is being appended, false if not
	 */
	public abstract void fetchStream(String lastId, boolean append);

	/**
	 * Called when the stream has responded with a success or failure.
	 * @param stream The stream response or null if the stream failed
	 * @param append True if the stream is being appended, false if not
	 */
	@Override public void handleResponse(final Stream stream, boolean append)
	{
		if (Looper.myLooper() == Looper.getMainLooper())
		{
			Debug.out("Handling response " + this);

			progress.setVisibility(View.GONE);
			getRefreshHelper().finish();

			if (stream != null)
			{
				if (getListView() instanceof RefreshableListView)
				{
					((RefreshableListView)getListView()).setBlockLayoutChildren(true);
				}

				if (append)
				{
					getAdapter().appendStream(stream);
				}
				else
				{
					final int[] lastViewPosition = getLastViewPosition(stream.getItems());
					getAdapter().setStream(stream);

					getListView().post(new Runnable()
					{
						@Override public void run()
						{
							getListView().setSelectionFromTop(lastViewPosition[0], lastViewPosition[1]);
						}
					});
				}

				getAdapter().notifyDataSetChanged();
				getListView().post(new Runnable()
				{
					@Override public void run()
					{
						if (getListView() instanceof RefreshableListView)
						{
							((RefreshableListView)getListView()).setBlockLayoutChildren(false);
							getListView().requestLayout();
						}

					}
				});

				writeToCache();
			}
			else
			{
				Toast.makeText(getContext(), R.string.stream_fail, Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			throw new IllegalAccessError("Handle response was not called on the main thread");
		}
	}

	public int[] getLastViewPosition(List<? extends AdnModel> items)
	{
		int pos = getListView().getLastVisiblePosition() - getListView().getHeaderViewsCount();

		if (pos > -1 && getAdapter().getItem(pos) != null)
		{
			String currentPostId = getAdapter().getItem(pos).getId();
			View v = getListView().getChildAt(Math.max(0, getListView().getChildCount() - 1));
			int top = (v == null) ? 0 : v.getTop();

			if (items != null)
			{
				for (int index = 0, count = items.size(); index < count; index++)
				{
					AdnModel item = items.get(index);
					if (item.getId().equals(currentPostId))
					{
						pos = index;
						break;
					}
				}
			}

			return new int[]{pos, top};
		}
		else
		{
			return new int[]{0, 0};
		}
	}

	public void scrollToTop()
	{
		getListView().smoothScrollToPositionFromTop(0, 0, 200);
	}

	public void writeToCache()
	{
		if (!TextUtils.isEmpty(getCacheKey()))
		{
			CacheWriter writer = new CacheWriter(getCacheKey());
			writer.write(getAdapter().getStream());
		}
	}

	public String getCacheKey()
	{
		return null;
	}

	public int getCacheTimeout()
	{
		return 10 * 60 * 1000;
	}
}
