package in.rob.client;

import android.os.Bundle;

import in.lib.adapter.ViewPageAdapter;
import in.lib.utils.Views.Injectable;
import in.rob.client.base.BaseActivity;
import in.rob.client.fragment.MentionsFragment;
import in.rob.client.fragment.ProfileFragment;
import in.rob.client.fragment.UserFriendsFragment;

@Injectable
public class ProfileActivity extends BaseActivity
{
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = new Bundle();

		if (getIntent().getExtras() != null)
		{
			args.putAll(getIntent().getExtras());
		}

		setPageAdapter(new ViewPageAdapter(this, getFragmentManager(), getViewPager()));
		getPageAdapter().addPage(ProfileFragment.class, args);
		getPageAdapter().addPage(MentionsFragment.class, args);
		getPageAdapter().addPage(UserFriendsFragment.class, args);
		getViewPager().setAdapter(getPageAdapter());

		// Set the initial position.
		// TODO: Handle override from Extras
		getViewPager().setCurrentItem(0);
		getPageAdapter().onPageSelected(0);
	}
}
