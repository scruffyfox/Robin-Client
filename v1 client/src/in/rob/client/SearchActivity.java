package in.rob.client;

import in.lib.Constants;
import in.lib.adapter.PhonePageAdapter;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.page.SearchPage;

import java.util.LinkedHashMap;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class SearchActivity extends RobinSlidingActivity
{
	@Override public void setup(boolean isPhone)
	{
		Bundle extras = new Bundle();
		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>(1);

		Bundle bundle1 = new Bundle(extras);
		bundle1.putString(Constants.EXTRA_TITLE, getString(R.string.search));
		pages.put(SearchPage.class, bundle1);

		PhonePageAdapter adapter = new PhonePageAdapter(getContext(), getSupportFragmentManager(), getViewPager(), pages, getSupportActionBar().getCustomView());
		adapter.setIndicatorVisible(false);
		getViewPager().setAdapter(adapter);
		setAdapter(adapter);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(android.view.MenuItem item)
	{
		if (item.getItemId() == R.id.menu_search)
		{
			((SearchPage)getAdapter().getCurrentFragment()).search();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}