package in.lib.adapter;

import in.lib.Constants;
import in.lib.manager.SettingsManager;
import in.lib.utils.Dimension;
import in.lib.view.JazzyViewPager;
import in.lib.view.JazzyViewPager.TransitionEffect;
import in.rob.client.R;
import in.rob.client.base.RobinListFragment;
import in.rob.client.page.base.StreamFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.TextView;

@SuppressWarnings("rawtypes") public class PhonePageAdapter extends FragmentPagerAdapter implements OnPageChangeListener
{
	private ArrayList<Class> classes;
	private ArrayList<Bundle> bundles;

	private final JazzyViewPager mViewPager;
	private final FragmentManager manager;
	private final Context mContext;
	private final List<String> mTitles;
	private OnPageChangeListener mOnPageChangeListener;

	@Getter @Setter private int index = 0;
	private ViewGroup mIndicator;
	private TextView mTitle, mTitle2;
	private final View mCustomView;

	private int mIndicatorViewWidth;

	public static LinkedHashMap<Class, Bundle> listToMap(List<Class> list)
	{
		LinkedHashMap<Class, Bundle> fragmentHash = new LinkedHashMap<Class, Bundle>();
		for (Class c : list)
		{
			fragmentHash.put(c, null);
		}

		return fragmentHash;
	}

	@Override public Object instantiateItem(ViewGroup container, final int position)
	{
		Object obj = super.instantiateItem(container, position);
		mViewPager.setObjectForPosition(obj, position);
		return obj;
	}

	/**
	 * Default constructor
	 *
	 * @param fm The fragment manager for all transactions
	 * @param pager The viewpager tied with the adapter
	 */
	public PhonePageAdapter(Context ctx, FragmentManager fm, ViewPager pager, List<Class> fragments, View absCustomView)
	{
		this(ctx, fm, pager, listToMap(fragments), absCustomView);
	}

