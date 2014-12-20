package in.rob.client.page;

import in.lib.Constants;
import in.lib.event.StarPostEvent;
import in.lib.event.UnStarPostEvent;
import in.lib.handler.streams.StarredResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.lib.manager.UserManager;
import in.model.User;
import in.rob.client.page.base.PostStreamFragment;
import android.os.Bundle;
import android.widget.AdapterView.OnItemClickListener;

import com.squareup.otto.Subscribe;

/**
 * Fragment used for displaying posts in a fragment list
 *
 * Possible extra arguments:
 * <ul>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_USER_ID}</b>: The {@link User} id to search against</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_USER}</b>: The {@link User} to search against</li>
 * </ul>
 */
public class StarredPage extends PostStreamFragment implements OnItemClickListener
{
	private String mUserId = "";

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (getArguments() != null)
		{
			if (arguments.containsKey(Constants.EXTRA_USER_ID))
			{
				mUserId = arguments.getString(Constants.EXTRA_USER_ID);
			}
			else if (arguments.containsKey(Constants.EXTRA_USER))
			{
				mUserId = ((User)arguments.get(Constants.EXTRA_USER)).getId();
			}
			else
			{
				mUserId = UserManager.getUserId();
			}
		}
		else
		{
			mUserId = UserManager.getUserId();
		}
	}

	@Override public void fetchStream(String lastId, final boolean append)
	{
		StarredResponseHandler handler = new StarredResponseHandler(getApplicationContext(), append);
		handler.setResponseKey(getResponseKeys()[0]);
		ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);
		APIManager.getInstance().getStarredPosts(mUserId, lastId, handler);
	}

	@Override public String getCacheFileName()
	{
		return String.format(Constants.CACHE_STARRED_LIST_NAME, mUserId);
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{String.format(Constants.RESPONSE_STARRED, mUserId)};
	}

	@Subscribe @Override public void onPostStarred(StarPostEvent event)
	{
		if (event != null)
		{
			prependItem(event.getPost());
		}
	}

	@Subscribe @Override public void onPostUnStarred(UnStarPostEvent event)
	{
		if (event != null)
		{
			deleteItem(event.getPost());
		}
	}
}