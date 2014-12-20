package net.callumtaylor.gridlistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class GridListView extends ListView
{
	private int mNumColumns = 0;

	private OnItemClickListener clickListener;
	private OnItemLongClickListener longClickListener;

	public GridListView(Context context)
	{
		super(context);
	}

	public GridListView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public GridListView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.GridListView);
		mNumColumns = attributes.getInt(R.styleable.GridListView_numColumns, 1);
		attributes.recycle();

		setSelector(new ColorDrawable(0x00000000));
	}

	public void setNumColumns(int columnCount)
	{
		this.mNumColumns = columnCount;

		if (getAdapter() != null)
		{
			((InternalAdapterImpl)getAdapter()).notifyDataSetChanged();
		}
	}

	public int getNumColumns()
	{
		return this.mNumColumns;
	}

	@Override public void setAdapter(ListAdapter adapter)
	{
		if (!(adapter instanceof BaseAdapter))
		{
			super.setAdapter(adapter);
			return;
		}

		InternalAdapterImpl wrapper = new InternalAdapterImpl(getContext(), (BaseAdapter)adapter);
		wrapper.generateColumnSpec();
		super.setAdapter(wrapper);
	}

	/**
	 * You must call this instead of {@link android.widget.ListView#getAdapter()} to get
	 * your base adapter. Calling {@link android.widget.ListView#getAdapter()} will return the wrapper
	 * adapter for the grid view.
	 * @return
	 */
	public ListAdapter getBaseAdapter()
	{
		ListAdapter adapter = super.getAdapter();

		if (adapter instanceof HeaderViewListAdapter)
		{
			adapter = (InternalAdapterImpl)((HeaderViewListAdapter)adapter).getWrappedAdapter();
		}

		if (adapter != null)
		{
			return ((InternalAdapterImpl)adapter).baseAdapter;
		}

		return null;
	}

	@Override public void setOnItemClickListener(OnItemClickListener listener)
	{
		this.clickListener = listener;
	}

	@Override public void setOnItemLongClickListener(OnItemLongClickListener listener)
	{
		this.longClickListener = listener;
	}

	@Override public boolean performItemClick(View view, int position, long id)
	{
		if (this.clickListener != null)
		{
			this.clickListener.onItemClick(this, view, position, id);
			return true;
		}

		return false;
	}

	private class InternalAdapterImpl extends BaseAdapter
	{
		private BaseAdapter baseAdapter;
		private Context context;
		private Integer[] columnSpec;

		public InternalAdapterImpl(Context context, BaseAdapter adapter)
		{
			this.baseAdapter = adapter;
			this.context = context;
		}

		private void generateColumnSpec()
		{
			if (baseAdapter instanceof GridAdapter)
			{
				ArrayList<Integer> columnSpecList = new ArrayList<Integer>();
				int total = getBaseCount();
				int index = 0;
				while (total > 0)
				{
					int columnCount = ((GridAdapter)baseAdapter).getColumnCount(index);
					columnSpecList.add(columnCount < 1 ? mNumColumns : columnCount);
					total -= columnSpecList.get(index++);
				}

				columnSpec = columnSpecList.toArray(new Integer[columnSpecList.size()]);
			}
		}

		private int getBaseCount()
		{
			return baseAdapter.getCount();
		}

		@Override public int getCount()
		{
			return columnSpec == null ? (int)Math.ceil((float)baseAdapter.getCount() / (float)getNumColumns()) : columnSpec.length;
		}

		@Override public Object getItem(int position)
		{
			return baseAdapter.getItem(position);
		}

		@Override public long getItemId(int position)
		{
			return baseAdapter.getItemId(position);
		}

		@Override public boolean hasStableIds()
		{
			return baseAdapter.hasStableIds();
		}

		@Override public void registerDataSetObserver(DataSetObserver observer)
		{
			baseAdapter.registerDataSetObserver(observer);
		}

		@Override public void unregisterDataSetObserver(DataSetObserver observer)
		{
			baseAdapter.unregisterDataSetObserver(observer);
		}

		@Override public void notifyDataSetChanged()
		{
			generateColumnSpec();

			super.notifyDataSetChanged();
			baseAdapter.notifyDataSetChanged();
		}

		@Override public void notifyDataSetInvalidated()
		{
			super.notifyDataSetInvalidated();
			baseAdapter.notifyDataSetInvalidated();
		}

		@Override public boolean areAllItemsEnabled()
		{
			return baseAdapter.areAllItemsEnabled();
		}

		@Override public boolean isEnabled(int position)
		{
			return baseAdapter.isEnabled(position);
		}

		@Override public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
			return baseAdapter.getDropDownView(position, convertView, parent);
		}

		@Override public int getItemViewType(int position)
		{
			return baseAdapter.getItemViewType(position);
		}

		@Override public int getViewTypeCount()
		{
			return baseAdapter.getViewTypeCount();
		}

		@Override public boolean isEmpty()
		{
			return baseAdapter.isEmpty();
		}

		@Override public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = LayoutInflater.from(context).inflate(R.layout.row, parent, false);
			}

			int columnCount = mNumColumns;
			int previousItems = 0;

			if (baseAdapter instanceof GridAdapter)
			{
				columnCount = columnSpec[position];
				columnCount = columnCount < 1 ? mNumColumns : columnCount;

				for (int index = 0; index < position; index++)
				{
					previousItems += columnSpec[index];
				}
			}
			else
			{
				previousItems += mNumColumns * position;
			}

			((LinearLayout)convertView).setWeightSum(columnCount);
			View[] convertViews = new View[columnCount];

			for (int index = 0; index < columnCount; index++)
			{
				if (previousItems + index >= getBaseCount()) break;

				GridCellWrapper wrapper;
				if ((wrapper = (GridCellWrapper)((LinearLayout)convertView).getChildAt(index)) == null)
				{
					wrapper = new GridCellWrapper(context);
				}

				View v = baseAdapter.getView(previousItems + index, wrapper.getChildAt(0), wrapper);
				wrapper.removeAllViews();
				wrapper.addView(v);

				convertViews[index] = wrapper;
			}

			((LinearLayout)convertView).removeAllViews();

			for (int index = 0; index < columnCount; index++)
			{
				if (convertViews[index] == null) break;

				final int pos = previousItems + index;
				if (baseAdapter.isEnabled(pos))
				{
					convertViews[index].setOnClickListener(new OnClickListener()
					{
						@Override public void onClick(View v)
						{
							if (clickListener != null)
							{
								clickListener.onItemClick(GridListView.this, v, pos, baseAdapter.getItemId(pos));
							}
						}
					});

					if (longClickListener != null)
					{
						convertViews[index].setOnLongClickListener(new OnLongClickListener()
						{
							@Override public boolean onLongClick(View v)
							{
								return longClickListener.onItemLongClick(GridListView.this, v, pos, baseAdapter.getItemId(pos));
							}
						});
					}
				}

				((LinearLayout)convertView).addView(convertViews[index]);
			}

			return convertView;
		}
	}
}
