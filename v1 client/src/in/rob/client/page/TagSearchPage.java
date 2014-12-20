package in.rob.client.page;

import in.lib.Constants;
import in.lib.adapter.PostAdapter;
import in.lib.event.DeletePostEvent;
import in.lib.event.NewPostEvent;
import in.lib.handler.streams.KeywordResponseHandler;
import in.lib.handler.streams.TagPostsResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.utils.ArrayUtils;
import in.model.Post;
import in.rob.client.page.base.PostStreamFragment;
import lombok.Getter;
import lombok.Setter;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

public class TagSearchPage extends PostStreamFragment
{
	@Getter @Setter private int pageIndex = 1;
	private String mTag;

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null)
		{
			mTag = arguments.getString(Constants.EXTRA_TAG_NAME);
		}
	}

	@Override public void setupAdapters()
	{
		super.setupAdapters();
		((PostAdapter)getAdapter()).setShowMuted(true);
	}

	@Override public void fetchStream(String lastId, final boolean append)
	{
		if (mTag.startsWith("#") || !SettingsManager.isKeywordSearchEnabled())
		{
			TagPostsResponseHandler handler = new TagPostsResponseHandler(getContext(), append);
			handler.setResponseKey(getResponseKeys()[0]);
			ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);
			APIManager.getInstance().searchPosts(mTag.replace("#", ""), lastId, handler);
		}
		else
		{
			KeywordResponseHandler handler = new KeywordResponseHandler(getContext(), append);
			handler.setResponseKey(getResponseKeys()[0]);
			ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);
			APIManager.getInstance().keywordSearch(mTag, lastId, handler);
		}
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{String.format(Constants.RESPONSE_TAG_SEARCH, mTag)};
	}

	@Override public String getCacheFileName()
	{
		return null;
	}

	@Subscribe @Override public void onPostRecieved(NewPostEvent event)
	{
		Post p = event.getPost();

		if (p != null)
		{
			if (ArrayUtils.indexOf(mTag, p.getHashTags()) > -1)
			{
				prependItem(p);
			}
		}
	}

	@Subscribe @Override public void onPostDeleted(DeletePostEvent event)
	{
		super.onPostDeleted(event);
	}
}