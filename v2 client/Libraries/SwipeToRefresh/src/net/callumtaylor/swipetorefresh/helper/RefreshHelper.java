package net.callumtaylor.swipetorefresh.helper;

import android.app.Activity;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.callumtaylor.pulltorefresh.R;
import net.callumtaylor.swipetorefresh.view.OnOverScrollListener;
import net.callumtaylor.swipetorefresh.view.RefreshableListView;
import net.callumtaylor.swipetorefresh.view.RefreshableScrollView;

/**
 * This is the refresh helper class which you could call to
 * wrap your refreshable views and activity to.
 *
 * You can call {@link RefreshHelper#showHelper()} to show the indeterminate progress
 * or {@link RefreshHelper#hideHelper()} to hide. This is useful when having more than one
 * refreshable list fragments in a view pager, call show/hide on the
 * relevant fragment when switching page to prevent multiple refreshables
 * from showing.
 */
public class RefreshHelper implements OnOverScrollListener
{
	private final View ptrOverlay;

	private final ProgressBar ptrProgressBar, ptrIndeterminateProgressBar;
	private final AccelerateInterpolator accelerationInterpolator;
	private final View abRoot;
	private OnRefreshListener refreshListener;

	private RefreshableListView listView;
	private RefreshableScrollView scrollView;

	private boolean refreshing = false;
	private Runnable reset = new Runnable()
	{
		@Override public void run()
		{
			if (ptrOverlay.getAnimation() == null)
			{
				resetOverlay();
			}
		}
	};

	private RefreshHelper(View overlay, View progressOverlay, View root)
	{
		this.ptrOverlay = overlay;
		this.ptrIndeterminateProgressBar = (ProgressBar)progressOverlay;
		this.accelerationInterpolator = new AccelerateInterpolator();
		this.ptrProgressBar = (ProgressBar)overlay.findViewById(R.id.refresh_progress);
		this.abRoot = root;

		ptrProgressBar.setMax(0);
		ptrProgressBar.setMax(100);
		ptrProgressBar.setProgress(0);
	}

	public void setRefreshing(boolean refreshing)
	{
		this.refreshing = refreshing;
	}

	public boolean isRefreshing()
	{
		return refreshing;
	}

	public void hideHelper()
	{
		if (isRefreshing())
		{
			ptrIndeterminateProgressBar.setVisibility(View.GONE);
		}
	}

