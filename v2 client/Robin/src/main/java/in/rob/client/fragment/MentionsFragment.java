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
import in.lib.manager.UserManager;
import in.lib.utils.Views.Injectable;
import in.model.AdnModel;
import in.model.Post;
import in.model.User;
import in.rob.client.ThreadActivity;
import in.rob.client.fragment.base.StreamFragment;
import lombok.Getter;

@Injectable
public class MentionsFragment extends StreamFragment
{
	@Getter private User user;

	@Override public void setupAdapter()
	{
		setAdapter(new PostAdapter(getContext(), user.getId()));
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
		APIManager.getInstance().getUserMentions(user.getId(), lastId, response);
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
			String.format(Constants.RESPONSE_MENTIONS, user.getId())
		};
	}

	@Override public String getCacheKey()
	{
		return String.format(Constants.CACHE_MENTIONS, user.getId());
	}
}
