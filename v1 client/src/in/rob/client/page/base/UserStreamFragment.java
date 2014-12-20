package in.rob.client.page.base;

import in.lib.Constants;
import in.lib.adapter.UserAdapter;
import in.lib.event.FollowUserEvent;
import in.lib.event.UnFollowUserEvent;
import in.model.SimpleUser;
import in.model.User;
import in.model.base.NetObject;
import in.rob.client.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.squareup.otto.Subscribe;

public abstract class UserStreamFragment extends StreamFragment
{
	@Override public void onDataReady()
	{

	}

	@Override public void retrieveArguments(Bundle arguments)
	{

	}

	@Override public void setupAdapters()
	{
		if (getAdapter() == null)
		{
			setAdapter(new UserAdapter(getContext(), new ArrayList<User>()));
		}
		else
		{
			setAdapter(getAdapter());
		}
	}

	/**
	 * Loops through posts and builds a list of users and tags to use in auto suggest
	 */
	public void extractUsersAndTags(List<NetObject> list)
	{
		List<SimpleUser> users = getCacheManager().readFileAsObject(Constants.CACHE_USERNAMES, new ArrayList<SimpleUser>());
		List<String> usersStr = getCacheManager().readFileAsObject(Constants.CACHE_USERNAMES_STR, new ArrayList<String>());

		if (list != null)
		{
			for (NetObject o : list)
			{
				User p = (User)o;

				if (!usersStr.contains(p.getId()))
				{
					users.add(SimpleUser.parseFromUser(p));
					usersStr.add(p.getId());
				}
			}
		}

		getCacheManager().asyncWriteFile(Constants.CACHE_USERNAMES, users);
		getCacheManager().asyncWriteFile(Constants.CACHE_USERNAMES_STR, usersStr);
	}

	@Override public void checkAdapterSizes()
	{
		super.checkAdapterSizes();

		if (getAdapter().getCount() > 7)
		{
			getListView().removeFooterView(getPaddingView());
		}
	}

	@Override public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
	{
		User u = (User)getAdapter().getItem(position - getHeadedListView().getHeaderViewsCount());
		Intent intent = new Intent(getContext(), ProfileActivity.class);
		intent.putExtra(Constants.EXTRA_USER, u);
		startActivity(intent);
	}

	/**************************************************
	 *
	 *  OTTO EVENTS
	 *
	 **************************************************/

	/**
	 * Called when a user is followed
	 *
	 * @param event The follow event with the user object
	 */
	@Subscribe public void onFollowUser(FollowUserEvent event)
	{
		if (getAdapter() != null)
		{
			User user = event.getUser();
			user.save();
		}
	}

	/**
	 * Called when a user is unfollowed
	 *
	 * @param event The unfollow event with the user object
	 */
	@Subscribe public void onUnFollowUser(UnFollowUserEvent event)
	{
		if (getAdapter() != null)
		{
			User user = event.getUser();
			user.save();
		}
	}
}