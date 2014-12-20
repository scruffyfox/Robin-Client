package net.callumtaylor.swipetorefresh.view;

import net.callumtaylor.swipetorefresh.helper.RefreshDelegate;
import net.callumtaylor.swipetorefresh.helper.RefreshDelegate.ScrollDelegate;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class RefreshableScrollView extends ScrollView implements View.OnTouchListener, ScrollDelegate
{
	private boolean canRefresh = true;
	public RefreshDelegate refreshDelegate;

	public RefreshableScrollView(Context context)
	{
		super(context);
		init();
	}

	public RefreshableScrollView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public boolean getCanRefresh()
	{
		return this.canRefresh;
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
		return getScrollY() <= 100;
	}

	@Override public boolean canStartRefreshing()
	{
		return isScrolledToTop();
	}

	public void onRefreshComplete()
	{
		refreshDelegate.onRefreshComplete();
	}

	@Override public final boolean onTouch(View view, MotionEvent event)
	{
		if (canRefresh)
		{
			refreshDelegate.onTouch(view, event);
		}

		return false;
	}

	public void setOnOverScrollListener(OnOverScrollListener l)
	{
		refreshDelegate.setOnOverScrollListener(l);
	}

	public void startRefresh()
	{
		refreshDelegate.startRefresh();
	}

	@Override public void onResetTouch(){}
}