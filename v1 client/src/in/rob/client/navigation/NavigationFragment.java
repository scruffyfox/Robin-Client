package in.rob.client.navigation;

import in.lib.Constants;
import in.lib.adapter.AccountAdapter;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.event.ProfileUpdatedEvent;
import in.lib.helper.BusHelper;
import in.lib.manager.SettingsManager;
import in.lib.manager.UpdateManager;
import in.lib.manager.UserManager;
import in.lib.utils.Views;
import in.lib.view.HintedAspectRatioImageButton;
import in.model.User;
import in.rob.client.AuthenticateActivity;
import in.rob.client.ChannelsActivity;
import in.rob.client.DraftsActivity;
import in.rob.client.MainActivity;
import in.rob.client.MainApplication;
import in.rob.client.MainApplication.ApplicationType;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.SearchActivity;
import in.rob.client.SettingsActivity;
import in.rob.client.StarredActivity;
import in.rob.client.base.RobinFragment;
import in.rob.client.dialog.AboutDialog;
import in.rob.client.dialog.base.DialogBuilder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.squareup.otto.Subscribe;

public class NavigationFragment extends RobinFragment implements View.OnClickListener
{
	@OnClick @InjectView(R.id.user_button) public HintedAspectRatioImageButton mUserButton;
	@OnClick @InjectView(R.id.home_button) public HintedAspectRatioImageButton mHomeButton;
	@OnClick @InjectView(R.id.mentions_button) public HintedAspectRatioImageButton mMentionsButton;
	@OnClick @InjectView(R.id.global_button) public HintedAspectRatioImageButton mGlobalButton;
	@OnClick @InjectView(R.id.drafts_button) public HintedAspectRatioImageButton mDraftsButton;
	@OnClick @InjectView(R.id.pm_button) public HintedAspectRatioImageButton mMessagesButton;
	@OnClick @InjectView(R.id.hash_button) public HintedAspectRatioImageButton mHashButton;
	@OnClick @InjectView(R.id.profile_button) public HintedAspectRatioImageButton mProfileButton;
	@OnClick @InjectView(R.id.starred_button) public HintedAspectRatioImageButton mStarredButton;
	@OnClick @InjectView(R.id.settings_button) public HintedAspectRatioImageButton mSettingsButton;
	@OnClick @InjectView(R.id.update_button) public HintedAspectRatioImageButton mUpdateButton;
	@OnClick @InjectView(R.id.about_button) public HintedAspectRatioImageButton mAboutButton;

	private View mRootView;
	private Context mContext;

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mContext = getActivity();
		BusHelper.getInstance().register(this);
	}

	@Override public void onDestroy()
	{
		super.onDestroy();
		BusHelper.getInstance().unregister(this);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mRootView = inflater.inflate(R.layout.navigation_layout, null);
		Views.inject(this, mRootView);

		return mRootView;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (((MainApplication)mContext.getApplicationContext()).getApplicationType() == ApplicationType.BETA)
		{
			mUpdateButton.setVisibility(View.VISIBLE);
		}

		if (!SettingsManager.isGlobalEnabled())
		{
			mGlobalButton.setVisibility(View.GONE);
		}

		loadAvatar();
	}

	@Subscribe public void onProfileUpdated(ProfileUpdatedEvent event)
	{
		loadAvatar();
	}

	public void loadAvatar()
	{
		User u = UserManager.getUser();
		if (u != null)
		{
			ImageLoader.getInstance().displayImage(u.getAvatarUrl() + "?avatar=1&id=" + u.getId(), mUserButton, MainApplication.getAvatarImageOptions());
		}
	}

	@Override public void onClick(View v)
	{
		if (v == mUserButton)
		{
			List<String> users = UserManager.getLinkedUserIds(getContext());
			ArrayList<User> loadedUsers = new ArrayList<User>();

			for (int index = 0; index < users.size(); index++)
			{
				User u = User.loadUser(users.get(index));

				if (u != null)
				{
					loadedUsers.add(u);
				}
			}

			if (loadedUsers.size() < 1)
			{
				loadedUsers.add(UserManager.getUser());
			}

			DialogBuilder.create(getContext())
				.setTitle(getString(R.string.select_account))
				.setAdapter(new AccountAdapter(getContext(), loadedUsers), new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						UserManager.selectUser(getContext(), which);
					}
				})
				.setPositiveButton(R.string.add_account, new OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						Intent loginIntent = new Intent(getContext(), AuthenticateActivity.class);
						loginIntent.putExtra(Constants.EXTRA_NEW_USER, true);
						startActivity(loginIntent);
					}
				})
				.setNegativeButton(R.string.close, null)
				.show();
		}

		boolean closeMenu = false;
		if (getActivity() instanceof MainActivity)
		{
			if (v == mHomeButton)
			{
				closeMenu = true;
				((MainActivity)getActivity()).setPage(0);
			}
			else if (v == mUpdateButton)
			{
				Intent updateIntent = new Intent(mContext, UpdateManager.class);
				startActivity(updateIntent);
			}
			else if (v == mMentionsButton)
			{
				closeMenu = true;
				((MainActivity)getActivity()).setPage(1);
			}
			else if (v == mGlobalButton)
			{
				closeMenu = true;
				((MainActivity)getActivity()).setPage(2);
			}
		}
		else
		{
			if (v == mHomeButton)
			{
				closeMenu = true;
				Intent homeIntent = new Intent(mContext, MainActivity.class);
				homeIntent.putExtra(Constants.EXTRA_START_PAGE, 0);
				homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(homeIntent);
			}
			else if (v == mMentionsButton)
			{
				closeMenu = true;
				Intent homeIntent = new Intent(mContext, MainActivity.class);
				homeIntent.putExtra(Constants.EXTRA_START_PAGE, 1);
				homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(homeIntent);
			}
			else if (v == mGlobalButton)
			{
				closeMenu = true;
				Intent homeIntent = new Intent(mContext, MainActivity.class);
				homeIntent.putExtra(Constants.EXTRA_START_PAGE, 2);
				homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(homeIntent);
			}
		}

		if (closeMenu && getActivity() instanceof SlidingFragmentActivity)
		{
			((SlidingFragmentActivity)getActivity()).showAbove();
		}

		if (v == mProfileButton)
		{
			Intent profileIntent = new Intent(mContext, ProfileActivity.class);
			profileIntent.putExtra(Constants.EXTRA_USER, UserManager.getUser());
			startActivity(profileIntent);
		}
		if (v == mMessagesButton)
		{
			Intent messagesIntent = new Intent(mContext, ChannelsActivity.class);
			startActivity(messagesIntent);
		}
		else if (v == mStarredButton)
		{
			Intent profileIntent = new Intent(mContext, StarredActivity.class);
			profileIntent.putExtra(Constants.EXTRA_USER_ID, UserManager.getUserId());
			startActivityForResult(profileIntent, Constants.REQUEST_PROFILE);
		}
		else if (v == mSettingsButton)
		{
			Intent settingsIntent = new Intent(mContext, SettingsActivity.class);
			getActivity().startActivityForResult(settingsIntent, Constants.REQUEST_SETTINGS);
		}
		else if (v == mHashButton)
		{
			Intent searchIntent = new Intent(mContext, SearchActivity.class);
			getActivity().startActivity(searchIntent);
		}
		else if (v == mAboutButton)
		{
			Intent dialog = new Intent(mContext, AboutDialog.class);
			getActivity().startActivity(dialog);
		}
		else if (v == mDraftsButton)
		{
			Intent drafts = new Intent(mContext, DraftsActivity.class);
			getActivity().startActivity(drafts);
		}
	}
}