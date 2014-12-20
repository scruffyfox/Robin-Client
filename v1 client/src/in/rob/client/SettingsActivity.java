package in.rob.client;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.utils.Dimension;
import in.lib.utils.Views;
import in.lib.view.JazzyViewPager;
import in.lib.view.JazzyViewPager.TransitionEffect;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.dialog.base.DialogBuilder;
import in.rob.client.page.AdditionalSettingsPage;
import in.rob.client.page.AppearanceSettingsPage;
import in.rob.client.page.GeneralSettingsPage;
import in.rob.client.page.NotificationSettingsPage;
import in.rob.client.page.ProfileSettingsPage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slidingmenu.lib.SlidingMenu;
import com.viewpagerindicator.TitlePageIndicator;

public class SettingsActivity extends RobinSlidingActivity
{
	@InjectView(R.id.pager) public JazzyViewPager mViewPager;
	@Getter @Setter private boolean restartRequired = false;

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		lockOrientation();
		Views.inject(this);

		((TextView)getSupportActionBar().getCustomView().findViewById(R.id.title)).setText(R.string.settings);
	}

	@Override public void setupForPhone()
	{
		setup(true);
	}

	@Override public void onClick(View v)
	{
		if (restartRequired)
		{
			showRestartDialog();
			return;
		}

		super.onClick(v);
	}

	@Override public void onBackPressed()
	{
		if (restartRequired)
		{
			showRestartDialog();
			return;
		}

		super.onBackPressed();
	}

	public void showRestartDialog()
	{
		DialogBuilder.create(getContext())
			.setTitle(R.string.restart_required_title)
			.setMessage(R.string.restart_required_desc)
			.setPositiveButton(R.string.yes, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					finish();

					Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			})
			.setNegativeButton(R.string.no, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					finish();
				}
			})
		.show();
	}

	@Override public void setup(boolean isPhone)
	{
		// Calculate the size for the sliding menu
		Dimension dimension = new Dimension(this);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			getSlidingMenu().setBehindWidth((int)dimension.getWidthFromRatio(100), SlidingMenu.LEFT);
		}
		else
		{
			getSlidingMenu().setBehindWidth((int)dimension.getWidthFromRatio(70), SlidingMenu.LEFT);
		}

		getSlidingMenu().setBehindScrollScale(0.2f, SlidingMenu.BOTH);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>(5);

		Bundle b1 = new Bundle();
		b1.putString(Constants.EXTRA_TITLE, getString(R.string.profile));
		pages.put(ProfileSettingsPage.class, b1);

		Bundle b2 = new Bundle();
		b2.putString(Constants.EXTRA_TITLE, getString(R.string.general));
		pages.put(GeneralSettingsPage.class, b2);

		Bundle b5 = new Bundle();
		b5.putString(Constants.EXTRA_TITLE, getString(R.string.notifications));
		pages.put(NotificationSettingsPage.class, b5);

		Bundle b3 = new Bundle();
		b3.putString(Constants.EXTRA_TITLE, getString(R.string.appearance));
		pages.put(AppearanceSettingsPage.class, b3);

		Bundle b4 = new Bundle();
		b4.putString(Constants.EXTRA_TITLE, getString(R.string.additional));
		pages.put(AdditionalSettingsPage.class, b4);

		getViewPager().setAdapter(new TestFragmentAdapter(pages, getSupportFragmentManager()));

		//Bind the title indicator to the adapter
		TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
		titleIndicator.setViewPager(getViewPager());
		titleIndicator.setVisibility(View.VISIBLE);

		if (getIntent().getExtras() != null)
		{
			titleIndicator.setCurrentItem(getIntent().getExtras().getInt(Constants.EXTRA_START_PAGE, 1));
		}
		else
		{
			titleIndicator.setCurrentItem(1);
		}

		((JazzyViewPager)getViewPager()).setTransitionEffect(TransitionEffect.Standard);
		ViewGroup mIndicator = (ViewGroup)findViewById(R.id.indicator);
		mIndicator.setVisibility(View.GONE);
	}

	public void lockOrientation()
	{
		switch (getResources().getConfiguration().orientation)
		{
			case Configuration.ORIENTATION_PORTRAIT:
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO)
				{
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
				else
				{
					int rotation = getWindowManager().getDefaultDisplay().getRotation();
					if (rotation == android.view.Surface.ROTATION_90 || rotation == android.view.Surface.ROTATION_180)
					{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
					}
					else
					{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					}
				}
			break;

			case Configuration.ORIENTATION_LANDSCAPE:
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO)
				{
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
				else
				{
					int rotation = getWindowManager().getDefaultDisplay().getRotation();
					if (rotation == android.view.Surface.ROTATION_0 || rotation == android.view.Surface.ROTATION_90)
					{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					}
					else
					{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
					}
				}
			break;
		}
	}

	@Override public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		// menu button
		if (keyCode == KeyEvent.KEYCODE_MENU && !handledLongPress)
		{
			toggle(SlidingMenu.LEFT);
			return true;
		}

		handledLongPress = false;
		return super.onKeyUp(keyCode, event);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.empty, menu);
		return super.onCreateOptionsMenu(menu);
	}

	class TestFragmentAdapter extends FragmentPagerAdapter
	{
		private final List<String> mTitles;
		private final LinkedHashMap<Class, Bundle> mFragments;
		private final FragmentManager mFragmentManager;

		@Override public Object instantiateItem(ViewGroup container, final int position)
		{
			Object obj = super.instantiateItem(container, position);
			mViewPager.setObjectForPosition(obj, position);
			return obj;
		}

		public TestFragmentAdapter(LinkedHashMap<Class, Bundle> pages, FragmentManager fm)
		{
			super(fm);

			mFragments = pages;
			mFragmentManager = fm;

			mTitles = new ArrayList<String>();
			Iterator<Class> i = mFragments.keySet().iterator();
			while (i.hasNext())
			{
				mTitles.add(mFragments.get(i.next()).getString(Constants.EXTRA_TITLE));
			}
		}

		@Override public Fragment getItem(int position)
		{
			Class[] keys = mFragments.keySet().toArray(new Class[mFragments.size()]);
			return Fragment.instantiate(getContext(), keys[position].getName(), mFragments.get(keys[position]));
		}

		@Override public int getCount()
		{
			return mFragments.size();
		}

		@Override public CharSequence getPageTitle(int position)
		{
			return mTitles.get(position).toUpperCase();
		}
	}
}