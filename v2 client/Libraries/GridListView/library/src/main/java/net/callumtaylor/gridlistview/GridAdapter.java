package net.callumtaylor.gridlistview;

import android.widget.BaseAdapter;

public abstract class GridAdapter extends BaseAdapter
{
	/**
	 * @param rowIndex The index of the row 0 based
	 * @return Return -1 to use the default set
	 */
	public int getColumnCount(int rowIndex)
	{
		return -1;
	}
}
