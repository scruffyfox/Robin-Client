package in.rob.client.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import in.controller.adapter.ProfilePostAdapter;
import in.controller.handler.PostStreamResponseHandler;
import in.data.stream.PostStream;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.manager.UserManager;
import in.lib.utils.Views.Injectable;
import in.model.User;
import in.rob.client.R;
import in.rob.client.base.BaseActivity;
import in.view.holder.ProfileHeaderHolder;
import lombok.Getter;

@Injectable
public class ProfileFragment extends TimelineFragment
{
	@Getter private User user;
	private ProfileHeaderHolder header;

	@Override public void setupAdapter()
	{
		setAdapter(new ProfilePostAdapter(getContext(), user.getId()));
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (getUser() != null)
		{
			header.populate(getUser());
			((BaseActivity)getActivity()).getPageAdapter().setTitle(ProfileFragment.class, "@" + user.getUsername());
		}
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		if (user != null)
		{
			outState.putParcelable(Constants.EXTRA_USER, user);
		}

		super.onSaveInstanceState(outState);
	}

	@Override public void setupHeaders()
	{
		View headerView = LayoutInflater.from(getContext()).inflate(R.layout.profile_header_stub, getListView(), false);
		header = new ProfileHeaderHolder(headerView);
		getListView().addHeaderView(headerView, null, false);
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
		PostStreamResponseHandler response = new PostStreamResponseHandler(append);
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
		APIManager.getInstance().getUserStream(getUser().getId(), lastId, response);
	}

	@Override public Class getCacheClass()
	{
		return PostStream.class;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			String.format(Constants.RESPONSE_USER_STREAM, getUser().getId())
		};
	}

	@Override public String getCacheKey()
	{
		return String.format(Constants.CACHE_USER_STREAM, getUser().getId());
	}
}
