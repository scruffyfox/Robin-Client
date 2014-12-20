package in.rob.client.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.utils.Views.OnClick;
import in.rob.client.R;
import in.rob.client.fragment.base.BaseFragment;

@Injectable
public class UserFriendsFragment extends BaseFragment
{
	@InjectView private FrameLayout fragmentHolder;
	@InjectView private TextView followersButton;
	@InjectView private TextView followingButton;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.user_switch_layout, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		onFollowersButtonClick(null);
	}

	@OnClick public void onFollowersButtonClick(View v)
	{
		Fragment fragment = Fragment.instantiate(getContext(), FollowersFragment.class.getName(), getArguments());
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_holder, fragment).commit();
		followingButton.setTextColor(0xffaaaaaa);
		followersButton.setTextColor(0xff000000);
	}

	@OnClick public void onFollowingButtonClick(View v)
	{
		Fragment fragment = Fragment.instantiate(getContext(), FollowingsFragment.class.getName(), getArguments());
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_holder, fragment).commit();
		followingButton.setTextColor(0xff000000);
		followersButton.setTextColor(0xffaaaaaa);
	}
}
