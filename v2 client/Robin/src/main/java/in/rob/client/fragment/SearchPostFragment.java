package in.rob.client.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import in.controller.adapter.PostAdapter;
import in.controller.handler.PostStreamResponseHandler;
import in.data.stream.PostStream;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.utils.Views.Injectable;
import in.model.AdnModel;
import in.model.Post;
import in.rob.client.ThreadActivity;
import in.rob.client.fragment.base.StreamFragment;
import lombok.Getter;

@Injectable
public class SearchPostFragment extends StreamFragment
{
	@Getter private String searchTerm;

	@Override public void setupAdapter()
	{
		setAdapter(new PostAdapter(getContext()));
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
		PostStreamResponseHandler response = new PostStreamResponseHandler(append);
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
		APIManager.getInstance().searchPosts(searchTerm, lastId, response);
	}

	@Override public void onListItemClick(AdnModel model)
	{
		Post item = (Post)model;
		Intent threadIntent = new Intent(getContext(), ThreadActivity.class);
		threadIntent.putExtra(Constants.EXTRA_POST, (Parcelable)item);
		getActivity().startActivity(threadIntent);
	}

	@Override public Class getCacheClass()
	{
		return PostStream.class;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			String.format(Constants.RESPONSE_SEARCH_POST, searchTerm)
		};
	}
}
