package in.rob.client.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import java.util.ArrayList;
import java.util.List;

import in.lib.Constants;
import in.lib.adapter.AccountAdapter;
import in.lib.builder.DialogBuilder;
import in.lib.manager.UserManager;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.utils.Views.OnClick;
import in.lib.view.AvatarImageView;
import in.model.User;
import in.rob.client.AuthenticationActivity;
import in.rob.client.ChannelsActivity;
import in.rob.client.DraftsActivity;
import in.rob.client.MainActivity;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.SearchActivity;
import in.rob.client.SettingsActivity;
import in.rob.client.StarredActivity;
import lombok.Getter;

@Injectable
public class NavigationFragment extends Fragment
{
	@InjectView private AvatarImageView avatar;
	@Getter private Context context;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.navigation_layout, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		context = getActivity();

		if (UserManager.getInstance().getUser() != null)
		{
			avatar.setUser(UserManager.getInstance().getUser(), true);
		}
	}

	@OnClick public void onAvatarClick(View view)
	{
		List<String> users = UserManager.getInstance().getLinkedUserIds();
		final ArrayList<User> loadedUsers = new ArrayList<User>();

		for (int index = 0; index < users.size(); index++)
		{
			User u = new User().load(users.get(index));

			if (u != null)
			{
				loadedUsers.add(u);
			}
		}

		if (loadedUsers.size() < 1)
		{
			loadedUsers.add(UserManager.getInstance().getUser());
		}

		DialogBuilder.create(getContext())
			.setTitle(getString(R.string.select_account))
			.setAdapter(new AccountAdapter(getContext(), loadedUsers), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (UserManager.getInstance().getUser().equals(loadedUsers.get(which)))
					{
						Intent main = new Intent(getContext(), ProfileActivity.class);
						getContext().startActivity(main);
					}
					else
					{
						UserManager.getInstance().selectUser(loadedUsers.get(which));

						Intent main = new Intent(getContext(), MainActivity.class);
						main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
						main.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getContext().startActivity(main);
					}
				}
			})
			.setPositiveButton(R.string.add_account, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					Intent loginIntent = new Intent(getContext(), AuthenticationActivity.class);
					loginIntent.putExtra(Constants.EXTRA_NEW_USER, true);
					startActivity(loginIntent);
				}
			})
			.setNegativeButton(R.string.close, null)
		.show();
	}

	@OnClick public void onTimelineClick(View view)
	{
		boolean closeMenu = false;

		if (getActivity() instanceof MainActivity)
		{
			closeMenu = true;
			((MainActivity)getActivity()).setPage(0);
		}
		else
		{
			closeMenu = true;
			Intent homeIntent = new Intent(getContext(), MainActivity.class);
			homeIntent.putExtra(Constants.EXTRA_START_PAGE, 0);
			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(homeIntent);
		}

		if (closeMenu && getActivity() instanceof SlidingFragmentActivity)
		{
			((SlidingFragmentActivity)getActivity()).showContent();
		}
	}

	@OnClick public void onMentionsClick(View view)
	{
		boolean closeMenu = false;

		if (getActivity() instanceof MainActivity)
		{
			closeMenu = true;
			((MainActivity)getActivity()).setPage(1);
		}
		else
		{
			closeMenu = true;
			Intent homeIntent = new Intent(getContext(), MainActivity.class);
			homeIntent.putExtra(Constants.EXTRA_START_PAGE, 1);
			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(homeIntent);
		}

		if (closeMenu && getActivity() instanceof SlidingFragmentActivity)
		{
			((SlidingFragmentActivity)getActivity()).showContent();
		}
	}

	@OnClick public void onChannelsClick(View view)
	{
		Intent channelsIntent = new Intent(getContext(), ChannelsActivity.class);
		getActivity().startActivity(channelsIntent);
	}

	@OnClick public void onDraftsClick(View view)
	{
		Intent draftsIntent = new Intent(getContext(), DraftsActivity.class);
		getActivity().startActivity(draftsIntent);
	}

	@OnClick public void onProfileClick(View view)
	{
		Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
		profileIntent.putExtra(Constants.EXTRA_USER, (Parcelable)UserManager.getInstance().getUser());
		getActivity().startActivity(profileIntent);
	}

	@OnClick public void onStarredClick(View view)
	{
		Intent starredIntent = new Intent(getContext(), StarredActivity.class);
		getActivity().startActivity(starredIntent);
	}

	@OnClick public void onSearchClick(View view)
	{
		Intent searchIntent = new Intent(getContext(), SearchActivity.class);
		getActivity().startActivity(searchIntent);
	}

	@OnClick public void onSettingsClick(View view)
	{
		Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
		getActivity().startActivity(settingsIntent);
	}
}
