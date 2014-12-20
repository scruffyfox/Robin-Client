package in.rob.client.base;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.ViewConfiguration;

import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import java.lang.reflect.Field;

import in.lib.adapter.ViewPageAdapter;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.rob.client.R;
import lombok.Getter;
import lombok.Setter;

@Injectable
public class BaseActivity extends SlidingFragmentActivity
{
	@Getter @Setter private ViewPageAdapter pageAdapter;
	@Getter @InjectView private ViewPager viewPager;

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		compatibilityCheck();

		setContentView(R.layout.main);
		Views.inject(this);

		setBehindContentView(R.layout.navigation_fragment);
		getSlidingMenu().setBehindWidth(getResources().getDimensionPixelSize(R.dimen.navigation_width));
	}

	public void onUpSelected()
	{
		try
		{
			NavUtils.navigateUpFromSameTask(this);
		}
		catch (Exception e)
		{
			finish();
		}
	}

	private void compatibilityCheck()
	{
		try
		{
			// Force the overflow menu to show for ActioBar
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			
			if (menuKeyField != null)
			{
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		}
		catch (Exception ex){}
	}
}
