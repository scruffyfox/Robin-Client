package in.rob.client.page;

import in.lib.Constants;
import in.lib.handler.streams.PostRepostResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.rob.client.page.base.UserStreamFragment;
import android.os.Bundle;

/**
 * Fragment used for displaying users in a fragment list
 *
 * @author callumtaylor
 */
public class PostRepostsPage extends UserStreamFragment
{
	// arguments
	private String mPostId;

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null)
		{
			mPostId = arguments.getString(Constants.EXTRA_POST_ID);
		}
		else
		{
			getActivity().finish();
		}
	}

	@Override public void fetchStream(String lastId, final boolean append)
	{
		PostRepostResponseHandler handler = new PostRepostResponseHandler(getApplicationContext(), append);
		handler.setResponseKey(getResponseKeys()[0]);
		ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);
		APIManager.getInstance().getPostReposts(mPostId, lastId, handler);
	}

	@Override public String getCacheFileName()
	{
		return null;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{String.format(Constants.RESPONSE_REPOSTED, mPostId)};
	}
}