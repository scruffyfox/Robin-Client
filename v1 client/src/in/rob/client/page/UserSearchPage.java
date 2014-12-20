package in.rob.client.page;

import in.lib.Constants;
import in.lib.event.FollowUserEvent;
import in.lib.event.UnFollowUserEvent;
import in.lib.handler.streams.UserSearchResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.rob.client.page.base.UserStreamFragment;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

public class UserSearchPage extends UserStreamFragment
{
	private String mTag;

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null)
		{
			mTag = arguments.getString(Constants.EXTRA_TAG_NAME);
		}

		mTag = mTag.replace("#", "");
	}

	@Override public void initData()
	{
		super.initData();
		setAllowPagination(false);
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		UserSearchResponseHandler handler = new UserSearchResponseHandler(getApplicationContext(), append);
		handler.setResponseKey(getResponseKeys()[0]);
		ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);
		APIManager.getInstance().searchUsers("@" + mTag, lastId, handler);
	}

	@Override public String getCacheFileName()
	{
		return null;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{String.format(Constants.RESPONSE_USER_SEARCH, mTag)};
	}

	@Override @Subscribe public void onFollowUser(FollowUserEvent event)
	{
		super.onFollowUser(event);
	}

	@Override @Subscribe public void onUnFollowUser(UnFollowUserEvent event)
	{
		super.onUnFollowUser(event);
	}

	@Override public void addLoadMoreView(){}
	@Override public void removeLoadMoreView(){}
}