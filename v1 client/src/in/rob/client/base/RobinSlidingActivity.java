package in.rob.client.base;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.adapter.PhonePageAdapter;
import in.lib.annotation.InjectView;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.Dimension;
import in.lib.utils.Views;
import in.rob.client.AuthenticateActivity;
import in.rob.client.MainApplication;
import in.rob.client.R;
import in.rob.client.SearchActivity;
import in.rob.client.navigation.NavigationFragment;
import in.rob.client.page.base.StreamFragment;

import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import com.google.analytics.tracking.android.EasyTracker;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public abstract class RobinSlidingActivity extends SlidingFragmentActivity implements OnClickListener, SensorEventListener
{
	@Getter @InjectView(R.id.pager) public ViewPager viewPager;

	@Getter private Context context;
	@Getter private NavigationFragment leftNavigation;
	@Getter @Setter private PhonePageAdapter adapter;
	protected boolean handledLongPress = false;
	private int pagerIndex = 0;

	private SensorManager sensorManager;
	private final int SHAKE_THRESHOLD = 25;
	private final int SHAKE_COUNT = 2;
	private long lastUpdate = 0L;
	private int mShakeCount = 0;
	private float mAccel = 0.00f;
	private float mAccelCurrent = SensorManager.GRAVITY_EARTH;
	private float mAccelLast = SensorManager.GRAVITY_EARTH;

	public static final int SLIDING_MENU_PORTRAIT_WIDTH = 100;
	public static final int SLIDING_MENU_LANDSACPE_WIDTH = 70;

	/**
	 * Initializes the custom action bar
	 * @param ctx
	 */
	protected static void initActionBar(final RobinSlidingActivity ctx)
	{
		ctx.getSupportActionBar().setCustomView(R.layout.action_bar);
		ctx.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ctx.getSupportActionBar().setDisplayShowCustomEnabled(true);
		ctx.getSupportActionBar().setDisplayShowHomeEnabled(false);
		ctx.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		ctx.getSupportActionBar().setHomeButtonEnabled(false);
		ctx.getSupportActionBar().setDisplayUseLogoEnabled(false);

		// sliding menu custom toggle
		View upBtn = ctx.getSupportActionBar().getCustomView().findViewById(R.id.up_button);
		upBtn.setOnClickListener(ctx);
	}

	@Override public void setTitle(CharSequence title)
	{
		getAdapter().setTitle(title.toString());
	}

	public void setTitle2(CharSequence title)
	{
		getAdapter().setTitle2(title.toString());
	}

	@Override public void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);
		setLocale();
		setStyle();

		this.context = this;
		this.sensorManager = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);
		RobinSlidingActivity.initActionBar(this);

		if (!UserManager.isLoggedIn())
		{
			Intent login = new Intent(this, AuthenticateActivity.class);
			login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			login.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(login);
			finish();
			return;
		}

		retrieveArguments(arg0 == null ? getIntent().getExtras() : arg0);

		setContentView(R.layout.sliding_view);
		Views.inject(this);

		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
		&& (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE))
		{
			setLeftNavigationContentForTablet();
			setupForTablet();
		}
		else
		{
			setBehindLeftContentView(R.layout.navigation_fragment);
			setupForPhone();
		}

		leftNavigation = (NavigationFragment)getSupportFragmentManager().findFragmentById(R.id.frame);

		if (arg0 != null && getAdapter() != null)
		{
			pagerIndex = arg0.getInt("view_pager_index", 0);
			getAdapter().setIndex(pagerIndex);
		}
	}

	public void setLeftNavigationContentForTablet()
	{
		setBehindLeftContentView(new View(this), new LayoutParams(0, LayoutParams.MATCH_PARENT));
	}

	@Override public void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		Debug.logHeap(getClass());
	}

	/**
	 * Sets up the basic layouts for tablets by disabling sliding menu.
	 * {@link setup(false)} is called directly after this method.
	 */
	public void setupForTablet()
	{
		getSlidingMenu().setSlidingEnabled(false);
		setup(false);
	}

	/**
	 * Sets up the basic layouts for the sliding menu and listeners for phone
	 * devices. {@link setup(true)} is called <b>before</b> the page change
	 * listener is for the adapter.
	 */
	public void setupForPhone()
	{
		// Calculate the size for the sliding menu
		Dimension dimension = new Dimension(this);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			getSlidingMenu().setBehindWidth((int)dimension.getWidthFromRatio(SLIDING_MENU_PORTRAIT_WIDTH), SlidingMenu.LEFT);
		}
		else
		{
			getSlidingMenu().setBehindWidth((int)dimension.getWidthFromRatio(SLIDING_MENU_LANDSACPE_WIDTH), SlidingMenu.LEFT);
		}

		setup(true);

		getSlidingMenu().setBehindScrollScale(0.2f, SlidingMenu.BOTH);
		getAdapter().setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override public void onPageSelected(int index)
			{
				if (index == 0)
				{
					getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
				}
				else
				{
					getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
				}
			}

			@Override public void onPageScrolled(int arg0, float arg1, int arg2){}
			@Override public void onPageScrollStateChanged(int arg0){}
		});

		if (getAdapter().getIndex() == 0)
		{
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		}
		else
		{
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		}
	}

	/**
	 * Called after either setupTablet() or setupPhone() to finish the activity
	 * setup. Here, you should inflate your page adapter with the relevant pages.
	 *
	 * @param isPhone If the device is a phone, this will be true.
	 */
	public abstract void setup(boolean isPhone);

	/**
	 * Sets the style of the activity based on the current set style in user
	 * settings. Override this to prevent or supply your own styles to set.
	 */
	public void setStyle()
	{
		String styleName = SettingsManager.getThemeName();
		int styleRes = getResources().getIdentifier(styleName, "style", getPackageName());
		try
		{
			setTheme(styleRes);
		}
		catch (Exception e)
		{
			setTheme(R.style.DefaultLight);
		}
	}

	public void setLocale()
	{
		String languageToLoad = SettingsManager.getLocale();

		if (TextUtils.isEmpty(languageToLoad))
		{
			languageToLoad = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(Constants.PREFS_DEFAULT_LOCALE, "en");
		}

		Locale locale = new Locale(languageToLoad);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
	}

	/**
	 * Gets the current fragment in the page adapter
	 * @return The current shown fragment in the adapter.
	 */
	public Fragment getCurrentFragment()
	{
		return adapter.getCurrentFragment();
	}

	@Override public void onAccuracyChanged(Sensor sensor, int accuracy){}
	@Override public void onSensorChanged(SensorEvent event)
	{
		long curTime = System.currentTimeMillis();
		if ((curTime - lastUpdate) > 150 && SettingsManager.isShakeRefreshEnabled())
		{
			lastUpdate = curTime;

			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			mAccelLast = mAccelCurrent;
			mAccelCurrent = (float)Math.sqrt((x * x + y * y + z * z));
			float delta = mAccelCurrent - mAccelLast;
			mAccel = mAccel * 0.9f + delta;

			if (mAccel > SHAKE_THRESHOLD)
			{
				if (++mShakeCount >= SHAKE_COUNT
				&& getCurrentFragment() != null
				&& getCurrentFragment() instanceof StreamFragment
				&& !((StreamFragment)getCurrentFragment()).isLoading())
				{
					((StreamFragment)getCurrentFragment()).onForceRefresh();
				}
			}
		}
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		for (int index = 0, count = getAdapter().getCount(); index < count; index++)
		{
			Fragment f = (getAdapter().getItemAt(index));
			if (f != null && f instanceof RobinListFragment)
			{
				boolean result = ((RobinListFragment)f).onOptionsItemSelected(item);

				if (result)
				{
					return true;
				}
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Constants.RESULT_REFRESH)
		{
			for (int index = 0, count = getAdapter().getCount(); index < count; index++)
			{
				if (getAdapter().getItemAt(index) != null)
				{
					getAdapter().getItemAt(index).onActivityResult(requestCode, resultCode, data);
				}
			}

			// set result for potential activities before
			setResult(Constants.RESULT_REFRESH, data);
		}

		if (requestCode == Constants.REQUEST_SETTINGS)
		{
			if (resultCode == Constants.RESULT_REFRESH)
			{
				if (data.hasExtra(Constants.EXTRA_REFRESH_GLOBAL))
				{
				if (SettingsManager.isGlobalEnabled())
					{
						leftNavigation.mGlobalButton.setVisibility(View.VISIBLE);
					}
					else
					{
						leftNavigation.mGlobalButton.setVisibility(View.GONE);
					}
				}

				if (data.hasExtra(Constants.EXTRA_REFRESH_ANIMATIONS))
				{
					// TODO: Figgure out how to reset the animations in the view pager
					//getAdapter().setAnimation();
				}
			}
		}
	}

	@Override public boolean onKeyLongPress(int keyCode, KeyEvent event)
	{
		if (checkMenuKey(keyCode))
		{
			handledLongPress = true;
			startActivity(new Intent(this, SearchActivity.class));
			return true;
		}

		return super.onKeyLongPress(keyCode, event);
	}

	@Override public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (checkMenuKey(keyCode))
		{
			event.startTracking();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Checks if the menu key has been pressed.
	 *
	 * Override this and return false to <b>disable</b> the check
	 * @param keyCode
	 * @return
	 */
	public boolean checkMenuKey(int keyCode)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			return true;
		}

		return false;
	}

	@Override public void onClick(View v)
	{
		if (v.getId() == R.id.up_button)
		{
			finish();
		}
	}

	/**
	 * @return Gets the base 64 rehashable id of the device
	 */
	public String getDeviceId()
	{
		return ((MainApplication)getApplication()).getDeviceId();
	}

	/**
	 * Override this to get the activities instances either from the saved instances or bundle extras
	 * @param instances Bundle extras if saved instances is null, saved instances if not null
	 */
	public void retrieveArguments(Bundle instances){}

	@Override protected void onStart()
	{
		super.onStart();

		if (SettingsManager.isAnalyticsEnabled())
		{
			EasyTracker.getInstance().activityStart(this);
		}

		Sensor s = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		this.sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override protected void onStop()
	{
		super.onStop();

		if (SettingsManager.isAnalyticsEnabled())
		{
			EasyTracker.getInstance().activityStop(this);
		}

		Sensor s = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		this.sensorManager.unregisterListener(this, s);
	}

	@Override public void onAttachedToWindow()
	{
		super.onAttachedToWindow();

		if (adapter != null)
		{
			adapter.setTopScrollable(getWindow());
		}
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("view_pager_index", getViewPager().getCurrentItem());
		super.onSaveInstanceState(outState);
	}

	/**
	 * Sets the current page
	 * @param index The index of the page
	 */
	public void setPage(int index)
	{
		getViewPager().setCurrentItem(index, true);
	}
}