	public void showHelper()
	{
		if (isRefreshing())
		{
			ptrIndeterminateProgressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override public void onBeginRefresh()
	{
		onRefreshScrolledPercentage(1.0f);
		AnimationHelper.pullRefreshActionBar(ptrOverlay, abRoot);
	}

	/**
	 * Alias for {@link RefreshHelper#onRefresh()}
	 */
	public void refresh()
	{
		onRefresh();
	}

	@Override public void onRefresh()
	{
		if (scrollView != null)
		{
			scrollView.setCanRefresh(false);
		}

		if (listView != null)
		{
			listView.setCanRefresh(false);
		}

		refreshing = true;
		ptrProgressBar.setVisibility(View.GONE);
		ptrIndeterminateProgressBar.setVisibility(View.VISIBLE);
		((TextView)ptrOverlay.findViewById(R.id.refresh_text)).setText(R.string.ptr_refreshing);

		ptrOverlay.postDelayed(reset, 800);

		if (refreshListener != null)
		{
			refreshListener.onRefresh();
		}
	}

	@Override public void onRefreshScrolledPercentage(float percentage)
	{
		ptrProgressBar.setVisibility(View.VISIBLE);
		ptrProgressBar.setProgress(Math.round(accelerationInterpolator.getInterpolation(percentage) * 100));
	}

	/**
	 * Alias for {@link RefreshHelper#onReset()}
	 */
	public void finish()
	{
		onReset();
	}

	/**
	 * You may call this method after the refreshable method has completed
	 * to reset the ptr functionality
	 */
	@Override public void onReset()
	{
		refreshing = false;
		if (ptrIndeterminateProgressBar.getVisibility() == View.VISIBLE)
		{
			AnimationHelper.fadeOut(ptrIndeterminateProgressBar);
		}

		ptrOverlay.removeCallbacks(reset);
		resetOverlay();

		if (scrollView != null)
		{
			scrollView.setCanRefresh(true);
			scrollView.onRefreshComplete();
		}

		if (listView != null)
		{
			listView.setCanRefresh(true);
			listView.onRefreshComplete();
		}
	}

	private void resetOverlay()
	{
		if (ptrOverlay.getVisibility() == View.VISIBLE)
		{
			AnimationHelper.pullRefreshActionBarCancel(ptrOverlay, abRoot);
			ptrProgressBar.setVisibility(View.GONE);
			ptrProgressBar.setProgress(0);

			ptrOverlay.postDelayed(new Runnable()
			{
				@Override public void run()
				{
					((TextView)ptrOverlay.findViewById(R.id.refresh_text)).setText(R.string.ptr_pull);
				}
			}, 400);
		}
		else
		{
			((TextView)ptrOverlay.findViewById(R.id.refresh_text)).setText(R.string.ptr_pull);
		}
	}

	public void setOnRefreshListener(OnRefreshListener l)
	{
		this.refreshListener = l;
	}

	public void setRefreshableListView(RefreshableListView l)
	{
		if (l != null)
		{
			this.listView = l;
			this.listView.setOnOverScrollListener(this);
		}
	}

	public void setRefreshableScrollView(RefreshableScrollView l)
	{
		if (l != null)
		{
			this.scrollView = l;
			this.scrollView.setOnOverScrollListener(this);
		}
	}

	private static View findActionBar(Window w)
	{
		return getFirstChildByClassName((ViewGroup)w.getDecorView(), "com.android.internal.widget.ActionBarContainer");
	}

	private static View getFirstChildByClassName(ViewGroup parent, String name)
	{
		View retView = null;
		int childCount = parent.getChildCount();

		for (int childIndex = 0; childIndex < childCount; childIndex++)
		{
			View child = parent.getChildAt(childIndex);

			if (child.getClass().getName().equals(name))
			{
				return child;
			}

			if (child instanceof ViewGroup)
			{
				View v = getFirstChildByClassName((ViewGroup)child, name);

				if (v != null)
				{
					return v;
				}
			}
		}

		return retView;
	}

	private static <T extends View> T getFirstChildByInstance(ViewGroup parent, Class<T> instance)
	{
		View retView = null;
		int childCount = parent.getChildCount();

		for (int childIndex = 0; childIndex < childCount; childIndex++)
		{
			View child = parent.getChildAt(childIndex);

			if (instance.isAssignableFrom(child.getClass()))
			{
				return instance.cast(child);
			}

			if (child instanceof ViewGroup)
			{
				View v = getFirstChildByInstance((ViewGroup)child, instance);

				if (v != null)
				{
					return instance.cast(v);
				}
			}
		}

		return instance.cast(retView);
	}

	/**
	 * You can call this at the start of your activity to reset any current set refresh bar
	 * @param ctx
	 */
	public static void reset(Activity ctx)
	{
		ViewGroup abRoot = null;

		int id = ctx.getResources().getIdentifier("action_bar_container", "id", ctx.getPackageName());

		if (id > 0)
		{
			abRoot = (ViewGroup)ctx.getWindow().getDecorView().findViewById(id);
		}

		if (id == 0 || abRoot == null)
		{
			abRoot = (ViewGroup)findActionBar(ctx.getWindow());
		}

		if (abRoot != null)
		{
			View view = abRoot.findViewById(R.id.refresh_view);
			while (view != null)
			{
				abRoot.removeView(view);
				view = abRoot.findViewById(R.id.refresh_view);
			}

			View indeterminate = abRoot.findViewById(R.id.refresh_progress_indeterminate);
			while (indeterminate != null)
			{
				abRoot.removeView(indeterminate);
				indeterminate = abRoot.findViewById(R.id.refresh_progress_indeterminate);
			}
		}
	}

	public static RefreshHelper wrapRefreshable(Activity ctx, RefreshableListView list, OnRefreshListener l)
	{
		RefreshHelper helper = wrapRefreshable(ctx, l);
		helper.setRefreshableListView(list);
		return helper;
	}

	public static RefreshHelper wrapRefreshable(Activity ctx, RefreshableScrollView scroll, OnRefreshListener l)
	{
		RefreshHelper helper = wrapRefreshable(ctx, l);
		helper.setRefreshableScrollView(scroll);
		return helper;
	}

	public static RefreshHelper wrapRefreshable(Activity ctx, RefreshableListView list, RefreshableScrollView scroll, OnRefreshListener l)
	{
		RefreshHelper helper = wrapRefreshable(ctx, l);
		helper.setRefreshableListView(list);
		helper.setRefreshableScrollView(scroll);
		return helper;
	}

	public static RefreshHelper wrapRefreshable(Fragment fragment, OnRefreshListener l)
	{
		RefreshHelper helper = wrapRefreshable(fragment.getActivity(), l);

		RefreshableListView list = getFirstChildByInstance((ViewGroup)fragment.getView(), RefreshableListView.class);
		RefreshableScrollView scroll = getFirstChildByInstance((ViewGroup)fragment.getView(), RefreshableScrollView.class);
		helper.setRefreshableListView(list);
		helper.setRefreshableScrollView(scroll);
		return helper;
	}

	public static RefreshHelper wrapRefreshable(View view, OnRefreshListener l)
	{
		RefreshHelper helper = wrapRefreshable((Activity)view.getContext(), l);

		RefreshableListView list = getFirstChildByInstance((ViewGroup)view, RefreshableListView.class);
		RefreshableScrollView scroll = getFirstChildByInstance((ViewGroup)view, RefreshableScrollView.class);
		helper.setRefreshableListView(list);
		helper.setRefreshableScrollView(scroll);
		return helper;
	}

	public static RefreshHelper wrapRefreshable(Activity ctx, OnRefreshListener l)
	{
		ViewGroup abRoot;

		if ((abRoot = (ViewGroup)ctx.getWindow().getDecorView().findViewById(R.id.action_bar_container)) == null)
		{
			abRoot = (ViewGroup)findActionBar(ctx.getWindow());
		}

		if (abRoot != null)
		{
			View overlay = LayoutInflater.from(ctx).inflate(R.layout.abs_overlay, abRoot, false);
			abRoot.addView(overlay);

			View progressOverlay = LayoutInflater.from(ctx).inflate(R.layout.abs_overlay_progress, abRoot, false);
			abRoot.addView(progressOverlay);

			RefreshHelper helper = new RefreshHelper(overlay, progressOverlay, abRoot.getChildAt(0));
			helper.setOnRefreshListener(l);
			return helper;
		}

		return null;
	}

	public interface OnRefreshListener
	{
		public void onRefresh();
	}
}