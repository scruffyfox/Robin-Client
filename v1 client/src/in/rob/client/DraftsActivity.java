package in.rob.client;

import in.lib.Constants;
import in.lib.adapter.PhonePageAdapter;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.dialog.NewPostDialog;
import in.rob.client.page.DraftsPage;

import java.util.LinkedHashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class DraftsActivity extends RobinSlidingActivity
{
	@Override public void setup(boolean isPhone)
	{
		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>();
		Bundle extras = new Bundle();

		Bundle bundle1 = new Bundle(extras);
		bundle1.putString(Constants.EXTRA_TITLE, getString(R.string.drafts));
		pages.put(DraftsPage.class, bundle1);

		PhonePageAdapter adapter = new PhonePageAdapter(this, getSupportFragmentManager(), getViewPager(), pages, getSupportActionBar().getCustomView());
		setAdapter(adapter);
		getViewPager().setAdapter(adapter);
		getAdapter().setIndicatorVisible(false);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(android.view.MenuItem item)
	{
		if (item == null) return false;

		if (item.getItemId() == android.R.id.home)
		{
			finish();
			return true;
		}

		if (item.getItemId() == R.id.menu_new_post)
		{
			Intent in = new Intent(this, NewPostDialog.class);
			startActivity(in);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}