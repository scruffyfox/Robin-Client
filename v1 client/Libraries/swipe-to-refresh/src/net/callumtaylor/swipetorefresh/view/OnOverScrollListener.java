package net.callumtaylor.swipetorefresh.view;

public interface OnOverScrollListener
{
	public void onReset();
	public void onBeginRefresh();
	public void onRefreshScrolledPercentage(float percentage);
	public void onRefresh();
}