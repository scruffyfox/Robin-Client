package in.rob.client.page;

import in.lib.Constants;
import in.lib.adapter.UserAdapter;
import in.lib.handler.streams.MutedResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.model.User;
import in.rob.client.R;
import in.rob.client.page.base.UserStreamFragment;
import android.os.Bundle;

public class MutedPage extends UserStreamFragment
{
	private String mUserId;

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null)
		{
			if (arguments.containsKey(Constants.EXTRA_USER_ID))
			{
				mUserId = arguments.getString(Constants.EXTRA_USER_ID);
			}
			else if (arguments.containsKey(Constants.EXTRA_USER))
			{
				mUserId = ((User)arguments.get(Constants.EXTRA_USER)).getId();
			}
		}
	}

	@Override public void setupAdapters()
	{
		super.setupAdapters();
		((UserAdapter)getAdapter()).setUserLayoutResource(R.layout.user_muted_list_item);
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		MutedResponseHandler handler = new MutedResponseHandler(getApplicationContext(), append);
		handler.setResponseKey(getResponseKeys()[0]);
		ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);

		APIManager.getInstance().getUserMuted(mUserId, lastId, handler);
	}

	@Override public String getCacheFileName()
	{
		return null;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{Constants.RESPONSE_MUTED};
	}
}