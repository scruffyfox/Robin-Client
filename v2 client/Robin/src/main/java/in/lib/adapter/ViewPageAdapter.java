package in.lib.adapter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import in.lib.Constants;
import in.lib.utils.Views;
import in.rob.client.R;
import in.rob.client.fragment.base.StreamFragment;
import lombok.Getter;

public class ViewPageAdapter extends FragmentStatePagerAdapter implements OnPageChangeListener
{
	@Getter private Context context;
	@Getter private FragmentManager manager;
	private SparseArray<Fragment> fragments;
	private ArrayList<Class<? extends Fragment>> fragmentClasses;
	private ArrayList<Bundle> bundles;
	private ViewPager viewPager;
	private LinearLayout indicatorContainer;
	private View indicator;
	private TextView title;
	private int index = 0;

	public ViewPageAdapter(Activity context, FragmentManager manager, ViewPager pager)
	{
		super(manager);
		this.context = context;
		this.manager = manager;
		this.viewPager = pager;
		this.bundles = new ArrayList<Bundle>(2);
		this.fragmentClasses = new ArrayList<Class<? extends Fragment>>(2);
		this.fragments = new SparseArray<Fragment>(3);
		this.indicatorContainer = Views.findViewById(R.id.indicator, context);
		this.setTopScrollable(context.getWindow());

		if ((context.getActionBar().getDisplayOptions() & ActionBar.DISPLAY_SHOW_CUSTOM) == ActionBar.DISPLAY_SHOW_CUSTOM)
		{
			this.title = Views.findViewById(R.id.title, context.getActionBar().getCustomView());
		}

		if (pager != null)
		{
			pager.setOnPageChangeListener(this);
		}
	}

	@Override public Parcelable saveState()
	{
		Bundle state = (Bundle)super.saveState();
		state.putInt("index", index);
		return state;
	}

	@Override public void restoreState(Parcelable state, ClassLoader loader)
	{
		super.restoreState(state, loader);
		if (state != null)
		{
			Bundle bundle = (Bundle)state;
			bundle.setClassLoader(loader);
			Parcelable[] fss = bundle.getParcelableArray("states");
			fragments.clear();

			Iterable<String> keys = bundle.keySet();
			for (String key : keys)
			{
				if (key.startsWith("f"))
				{
					int index = Integer.parseInt(key.substring(1));
					Fragment f = manager.getFragment(bundle, key);
					if (f != null)
					{
						f.setMenuVisibility(false);
						fragments.put(index, f);
					}
				}
			}

			this.index = bundle.getInt("index");
			onPageSelected(index);
		}
	}

	public void addPage(Class<? extends Fragment> fragment, Bundle args)
	{
		this.fragmentClasses.add(fragment);
		this.bundles.add(args);
		this.indicatorContainer.post(new Runnable()
		{
			@Override public void run()
			{
				updateIndicator();
			}
		});
	}

	public void setTitle(Class<? extends Fragment> fragment, String title)
	{
		int index = fragmentClasses.indexOf(fragment);

		if (index > -1)
		{
			Bundle args = bundles.get(index);

			if (args != null)
			{
				args.putString(Constants.EXTRA_TITLE, title);
			}
		}

		onPageSelected(this.index);
	}

	private void updateIndicator()
	{
		if (this.indicatorContainer != null)
		{
			this.indicatorContainer.removeAllViewsInLayout();
			this.indicatorContainer.removeAllViews();

			int count = this.fragmentClasses.size();
			indicator = LayoutInflater.from(getContext()).inflate(R.layout.action_bar_tab, this.indicatorContainer, false);
			indicator.setLayoutParams(new LayoutParams(this.indicatorContainer.getMeasuredWidth() / count, ViewGroup.LayoutParams.MATCH_PARENT));

			this.indicatorContainer.addView(indicator);
			this.indicatorContainer.scrollTo(0, 0);

			if (count <= 1)
			{
				this.indicatorContainer.setVisibility(View.GONE);
			}
			else
			{
				this.indicatorContainer.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override public Fragment getItem(int index)
	{
		Fragment fragment = Fragment.instantiate(getContext(), fragmentClasses.get(index).getName(), bundles.get(index));
		return fragment;
	}

	@Override public Object instantiateItem(ViewGroup container, int position)
	{
		Fragment fragment = (Fragment)super.instantiateItem(container, position);
		fragments.put(position, fragment);
		return fragment;
	}

	@Override public void destroyItem(ViewGroup container, int position, Object object)
	{
		fragments.remove(position);
		super.destroyItem(container, position, object);
	}

	/**
	 * Gets the fragment at the current position. This
	 * is <b>not</b> the same as {@link #getItem(int)} as
	 * it does not instantiate a new fragment on request.
	 *
	 * @param position The position to query
	 * @return The fragment if found, or null
	 */
	public Fragment getItemAt(int position)
	{
		return fragments.get(position);
	}

	public Fragment getCurrentFragment()
	{
		return fragments.get(viewPager.getCurrentItem());
	}

	@Override public void onPageScrolled(int index, float positionOffset, int positionOffsetPixels)
	{
		if (this.indicatorContainer != null && indicator != null)
		{
			this.indicatorContainer.scrollTo(-(int)(indicator.getMeasuredWidth() * (positionOffset + index)), 0);
		}
	}

	@Override public void onPageSelected(int index)
	{
		this.index = index;

		if (this.indicatorContainer != null && indicator != null)
		{
			this.indicatorContainer.scrollTo(-(indicator.getMeasuredWidth() * index), 0);
		}

		if (this.title != null)
		{
			this.title.setText(getPageTitle(index));
		}

		resetRefreshables();
		setRefreshable(index);
	}

	@Override public void onPageScrollStateChanged(int state)
	{

	}

	@Override public int getCount()
	{
		return fragmentClasses.size();
	}

	@Override public CharSequence getPageTitle(int position)
	{
		Bundle bundle = bundles.get(position);
		if (bundle != null && bundle.containsKey(Constants.EXTRA_TITLE))
		{
			String title = bundle.getString(Constants.EXTRA_TITLE, "");
			return title;
		}

		return "";
	}

	/**
	 * Hides all the refreshables for the non-visible fragments
	 */
	public void resetRefreshables()
	{
		for (int index = 0, size = getCount(); index < size; index++)
		{
			Fragment frag = getItemAt(index);
			if (frag instanceof StreamFragment && ((StreamFragment)frag).getRefreshHelper() != null)
			{
				((StreamFragment)frag).getRefreshHelper().hideHelper();
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
		if (frag instanceof StreamFragment && ((StreamFragment)frag).getRefreshHelper() != null)
		{
			if (((StreamFragment)frag).isLoading())
			{
				((StreamFragment)frag).getRefreshHelper().showHelper();
			}
		}
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
		}
	}
}
