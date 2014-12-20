package in.rob.client;

import in.lib.Constants;
import in.lib.adapter.PhonePageAdapter;
import in.lib.helper.ThemeHelper;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.view.JazzyViewPager;
import in.lib.view.JazzyViewPager.TransitionEffect;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.dialog.NewPostDialog;
import in.rob.client.navigation.NavigationFragment;
import in.rob.client.page.GlobalPage;
import in.rob.client.page.MentionsPage;
import in.rob.client.page.TimelinePage;
import in.rob.client.widget.RobinDashClockExtension;

import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.slidingmenu.lib.SlidingMenu;

public class MainActivity extends RobinSlidingActivity
{
	private int mStartPage = 0;
	private boolean mForceRefreshMentions = false;

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		try
		{
			int menuRes = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_icon_menu);
			((ImageButton)getSupportActionBar().getCustomView().findViewById(R.id.up_button)).setImageResource(menuRes);
			getSupportActionBar().getCustomView().findViewById(R.id.up_button).setContentDescription(getString(R.string.nav_menu));
		}
		catch (Exception e){}
	}

	@Override protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		retrieveArguments(intent.getExtras());
	}

	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		if (instances != null)
		{
			if (instances.containsKey(Constants.EXTRA_SELECT_USER))
			{
				List<String> users = UserManager.getLinkedUserIds(getContext());
				String selectUser = instances.getString(Constants.EXTRA_SELECT_USER);
				for (int index = 0; index < users.size(); index++)
				{
					if (users.get(index).equals(selectUser))
					{
						if (!UserManager.getUserId().equals(selectUser))
						{
							instances.remove(Constants.EXTRA_SELECT_USER);

							UserManager.selectUser(getContext(), index, false);
							Intent main = new Intent(getContext(), MainActivity.class);
							main.putExtras(instances);
							main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
							main.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(main);
							return;
						}

						break;
					}
				}
			}

			if (instances.getBoolean(Constants.EXTRA_OPEN_THREAD, false))
			{
				Intent threadDetails = new Intent(getContext(), ThreadActivity.class);
				threadDetails.putExtras(instances);
				startActivity(threadDetails);

				SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
				prefs.edit()
					.remove(Constants.PREFS_NOTIFICATION_ID + UserManager.getUserId())
					.remove(Constants.PREFS_NOTIFICATION_COUNT + UserManager.getUserId())
				.apply();

				CacheManager.getInstance().removeFile(Constants.PREFS_NOTIFICATION_PREVIEW_LINES + UserManager.getUserId());
			}

			if (instances.containsKey(Constants.EXTRA_START_PAGE))
			{
				mStartPage = instances.getInt(Constants.EXTRA_START_PAGE);

				SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
				prefs.edit()
					.remove(Constants.PREFS_NOTIFICATION_ID + UserManager.getUserId())
					.remove(Constants.PREFS_NOTIFICATION_COUNT + UserManager.getUserId())
				.apply();

				CacheManager.getInstance().removeFile(Constants.PREFS_NOTIFICATION_PREVIEW_LINES + UserManager.getUserId());

				if (getAdapter() != null && getViewPager() != null)
				{
					TransitionEffect effect = ((JazzyViewPager)getViewPager()).getEffect();
					((JazzyViewPager)getViewPager()).setTransitionEffect(TransitionEffect.Standard);
					getViewPager().setCurrentItem(mStartPage, true);
					((JazzyViewPager)getViewPager()).setTransitionEffect(effect);
					getAdapter().onPageSelected(mStartPage);

					showAbove();
				}
			}

			if (instances.containsKey(Constants.EXTRA_CLEAR_DASH))
			{
				Intent service = new Intent(getContext(), RobinDashClockExtension.class);
				getContext().startService(service);
				getContext().stopService(service);
			}

			mForceRefreshMentions = instances.getBoolean(Constants.EXTRA_FORCE_REFRESH, false);
		}
	}

	@Override public void setup(boolean isPhone)
	{
		PhonePageAdapter adapter = new PhonePageAdapter(getContext(), getSupportFragmentManager(), getViewPager(), createPages(), getSupportActionBar().getCustomView());
		setAdapter(adapter);
		getViewPager().setAdapter(adapter);
		getViewPager().setCurrentItem(mStartPage);

		if (!isPhone && getAdapter().getCount() <= 2)
		{
			getAdapter().setIndicatorVisible(false);
		}
	}

	public LinkedHashMap<Class, Bundle> createPages()
	{
		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>(3);

		Bundle bundle1 = new Bundle();
		bundle1.putString(Constants.EXTRA_TITLE, getString(R.string.timeline));
		pages.put(TimelinePage.class, bundle1);
		Bundle bundle2 = new Bundle();
		bundle2.putString(Constants.EXTRA_TITLE, getString(R.string.mentions));
		bundle2.putBoolean(Constants.EXTRA_FORCE_REFRESH, mForceRefreshMentions);
		pages.put(MentionsPage.class, bundle2);

		if (SettingsManager.isGlobalEnabled())
		{
			Bundle bundle3 = new Bundle();
			bundle3.putString(Constants.EXTRA_TITLE, getString(R.string.global));
			pages.put(GlobalPage.class, bundle3);
		}

		return pages;
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == Constants.REQUEST_SETTINGS)
		{
			if (resultCode == Constants.RESULT_REFRESH && data.hasExtra(Constants.EXTRA_REFRESH_GLOBAL))
			{
				LinkedHashMap<Class, Bundle> pages = createPages();

				NavigationFragment nav = (NavigationFragment)getSupportFragmentManager().findFragmentById(R.id.frame);
				if (SettingsManager.isGlobalEnabled())
				{
					nav.mGlobalButton.setVisibility(View.VISIBLE);
					getAdapter().setIndicatorVisible(true);
				}
				else
				{
					nav.mGlobalButton.setVisibility(View.GONE);

					if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
					&& (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE))
					{
						getAdapter().setIndicatorVisible(false);
					}
				}

				getAdapter().setPages(pages);
				getAdapter().notifyDataSetChanged();

				return;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return true;
	}

	@Override public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU && !handledLongPress && (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE)
		{
			toggle(SlidingMenu.LEFT);
			return true;
		}

		handledLongPress = false;
		return super.onKeyUp(keyCode, event);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_new_post:
			{
				Intent in = new Intent(this, NewPostDialog.class);
				startActivityForResult(in, Constants.REQUEST_NEW_POST);
				break;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override public void onClick(View v)
	{
		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
		&& (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		)
		{
			return;
		}

		if (v.getId() == R.id.up_button)
		{
			toggle(SlidingMenu.LEFT);
		}
	}
}