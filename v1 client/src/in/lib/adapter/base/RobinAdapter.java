package in.lib.adapter.base;

import in.lib.holder.base.ViewHolder;
import in.lib.manager.SettingsManager;
import in.lib.utils.ViewUtils;
import in.lib.view.HeadedListView;
import in.lib.view.ResizableTextView;
import in.model.Stream;
import in.model.base.NetObject;
import in.obj.StreamMarker;
import in.rob.client.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class RobinAdapter extends BaseAdapter implements OnClickListener, OnItemLongClickListener, HeadedListView.OnScrollListener
{
	public enum Order
	{
		ASC,
		DESC
	};

	@Getter private volatile Stream stream;
	private final LayoutInflater mLayoutInflater;
	private Order order = Order.ASC;
	private HeadedListView mListView;
	@Setter protected OnPagerListener onPagerListener;
	@Getter @Setter private int pageCount = SettingsManager.getPageSize();
	@Getter @Setter private boolean animationsEnabled = true;
	@Getter @Setter private int lastPositionAnimated = 5;

	public static abstract class OnPagerListener
	{
		public abstract void endReached();
		public void onBreakClicked(NetObject v){}
	}

	public RobinAdapter(Context context, Stream stream)
	{
		this.stream = stream;
		mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public RobinAdapter(Context context, Stream stream, Order order)
	{
		this.order = order;
		this.stream = stream;
		setItems(stream.getObjects());
		mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public RobinAdapter(Context context, List<? extends NetObject> items)
	{
		stream = new Stream();
		stream.getObjects().addAll(items);
		mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public RobinAdapter(Context context, List<? extends NetObject> items, Order order)
	{
		stream = new Stream();
		this.order = order;
		setItems(items);
		mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void refreshFontSizes()
	{
		List<View> views = new ArrayList<View>();
		getListView().reclaimViews(views);

		if (views != null && views.size() > 0)
		{
			for (View v : views)
			{
				List<View> children = ViewUtils.getAllChildrenByInstance((ViewGroup)v, ResizableTextView.class);

				if (children != null && children.size() > 0)
				{
					for (View c : children)
					{
						((ResizableTextView)c).refresh();
					}
				}
			}
		}
	}

	public void setStream(Stream s)
	{
		if (s != null)
		{
			stream = s;
		}
	}

	public void setStreamMarker(StreamMarker m)
	{
		stream.setMarker(m);
	}

	public StreamMarker getStreamMarker()
	{
		return stream.getMarker();
	}

	public void setHasMore(boolean more)
	{
		stream.setHasMore(more);
	}

	public String getLastId()
	{
		return stream.getMinId();
	}

	public String getFirstId()
	{
		return stream.getMaxId();
	}

	public int getBreakPosition()
	{
		return stream.getBreakPosition();
	}

	public void setBreakPosition(int pos)
	{
		stream.setBreakPosition(pos);
	}

	public void setLastId(String id)
	{
		stream.setMinId(id);
	}

	public void setFirstId(String id)
	{
		stream.setMaxId(id);
	}

	public ListView getListView()
	{
		return mListView;
	}

	/**
	 * @return The current set layout inflater
	 */
	public LayoutInflater getLayoutInflater()
	{
		return mLayoutInflater;
	}

	/**
	 * @return The number of items in the current list
	 */
	@Override public int getCount()
	{
		return stream.getObjects().size();
	}

	/**
	 * @param position The position to fetch
	 * @return The item object at the position
	 */
	@Override public NetObject getItem(int position)
	{
		if (stream.getObjects().size() <= position)
		{
			return null;
		}

		return stream.getObjects().get(position);
	}

	/**
	 * Gets an item from it's ID
	 * @param id the ID to search for
	 * @return The object, or null
	 */
	public NetObject getItemById(String id)
	{
		for (NetObject obj : stream.getObjects())
		{
			if (obj.getId().equals(id))
			{
				return obj;
			}
		}

		return null;
	}

	/**
	 * @return The list of items
	 */
	public List<NetObject> getItems()
	{
		return stream.getObjects();
	}

	/**
	 * @param position The position to of the object to fetch
	 * @return The id of the fetched object
	 */
	public String getId(int position)
	{
		return stream.getObjects().get(position).getId();
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		if (onPagerListener != null)
		{
			int trigger = getCount() - (getPageCount() / 2);
			if (trigger >= getPageCount() / 2 && position >= trigger)
			{
				onPagerListener.endReached();
			}
		}

		return null;
	}

	@Override public long getItemId(int position)
	{
		try
		{
			return Long.parseLong(getItem(position).getId());
		}
		catch (Exception e)
		{
			return -1;
		}
	}

	/**
	 * @param order The new order of the list, Either Order.ASC or Order.DESC
	 */
	public void setOrder(Order order)
	{
		this.order = order;
	}

	/**
	 * @param list The new list view in the adapter
	 */
	public void setListView(HeadedListView list)
	{
		mListView = list;

		if (list != null)
		{
			list.setOnItemLongClickListener(this);
			list.addOnScrollListener(this);
			list.setRecyclerListener(new RecyclerListener()
			{
				@Override public void onMovedToScrapHeap(View view)
				{
					Object tag = view.getTag(R.id.TAG_VIEW_HOLDER);
					if (tag instanceof ViewHolder)
					{
						ViewHolder holder = (ViewHolder)tag;
						holder.onViewDestroyed(view);
					}
				}
			});
		}
	}

	/**
	 * @param position The position to add the new item
	 * @param item The item to add
	 */
	public void addItem(int position, NetObject item)
	{
		stream.getObjects().add(position, item);
	}

	/**
	 * @param position The position to add the new item
	 * @param items The items to add
	 */
	public void addItems(int position, List<? extends NetObject> items)
	{
		stream.getObjects().addAll(position, items);
	}

	/**
	 * Adds a new item in the list based on the set Order
	 * @param item Thew item to add
	 */
	public void addItem(NetObject item)
	{
		if (item == null) return;

		if (order.equals(Order.DESC))
		{
			stream.getObjects().add(item);
		}
		else
		{
			stream.getObjects().add(0, item);
		}
	}

	/**
	 * Adds items to the list based on the set Order
	 * @param items The list of items to add
	 */
	public void addItems(List<? extends NetObject> items)
	{
		if (items == null) return;

		if (order.equals(Order.DESC))
		{
			stream.getObjects().addAll(items);
		}
		else
		{
			Collections.reverse(items);
			stream.getObjects().addAll(0, items);
		}
	}

	/**
	 * Removes an item from the adapter
	 * @param item The item to remove
	 */
	public void removeItem(NetObject item)
	{
		stream.getObjects().remove(item);
	}

	/**
	 * @param id The id of the item to remove
	 */
	public void removeItem(String id)
	{
		int size = stream.getObjects().size();
		for (int index = 0; index < size; index++)
		{
			if (stream.getObjects().get(index).getId().equals(id))
			{
				stream.getObjects().remove(index);
				break;
			}
		}
	}

	/**
	 * Removes an item at the index
	 * @param index The index to remove the item
	 */
	public void removeItemAt(int index)
	{
		stream.getObjects().remove(index);
	}

	/**
	 * Adds items to the start of the list. Ignores the set Order
	 * @param items The items to add
	 */
	public void prependItems(List<? extends NetObject> items)
	{
		stream.getObjects().addAll(0, items);
	}

	/**
	 * Adds an item to the start of the list. Ignores the set Order
	 * @param items The item to add
	 */
	public void prependItem(NetObject items)
	{
		stream.getObjects().add(0, items);
	}

	/**
	 * Adds an item to the end of the list. Ignores the set Order
	 * @param item The item to add
	 */
	public void appendItem(NetObject item)
	{
		stream.getObjects().add(item);
	}

	/**
	 * Adds items to the end of the list. Ignores the set Order
	 * @param item The items to add
	 */
	public void appendItem(List<? extends NetObject> item)
	{
		stream.getObjects().addAll(item);
	}

	/**
	 * Clears the list and sets the new item set
	 * @param items The items to add
	 */
	public void setItems(List<? extends NetObject> items)
	{
		stream.getObjects().clear();
		addItems(items);
	}

	/**
	 * Clears the list
	 */
	public void clear()
	{
		stream.getObjects().clear();
	}

	/**
	 * @param item The item to find
	 * @return The postition of said new item
	 */
	public int indexOf(NetObject item)
	{
		return stream.getObjects().indexOf(item);
	}

	/**
	 * @param item The ID of the item to find
	 * @return The postition of said new item
	 */
	public int indexOf(String id)
	{
		for (int index = 0, size = stream.getObjects().size(); index < size; index++)
		{
			if (stream.getObjects().get(index).getId().equals(id))
			{
				return index;
			}
		}

		return -1;
	}

	@Override public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		return false;
	}

	@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){}
	@Override public void onScrollStateChanged(AbsListView view, int scrollState){}
	@Override public void onClick(View v){}
	@Override public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){}
}