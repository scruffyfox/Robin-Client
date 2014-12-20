package in.rob.client.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import in.controller.adapter.UserAdapter;
import in.controller.handler.UserStreamResponseHandler;
import in.data.stream.UserStream;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.utils.Views.Injectable;
import in.model.AdnModel;
import in.model.User;
import in.rob.client.ProfileActivity;
import in.rob.client.fragment.base.StreamFragment;
import lombok.Getter;

@Injectable
public class SearchUserFragment extends StreamFragment
{
	@Getter private String searchTerm;

	@Override public void setupAdapter()
	{
		setAdapter(new UserAdapter(getContext()));
	}

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null && arguments.containsKey(Constants.EXTRA_SEARCH_TERM))
		{
			searchTerm = arguments.getString(Constants.EXTRA_SEARCH_TERM);
		}
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		if (append)
		{
			// User search doesn't have pagination
			return;
		}

		UserStreamResponseHandler response = new UserStreamResponseHandler(append);
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
		APIManager.getInstance().searchUsers(searchTerm, lastId, response);
	}

	@Override public void onListItemClick(AdnModel model)
	{
		User item = (User)model;
		Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
		profileIntent.putExtra(Constants.EXTRA_USER, (Parcelable)item);
		getActivity().startActivity(profileIntent);
	}

	@Override public Class getCacheClass()
	{
		return UserStream.class;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			String.format(Constants.RESPONSE_SEARCH_USER, searchTerm)
		};
	}
}
