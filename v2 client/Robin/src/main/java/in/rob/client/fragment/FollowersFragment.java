package in.rob.client.fragment;

import android.os.Bundle;

import in.controller.adapter.UserAdapter;
import in.controller.handler.UserStreamResponseHandler;
import in.data.stream.UserStream;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.manager.UserManager;
import in.lib.utils.Views.Injectable;
import in.model.User;
import in.rob.client.fragment.base.StreamFragment;
import lombok.Getter;

@Injectable
public class FollowersFragment extends StreamFragment
{
	@Getter private User user;

	@Override public void setupAdapter()
	{
		setAdapter(new UserAdapter(getContext()));
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		if (user != null)
		{
			outState.putParcelable(Constants.EXTRA_USER, user);
		}

		super.onSaveInstanceState(outState);
	}

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null && arguments.containsKey(Constants.EXTRA_USER))
		{
			user = (User)arguments.getParcelable(Constants.EXTRA_USER);
		}
		else
		{
			user = UserManager.getInstance().getUser();
		}
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		UserStreamResponseHandler response = new UserStreamResponseHandler(append);
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
		APIManager.getInstance().getFollowers(user.getId(), lastId, response);
	}

	@Override public Class getCacheClass()
	{
		return UserStream.class;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			String.format(Constants.RESPONSE_FOLLOWERS, user.getId())
		};
	}

	@Override public String getCacheKey()
	{
		return String.format(Constants.CACHE_FOLLOWERS, user.getId());
	}
}
