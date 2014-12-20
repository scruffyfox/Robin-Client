package in.rob.client.page;

import in.lib.Constants;
import in.lib.event.NewPrivateMessageEvent;
import in.lib.handler.streams.ChannelResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.lib.manager.UserManager;
import in.model.Channel;
import in.model.User;
import in.rob.client.MessagesActivity;
import in.rob.client.page.base.ChannelStreamFragment;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

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
public class ChannelsPage extends ChannelStreamFragment
{
	@Override public void fetchStream(String lastId, boolean append)
	{
		ChannelResponseHandler handler = new ChannelResponseHandler(getApplicationContext(), append);
		handler.setResponseKey(getResponseKeys()[0]);
		ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);
		APIManager.getInstance().getMessageChannels(lastId, handler);
	}

	@Override public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
	{
		Channel channel = (Channel)getAdapter().getItem(position - getHeadedListView().getHeaderViewsCount());
		Intent messageView = new Intent(getContext(), MessagesActivity.class);
		messageView.putExtra(Constants.EXTRA_CHANNEL, channel);
		startActivity(messageView);

		channel.setUnread(false);
		getAdapter().notifyDataSetChanged();
	}

	@Override public void onDestroy()
	{
		writeToCache(getAdapter().getStream());
		super.onDestroy();
	}

	@Subscribe @Override public void onMessageReceived(NewPrivateMessageEvent m)
	{
		if (m.getMessage() != null)
		{
			// find the channel, update the recent message, bump item to top of adapter and refresh
			Channel c = (Channel)getAdapter().getItemById(m.getMessage().getChannelId());
			c.setRecentMessage(m.getMessage());
			c.getReaders().remove(m.getMessage().getPoster().getId());
			c.getReaders().add(0, m.getMessage().getPoster().getId());
			getAdapter().removeItem(c);
			getAdapter().prependItem(c);
			getAdapter().notifyDataSetChanged();
		}
	}

	@Override public String getCacheFileName()
	{
		return String.format(Constants.CACHE_CHANNELS_LIST_NAME, UserManager.getUserId());
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{String.format(Constants.RESPONSE_CHANNELS, UserManager.getUserId())};
	}
}