package in.rob.client.page;

import in.lib.Constants;
import in.lib.event.DeletePostEvent;
import in.lib.event.NewPostEvent;
import in.lib.handler.streams.MentionsResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.lib.manager.UserManager;
import in.model.Post;
import in.model.User;
import in.obj.entity.MentionEntity;
import in.rob.client.MainActivity;
import in.rob.client.page.base.PostStreamFragment;
import in.rob.client.widget.ScrollWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

/**
 * Fragment used for displaying posts in a fragment list
 * @author callumtaylor
 */
public class MentionsPage extends PostStreamFragment
{
	private String userId = "";

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null)
		{
			if (arguments.containsKey(Constants.EXTRA_USER_ID))
			{
				userId = (String)arguments.get(Constants.EXTRA_USER_ID);
			}
			else if (arguments.containsKey(Constants.EXTRA_USER))
			{
				userId = ((User)arguments.getParcelable(Constants.EXTRA_USER)).getId();
			}
			else
			{
				userId = UserManager.getUserId();
			}
		}
	}

	@Override public void setTicker(int count)
	{
		if (getActivity() instanceof MainActivity)
		{
			super.setTicker(count);
		}
	}

	@Subscribe @Override public void onPostRecieved(NewPostEvent event)
	{
		Post p = event.getPost();

		if (p != null)
		{
			for (MentionEntity m : p.getMentions())
			{
				if (m.getId().equals(userId))
				{
					p.setMention(true);
					break;
				}
			}

			if (p.isMention())
			{
				super.onPostRecieved(event);
			}
		}
	}

	@Subscribe @Override public void onPostDeleted(DeletePostEvent event)
	{
		super.onPostDeleted(event);
	}

	@Override public void fetchStream(String lastId, final boolean append)
	{
		showProgressLoader();

		MentionsResponseHandler handler = new MentionsResponseHandler(getApplicationContext(), append);
		ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);
		APIManager.getInstance().getMentions(userId, lastId, handler);
	}

	@Override public void onFinishedWriting()
	{
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			Context context = getApplicationContext();
			ComponentName name = new ComponentName(context, ScrollWidgetProvider.class);
			int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);

			for (int index = 0; index < ids.length; index++)
			{
				Intent refresh = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				refresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, ids[index]);
				getContext().sendBroadcast(refresh);
			}
		}
	}

	@Override public String getCacheFileName()
	{
		return String.format(Constants.CACHE_MENTION_LIST_NAME, this.userId);
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{String.format(Constants.RESPONSE_MENTIONS, userId)};
	}
}