package in.rob.client.navigation;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.manager.UserManager;
import in.lib.utils.Views;
import in.lib.view.HintedAspectRatioImageButton;
import in.model.SimpleUser;
import in.rob.client.MutedActivity;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.StarredActivity;
import in.rob.client.dialog.NewChannelDialog;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class ProfileNavigationFragment extends Fragment implements View.OnClickListener
{
	@OnClick @InjectView(R.id.menu_follow_button) public HintedAspectRatioImageButton mFollow;
	@OnClick @InjectView(R.id.menu_followers) public HintedAspectRatioImageButton mFollowers;
	@OnClick @InjectView(R.id.menu_mentions) public HintedAspectRatioImageButton mMentions;
	@OnClick @InjectView(R.id.mute_button) public HintedAspectRatioImageButton mMute;
	@OnClick @InjectView(R.id.block_button) public HintedAspectRatioImageButton mBlock;
	@OnClick @InjectView(R.id.star_button) public HintedAspectRatioImageButton mStar;
	@OnClick @InjectView(R.id.muted_button) public HintedAspectRatioImageButton mMuted;
	@OnClick @InjectView(R.id.menu_message_button) public HintedAspectRatioImageButton mMessage;

	private View mRootView;
	private Context mContext;
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mContext = getActivity();
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.profile_navigation_layout, null);
		Views.inject(this, mRootView);

		return mRootView;
	}

	@Override public void onClick(View v)
	{
		if (getActivity() instanceof SlidingFragmentActivity)
		{
			((SlidingFragmentActivity)getActivity()).toggle(SlidingMenu.RIGHT);
		}

		if (getActivity() instanceof ProfileActivity)
		{
			if (v == mFollow)
			{
				((ProfileActivity)getActivity()).followUnfollow();
			}
			else if (v == mMessage)
			{
				if (((ProfileActivity)getActivity()).getUser() != null)
				{
					ArrayList<SimpleUser> recipients = new ArrayList<SimpleUser>();
					recipients.add(SimpleUser.parseFromUser(UserManager.getUser()));
					recipients.add(SimpleUser.parseFromUser(((ProfileActivity)getActivity()).getUser()));
					Intent messageActivity = new Intent(mContext, NewChannelDialog.class);
					messageActivity.putExtra(Constants.EXTRA_USER_LIST, recipients);
					startActivity(messageActivity);
				}
			}
			else if (v == mMute)
			{
				((ProfileActivity)getActivity()).muteUnmute();
			}
			else if (v == mBlock)
			{
				((ProfileActivity)getActivity()).blockUnblock();
			}
			else if (v == mStar)
			{
				if (getActivity() instanceof ProfileActivity)
				{
					Intent starredIntent = new Intent(mContext, StarredActivity.class);
					starredIntent.putExtra(Constants.EXTRA_USER_ID, ((ProfileActivity)getActivity()).getUser().getId());
					startActivity(starredIntent);
				}
			}
			else if (v == mMuted)
			{
				if (getActivity() instanceof ProfileActivity)
				{
					Intent mutedIntent = new Intent(mContext, MutedActivity.class);
					mutedIntent.putExtra(Constants.EXTRA_USER_ID, ((ProfileActivity)getActivity()).getUser().getId());
					startActivity(mutedIntent);
				}
			}
			else if (v == mFollowers)
			{
				if (getActivity() instanceof ProfileActivity)
				{
					((ProfileActivity)getActivity()).setPage(2);
				}
			}
			else if (v == mMentions)
			{
				if (getActivity() instanceof ProfileActivity)
				{
					((ProfileActivity)getActivity()).setPage(1);
				}
			}
		}
	}
}