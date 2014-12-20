package in.rob.client;

import in.lib.Constants;
import in.lib.adapter.PhonePageAdapter;
import in.model.User;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.page.StarredPage;

import java.util.LinkedHashMap;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * Activity to show a user's starred posts
 */
public class StarredActivity extends RobinSlidingActivity
{
	private String mUserId = "";

	@Override public void retrieveArguments(Bundle instances)
	{
		if (getIntent().getExtras() != null)
		{
			if (getIntent().getExtras().containsKey(Constants.EXTRA_USER_ID))
			{
				mUserId = getIntent().getExtras().getString(Constants.EXTRA_USER_ID);
			}
			else if (getIntent().getExtras().containsKey(Constants.EXTRA_USER))
			{
				mUserId = ((User)getIntent().getExtras().getParcelable(Constants.EXTRA_USER)).getId();
			}
		}
	}

	@Override public void setup(boolean isPhone)
	{
		Bundle extras = new Bundle();
		extras.putString(Constants.EXTRA_USER_ID, mUserId);

		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>(1);

		Bundle bundle1 = new Bundle(extras);
		bundle1.putString(Constants.EXTRA_TITLE, getString(R.string.starred_posts));
		pages.put(StarredPage.class, bundle1);

		PhonePageAdapter adapter = new PhonePageAdapter(getContext(), getSupportFragmentManager(), getViewPager(), pages, getSupportActionBar().getCustomView());
		adapter.setIndicatorVisible(false);
		getViewPager().setAdapter(adapter);
		setAdapter(adapter);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.empty, menu);
		return true;
	}
}