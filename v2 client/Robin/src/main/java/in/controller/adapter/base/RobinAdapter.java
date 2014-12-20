package in.controller.adapter.base;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import in.data.stream.base.Stream;
import in.lib.view.LinkedTextView;
import in.model.AdnModel;
import in.view.delegate.base.AdapterDelegate;
import lombok.Getter;
import lombok.Setter;

public abstract class RobinAdapter<T extends AdnModel> extends BaseAdapter implements OnItemLongClickListener
{
	@Getter private Context context;
	@Getter private SparseArray<AdapterDelegate<T>> itemTypes = new SparseArray<AdapterDelegate<T>>();
	@Getter protected Stream<T> stream;
	@Setter protected PageListener pageListener;
	@Getter private ListView listView;
	@Getter private int pageCount = 60;

	public static interface PageListener
	{
		public void onEndReached();
	}

	public RobinAdapter(Context context)
	{
		this.context = context;
	}

	public void setStream(Stream<T> stream)
	{
		this.stream = null;
		this.stream = stream;
	}

	@Override public boolean isEmpty()
	{
		return this.stream == null || (this.stream != null && this.stream.getItems().size() < 1);
	}

	/**
	 * Appends the stream object with new items and updates the {@link in.data.Meta#minId} and {@link in.data.Meta#moreAvailable}
	 * members.
	 * @param stream
	 */
	public void appendStream(Stream stream)
	{
		if (stream == null)
		{
			setStream(stream);
		}
		else
		{
			this.stream.getItems().addAll(stream.getItems());
			this.stream.getMeta().setMinId(stream.getMeta().getMinId());
			this.stream.getMeta().setMoreAvailable(stream.getMeta().isMoreAvailable());
		}
	}

	public void addItem(T item)
	{
		if (stream != null)
		{
			this.stream.getItems().add(item);
			this.stream.getMeta().setMinId(item.getId());
		}
	}

	public int indexOf(T item)
	{
		for (int index = 0, count = getCount(); index < count; index++)
		{
			if (getItem(index).equals(item))
			{
				return index;
			}
		}

		return -1;
	}

	public void setListView(ListView list)
	{
		this.listView = list;
		this.listView.setOnItemLongClickListener(this);
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		if (pageListener != null)
		{
			int trigger = getCount() - (pageCount / 2);
			if (trigger >= pageCount / 2 && position >= trigger)
			{
				pageListener.onEndReached();
			}
		}

		int viewType = getItemViewType(position);
		T item = (T)getItem(position);

		convertView = getItemTypes().get(viewType).getView(item, position, convertView, parent, LayoutInflater.from(getContext()));

		return convertView;
	}

	@Override public int getViewTypeCount()
	{
		return itemTypes.size();
	}

	@Override public boolean hasStableIds()
	{
		return true;
	}

	@Override public int getCount()
	{
		if (this.stream != null && this.stream.getItems() != null)
		{
			return this.stream.getItems().size();
		}

		return 0;
	}

	@Override public T getItem(int position)
	{
		if (this.stream != null && this.stream.getItems() != null)
		{
			return this.stream.getItems().get(position);
		}

		return null;
	}

	@Override public long getItemId(int position)
	{
		if (this.stream != null && this.stream.getItems() != null)
		{
			try
			{
				return Long.parseLong(getItem(position).getId());
			}
			catch (NumberFormatException e){}
		}

		return 0L;
	}

	@Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		// weird conflicts with LinkifiedTextView on Jelly Bean
		if (view instanceof LinkedTextView)
		{
			return false;
		}

		int viewType = getItemViewType(position - getListView().getHeaderViewsCount());
		boolean ret = getItemTypes().get(viewType).onItemLongClick(position, view);

		return ret;
	}
}
