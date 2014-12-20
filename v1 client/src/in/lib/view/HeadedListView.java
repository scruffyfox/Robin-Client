package in.lib.view;

import in.lib.manager.SettingsManager;
import in.lib.utils.Dimension;
import in.rob.client.R;

import java.util.ArrayList;
import java.util.List;

import net.callumtaylor.swipetorefresh.view.RefreshableListView;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public class HeadedListView extends RefreshableListView implements OnScrollListener
{
	private View mHeaderView;
	private ImageView mHeaderImage;
	private int mScrollState;
	private PauseOnScrollListener pauseListener;

	private List<OnScrollListener> mOnScrollListener;

	private int mMaxImageHeight = 200;

	public interface OnScrollListener
	{
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
		public void onScrollStateChanged(AbsListView view, int scrollState);
		public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
	}

	public HeadedListView(Context context)
	{
		super(context);
		init();
	}

	public HeadedListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	private void init()
	{
		Dimension dimen = new Dimension(getContext());
		if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			mMaxImageHeight = (int)dimen.getHeightFromRatio(170);
		}
		else
		{
			mMaxImageHeight = (int)dimen.getHeightFromRatio(230);
		}

		mOnScrollListener = new ArrayList<OnScrollListener>();

		setHeaderView(R.layout.headed_listview_header_view);
		pauseListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
		setOnScrollListener(this);
	}

	public int getScrollState()
	{
		return mScrollState;
	}

	public void addOnScrollListener(OnScrollListener l)
	{
		this.mOnScrollListener.add(l);
	}

	public void removeOnScrollListener(OnScrollListener l)
	{
		this.mOnScrollListener.remove(l);
	}

	public void setOnScrollListener(OnScrollListener l)
	{
		this.mOnScrollListener.clear();
		this.mOnScrollListener.add(l);
	}

	public void setHeaderView(View v)
	{
		mHeaderView = v;
		mHeaderImage = (ImageView)mHeaderView.findViewById(R.id.header_image);
		if (mHeaderImage == null)
		{
			throw new IllegalArgumentException("You must have an ImageView with the id @id/header_image");
		}

		mHeaderImage.setAdjustViewBounds(true);
		addHeaderView(mHeaderView, null, false);
	}

	public void setHeaderView(int res)
	{
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setHeaderView(inflater.inflate(res, null));
	}

	public void setHeaderImage(int res)
	{
		mHeaderImage.setImageResource(res);
		recalcSizes();
	}

	public void setHeaderImage(Bitmap image)
	{
		mHeaderImage.setImageBitmap(image);
		recalcSizes();
	}

	public void setHeaderUrl(String url)
	{
		if (mHeaderImage instanceof ImageView)
		{
			DisplayImageOptions opts = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.showImageOnLoading(R.drawable.default_cover)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.resetViewBeforeLoading(true)
				.showImageForEmptyUri(R.drawable.default_cover)
			.build();

			ImageLoader.getInstance().displayImage(url, mHeaderImage, opts, new ImageLoadingListener()
			{
				@Override public void onLoadingComplete(String arg0, View arg1, Bitmap arg2)
				{
					recalcSizes();
				}

				@Override public void onLoadingCancelled(String arg0, View arg1)
				{
					recalcSizes();
				}

				@Override public void onLoadingFailed(String arg0, View arg1, FailReason arg2)
				{
					recalcSizes();
				}

				@Override public void onLoadingStarted(String arg0, View arg1)
				{
				}
			});
		}
	}

	private void recalcSizes()
	{
		if (mHeaderImage != null && mHeaderImage.getDrawable() != null)
		{
			double ratio = (double)mHeaderImage.getDrawable().getIntrinsicWidth() / (double)mHeaderImage.getDrawable().getIntrinsicHeight();
			int newWidth = getMeasuredWidth();
			int newHeight = (int)Math.round(newWidth / ratio);

			mHeaderImage.setMinimumHeight(mMaxImageHeight);
			mHeaderImage.setMaxHeight(mMaxImageHeight);

			ViewGroup.LayoutParams lp = mHeaderImage.getLayoutParams();
			lp.height = mMaxImageHeight;
			mHeaderImage.setLayoutParams(lp);
			mHeaderImage.setScaleType(newHeight < mMaxImageHeight ? ScaleType.FIT_XY : ScaleType.CENTER_CROP);
		}
	}

	@Override protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		if (changed)
		{
			recalcSizes();
		}

		super.onLayout(changed, l, t, r, b);
	}

	@Override public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		pauseListener.onScrollStateChanged(view, scrollState);

		mScrollState = scrollState;
		if (mOnScrollListener != null)
		{
			for (OnScrollListener l : mOnScrollListener)
			{
				l.onScrollStateChanged(view, scrollState);
			}
		}
	}

	@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		pauseListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

		if (mOnScrollListener != null)
		{
			for (OnScrollListener l : mOnScrollListener)
			{
				l.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		}

		if (mHeaderImage != null && SettingsManager.isCoverImageAnimationEnabled())
		{
			float yTop = mHeaderView.getTop();
			if (yTop > 0)
			{
				return;
			}

			mHeaderImage.scrollTo(0, (int)(yTop * 0.4));
		}
	}
}