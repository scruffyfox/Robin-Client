package in.rob.client;

import in.lib.Constants;
import in.lib.adapter.PhonePageAdapter;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.page.PostRepostsPage;
import in.rob.client.page.PostStarsPage;

import java.util.LinkedHashMap;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;

public class PostUserListActivity extends RobinSlidingActivity
{
	private ViewPager mViewPager;
	private PhonePageAdapter mPageAdapter;
	private String mPostId;

	@Override public void setupForPhone()
	{
		super.setupForPhone();

		if (getIntent().getExtras() != null)
		{
			mViewPager.setCurrentItem(getIntent().getExtras().getInt(Constants.EXTRA_START_TAB, 0));
		}
	}

	@Override public void setup(boolean isPhone)
	{
		Bundle extras = new Bundle();
		extras.putString(Constants.EXTRA_POST_ID, mPostId);
		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>(2);

		Bundle repostsPage = new Bundle(extras);
		repostsPage.putCharSequence(Constants.EXTRA_TITLE, getString(R.string.reposted_by));
		pages.put(PostRepostsPage.class, repostsPage);
		Bundle starredPage = new Bundle(extras);
		starredPage.putCharSequence(Constants.EXTRA_TITLE, getString(R.string.starred_by));
		pages.put(PostStarsPage.class, starredPage);

		PhonePageAdapter adapter = new PhonePageAdapter(this, getSupportFragmentManager(), getViewPager(), pages, getSupportActionBar().getCustomView());
		mViewPager.setAdapter(adapter);
		setAdapter(adapter);

		if (!isPhone)
		{
			getAdapter().setIndicatorVisible(false);
		}
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.empty, menu);
		return true;
	}
}