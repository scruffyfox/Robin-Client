package in.lib.view;

import in.lib.manager.SettingsManager;
import in.lib.utils.Dimension;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.ImageView;

public class ScrollableImageView extends ImageView implements HeadedListView.OnScrollListener
{
	private HeadedListView mListParent;
	private View mContainerView;
	private boolean mCalculateScroll = true;
	private int mImageHeight = 0;
	private int mScaledImageHeight = 0;
	private Dimension mDimension;
	private int mMaxScrollSize;

	public ScrollableImageView(Context context)
	{
		super(context);
	}

	public ScrollableImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	private void init()
	{
		mListParent = (HeadedListView)getScrollableView();
		if (mListParent != null)
		{
			mListParent.addOnScrollListener(this);
		}

		mDimension = new Dimension(getContext());
	}

	@Override public void setImageBitmap(Bitmap bm)
	{
		super.setImageBitmap(bm);
		mCalculateScroll = false;

		if (bm != null)
		{
			mCalculateScroll = true;
			mImageHeight = bm.getHeight();

			double ratio = (double)getWidth() / (double)bm.getWidth();
			mScaledImageHeight = (int)(bm.getHeight() * ratio);
		}
	}

	@Override protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();

		if (mListParent != null)
		{
			mListParent.removeOnScrollListener(this);
		}
	}

	@Override protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();

		init();
	}

	/**
	 * Look for HeadedListView
	 * @return
	 */
	private View getScrollableView()
	{
		ViewParent v = getParent();
		while (true)
		{
			if (v == null) break;

			if (v.getParent() instanceof HeadedListView)
			{
				mContainerView = (View)v;
			}

			if (v instanceof HeadedListView)
			{
				return (View)v;
			}

			v = v.getParent();
		}

		return null;
	}

	@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		if (mContainerView != null && mImageHeight > 0 && mImageHeight > getHeight() + 50 && mCalculateScroll && SettingsManager.isInlineAnimationEnabled())
		{
			int[] pos = new int[2];
			getLocationInWindow(pos);
			mMaxScrollSize = (mScaledImageHeight - getHeight()) / 2;

			if (pos[1] < mDimension.getScreenHeight() && pos[1] + getHeight() > 0)
			{
				int y = (pos[1] - (int)(mScaledImageHeight / 1.5)) + (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? (int)(mDimension.getScreenHeight() / 1.5) : 0);
				scrollTo(0, y);

				if (y < -mMaxScrollSize)
				{
					scrollTo(0, -mMaxScrollSize);
				}

				if (y + getHeight() > -mMaxScrollSize + mScaledImageHeight)
				{
					scrollTo(0, mMaxScrollSize);
				}
			}
		}
	}

	@Override public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){}
	@Override public void onScrollStateChanged(AbsListView view, int scrollState){}
}