	@Override public float getPageWidth(int position)
	{
		if ((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
			(mContext.getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE &&
			getCount() > 1
		)
		{
			return 0.5f;
		}

		return super.getPageWidth(position);
	}

	/**
	 * Default constructor
	 *
	 * @param fm The fragment manager for all transactions
	 * @param pager The viewpager tied with the adapter
	 */
	public PhonePageAdapter(Context ctx, FragmentManager fm, ViewPager pager, LinkedHashMap<Class, Bundle> fragments, View absCustomView)
	{
		super(fm);

		this.manager = fm;
		this.mContext = ctx;
		this.mViewPager = (JazzyViewPager)pager;
		this.mViewPager.setOnPageChangeListener(this);
		this.mCustomView = absCustomView;
		setPages(fragments);
		setAnimation();
		updateViewIndicator();

		if (mIndicator != null && this.classes.size() > 0)
		{
			switchPage(0);
		}
		else if (this.classes.size() < 1)
		{
			setIndicatorVisible(false);
		}

		mTitles = new ArrayList<String>(fragments.keySet().size());
		Iterator<Class> i = fragments.keySet().iterator();
		while (i.hasNext())
		{
			mTitles.add(fragments.get(i.next()).getString(Constants.EXTRA_TITLE));
		}
	}

	public void setAnimation()
	{
		if (((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
		&&	(mContext.getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE
		&&	this.classes.size() > 1)
		|| SettingsManager.getAllocatedMemory() < 16
		|| !SettingsManager.isPaginationAnimationEnabled())
		{
			mViewPager.setTransitionEffect(TransitionEffect.Standard);
		}
		else
		{
			mViewPager.setTransitionEffect(TransitionEffect.Tablet);
		}
	}

	public void setPages(HashMap<Class, Bundle> fragments)
	{
		if (fragments.keySet().size() == fragments.values().size())
		{
			classes = new ArrayList<Class>(fragments.keySet());
			bundles = new ArrayList<Bundle>(fragments.values());
		}
		else
		{
			classes = new ArrayList<Class>(fragments.keySet().size());
			bundles = new ArrayList<Bundle>(fragments.keySet().size());

			Iterator<Class> iterator = fragments.keySet().iterator();
			while (iterator.hasNext())
			{
				Class c = iterator.next();
				classes.add(c);
				bundles.add(fragments.get(c) == null ? new Bundle() : fragments.get(c));
			}
		}

		int count = this.classes.size();
		if ((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
			(mContext.getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE &&
			count <= 2
		)
		{
			mViewPager.setPagingEnabled(false);
		}
		else
		{
			mViewPager.setPagingEnabled(true);
		}
	}

	public void updateViewIndicator()
	{
		if (mCustomView != null)
		{
			mTitle = (TextView)mCustomView.findViewById(R.id.title);
			mTitle2 = (TextView)mCustomView.findViewById(R.id.title2);
			mIndicator = (ViewGroup)((Activity)mContext).findViewById(R.id.indicator);
			mIndicator.removeAllViewsInLayout();
			mIndicator.removeAllViews();

//			if (this.index > this.classes.size() - 1)
//			{
//				this.index = this.classes.size() - 1;
//			}

			int count = this.classes.size();
			if ((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
				(mContext.getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE &&
				count > 1
			)
			{
				count = (int)Math.ceil(count / 2.0d);
			}

			View indicator = LayoutInflater.from(mContext).inflate(R.layout.action_bar_tab, mIndicator, false);

			Dimension d = new Dimension(mContext);
			int width = d.getScreenWidth() / count;

			indicator.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));

			mIndicator.addView(indicator);
			mIndicator.scrollTo(index * width, 0);
			mIndicatorViewWidth = width;
		}
	}

	public int getIndex()
	{
		return index;
	}

	/**
	 * @param visible True to set the tab indicater, false to set it invisible
	 */
	public void setIndicatorVisible(boolean visible)
	{
		if (mIndicator != null)
		{
			mIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	/**
	 * @param title The new title of the activity
	 */
	public void setTitle(String title)
	{
		mTitle.setText(title);
	}

	/**
	 * @param title The new title of the activity
	 */
	public void setTitle2(String title)
	{
		mTitle2.setText(title);
	}

	/**
	 * Sets the on page change listener
	 * @param l The new listener
	 */
	public void setOnPageChangeListener(OnPageChangeListener l)
	{
		mOnPageChangeListener = l;
	}

	@Override public int getCount()
	{
		return this.classes.size();
	}

	@Override public Fragment getItem(int position)
	{
		Fragment f = Fragment.instantiate(mContext, this.classes.get(position).getName(), this.bundles.get(position));
		return f;
	}

	/**
	 * Gets the fragment at the current position. This
	 * is <b>not</b> the same as {@link getItem(int)} as
	 * it does not instantiate a new fragment on request.
	 *
	 * @param position The position to query
	 * @return The fragment if found, or null
	 */
	public Fragment getItemAt(int position)
	{
		return manager.findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + position);
	}

	@Override public void onPageScrollStateChanged(int scrollState)
	{
		mIndicator.scrollTo(-(index * mIndicatorViewWidth), 0);

		if (mOnPageChangeListener != null)
		{
			mOnPageChangeListener.onPageScrollStateChanged(scrollState);
		}
	}

	@Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
	{
		int count = this.classes.size();
		if ((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
			(mContext.getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE &&
			count > 1
		)
		{
			count = (int)Math.ceil(count / 2.0d);
		}

		int targetOffset = positionOffsetPixels / count;
		mIndicator.scrollTo(-((mIndicatorViewWidth * position) + targetOffset), 0);

		// Call sublistener
		if (mOnPageChangeListener != null)
		{
			mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	/**
	 * Called when the page is changed
	 * @param index The index of the current fragment in view
	 */
	@Override public void onPageSelected(int index)
	{
		this.index = index;
		if (mOnPageChangeListener != null)
		{
			mOnPageChangeListener.onPageSelected(index);
		}

		if (mIndicator != null)
		{
			switchPage(index);
		}

		resetRefreshables();
		setRefreshable(index);
	}

	/**
	 * Hides all the refreshables for the non-visible fragments
	 */
	public void resetRefreshables()
	{
		for (int index = 0; index < getCount(); index++)
		{
			Fragment frag = getItemAt(index);
			if (frag instanceof RobinListFragment && ((RobinListFragment)frag).getRefreshHelper() != null)
			{
				((RobinListFragment)frag).getRefreshHelper().hideHelper();
			}
		}
	}

	/**
	 * Shows the indeterminate refreshable for the specific
	 * index
	 * @param index
	 */
	public void setRefreshable(int index)
	{
		Fragment frag = getItemAt(index);
		if (frag instanceof RobinListFragment && ((RobinListFragment)frag).getRefreshHelper() != null)
		{
			((RobinListFragment)frag).getRefreshHelper().showHelper();
		}
	}

	/**
	 * Switches the page to the requested index
	 * @param index The index of the new page to display
	 */
	private void switchPage(int index)
	{
		mIndicator.scrollTo(-(index * mIndicatorViewWidth), 0);

		if ((mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
		&& (mContext.getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE
		&& this.classes.size() > 1)
		{
			index = (int)Math.ceil(index / 2.0d);

			if (index + 1 < this.bundles.size())
			{
				mTitle2.setText(this.bundles.get(index + 1).getCharSequence("title"));
				mTitle2.setVisibility(View.VISIBLE);
			}

			mTitle.setText(this.bundles.get(index).getCharSequence("title"));
		}
		else
		{
			mTitle2.setVisibility(View.GONE);
			mTitle.setText(this.bundles.get(index).getCharSequence("title"));
		}
	}

	@Override public CharSequence getPageTitle(int position)
	{
		return mTitles.get(position).toUpperCase();
	}

	/**
	 * Gets the current fragment in view
	 *
	 * @return The current fragment
	 */
	public Fragment getCurrentFragment()
	{
		return getItemAt(index);
	}

	@Override public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();

		updateViewIndicator();
	}

	public void setTopScrollable(Window window)
	{
		if ((window.getDecorView().findViewById(R.id.title) != null))
		{
			window.getDecorView().findViewById(R.id.title).setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					if (getCurrentFragment() instanceof StreamFragment)
					{
						((StreamFragment)getCurrentFragment()).scrollToTop();
					}
				}
			});

			window.getDecorView().findViewById(R.id.title2).setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					if (getItemAt(getIndex() + 1) instanceof StreamFragment)
					{
						((StreamFragment)getItemAt(getIndex() + 1)).scrollToTop();
					}
				}
			});
		}
	}
}