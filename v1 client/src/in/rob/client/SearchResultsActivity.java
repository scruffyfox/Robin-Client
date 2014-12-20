package in.rob.client;

import in.lib.Constants;
import in.lib.adapter.PhonePageAdapter;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.dialog.NewPostDialog;
import in.rob.client.page.TagSearchPage;
import in.rob.client.page.UserSearchPage;

import java.util.LinkedHashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class SearchResultsActivity extends RobinSlidingActivity
{
	private String mTag = "";

	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		if (instances != null && instances.containsKey(Constants.EXTRA_TAG_NAME))
		{
			mTag = instances.getString(Constants.EXTRA_TAG_NAME);
		}
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putString(Constants.EXTRA_TAG_NAME, mTag);
		super.onSaveInstanceState(outState);
	}

	@Override public void setup(boolean isPhone)
	{
		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>(2);
		boolean userSearch = mTag.startsWith("@");
		mTag = mTag.replace("%40", "").replace("@", "").replace("##", "#");

		Bundle bundle1 = new Bundle();
		bundle1.putString(Constants.EXTRA_TITLE, mTag);
		bundle1.putString(Constants.EXTRA_TAG_NAME, mTag);

		Bundle bundle2 = new Bundle();
		bundle2.putString(Constants.EXTRA_TITLE, getString(R.string.at) + mTag.replace("#", ""));
		bundle2.putString(Constants.EXTRA_TAG_NAME, mTag);

		if (userSearch)
		{
			pages.put(UserSearchPage.class, bundle2);
			pages.put(TagSearchPage.class, bundle1);
		}
		else
		{
			pages.put(TagSearchPage.class, bundle1);
			pages.put(UserSearchPage.class, bundle2);
		}

		PhonePageAdapter adapter = new PhonePageAdapter(getContext(), getSupportFragmentManager(), getViewPager(), pages, getSupportActionBar().getCustomView());
		getViewPager().setAdapter(adapter);
		setAdapter(adapter);

		if (!isPhone)
		{
			getAdapter().setIndicatorVisible(false);
		}
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(android.view.MenuItem item)
	{
		if (item.getItemId() == R.id.menu_new_post)
		{
			Intent newPost = new Intent(this, NewPostDialog.class);
			newPost.putExtra(Constants.EXTRA_TAG_NAME, mTag);
			startActivity(newPost);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}