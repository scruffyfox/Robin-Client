package net.callumtaylor.swipetorefresh.view;

import net.callumtaylor.swipetorefresh.helper.RefreshDelegate;
import net.callumtaylor.swipetorefresh.helper.RefreshDelegate.ScrollDelegate;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class RefreshableListView extends ListView implements View.OnTouchListener, ScrollDelegate
{
	private boolean mBlockLayoutChildren = false;
	private boolean canRefresh = true;
	private int touchDownPos = Integer.MAX_VALUE;
	public RefreshDelegate refreshDelegate;

	public RefreshableListView(Context context)
	{
		super(context);
		init();
	}

	public RefreshableListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public boolean getCanRefresh()
	{
		return canRefresh;
	}

	public void setCanRefresh(boolean canRefresh)
	{
		this.canRefresh = canRefresh;
	}

	/**
	 * Starts a refresh intent only showing the indeterminate progress
	 */
	public void indeterminateRefresh()
	{
		refreshDelegate.fauxRefresh();
	}

	private void init()
	{
		refreshDelegate = new RefreshDelegate(getContext(), this);
		setOnTouchListener(this);
	}

	@Override public boolean isScrolledToTop()
	{
		if (getCount() == 0)
		{
			return true;
		}
		else if (touchDownPos < 2)
		{
			return true;
		}

		return false;
	}

	@Override public boolean canStartRefreshing()
	{
		if (getCount() == 0)
		{
			return true;
		}
		else if (getFirstVisiblePosition() <= 0)
		{
			final View firstVisibleChild = getChildAt(0);
			return firstVisibleChild != null && firstVisibleChild.getTop() >= 0;
		}

		return false;
	}

	@Override protected void layoutChildren()
	{
		if (!mBlockLayoutChildren)
		{
			super.layoutChildren();
		}
	}

	public void onRefreshComplete()
	{
		refreshDelegate.onRefreshComplete();
	}

	@Override public final boolean onTouch(View view, MotionEvent event)
	{
		if ((event.getAction() == MotionEvent.ACTION_DOWN)
		|| (event.getAction() == MotionEvent.ACTION_MOVE && touchDownPos >= Integer.MAX_VALUE))
		{
			touchDownPos = getFirstVisiblePosition() - getHeaderViewsCount();
		}
		else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP)
		{
			touchDownPos = Integer.MAX_VALUE;
		}

		if (canRefresh)
		{
			refreshDelegate.onTouch(view, event);
		}

		return false;
	}

	public void setBlockLayoutChildren(boolean t)
	{
		mBlockLayoutChildren = t;
	}

	public void setOnOverScrollListener(OnOverScrollListener l)
	{
		refreshDelegate.setOnOverScrollListener(l);
	}

	public void startRefresh()
	{
		refreshDelegate.startRefresh();
	}

	@Override public void onResetTouch()
	{
	//	touchDownPos = Integer.MAX_VALUE;
	}
}