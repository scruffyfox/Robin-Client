package in.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.data.annotation.FileAnnotation;
import in.rob.client.R;
import in.view.delegate.FileDelegate;

public class FileAdapter extends RobinAdapter<FileAnnotation>
{
	private static final int TYPE_IMAGE = 0;
	private int columnCount = 2;

	public FileAdapter(Context context)
	{
		super(context);

		getItemTypes().put(TYPE_IMAGE, new FileDelegate(this));
		columnCount = getContext().getResources().getInteger(R.integer.browser_grid_column_count);
	}

	@Override public int getItemViewType(int position)
	{
		return TYPE_IMAGE;
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		if (pageListener != null)
		{
			int trigger = getCount() - (getPageCount() / columnCount);
			if (trigger >= (getPageCount() / columnCount) && position >= trigger)
			{
				pageListener.onEndReached();
			}
		}

		int viewType = getItemViewType(position);
		FileAnnotation item = getItem(position);

		convertView = getItemTypes().get(viewType).getView(item, position, convertView, parent, LayoutInflater.from(getContext()));

		return convertView;
	}
}
