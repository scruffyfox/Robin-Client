package net.callumtaylor.gridlistview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

class GridCellWrapper extends FrameLayout
{
	public GridCellWrapper(Context context)
	{
		super(context);

		init();
	}

	private void init()
	{
		setLayoutParams(new LinearLayout.LayoutParams(0, -1));
		((LinearLayout.LayoutParams)getLayoutParams()).weight = 1;

		setBackgroundResource(android.R.drawable.list_selector_background);
	}

	@Override public void addView(View child, int index, ViewGroup.LayoutParams params)
	{
		if (child.getBackground() != null)
		{
			setBackgroundDrawable(null);
		}

		super.addView(child, index, params);
	}
}
