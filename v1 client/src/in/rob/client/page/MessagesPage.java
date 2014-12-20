package in.rob.client.page;

import in.lib.Constants;
import in.lib.adapter.AccountAdapter;
import in.lib.adapter.PrivateMessageAdapter;
import in.lib.event.DeletePrivateMessageEvent;
import in.lib.event.NewPrivateMessageEvent;
import in.lib.handler.streams.MessagesResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.model.Channel;
import in.model.PrivateMessage;
import in.model.User;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.dialog.base.DialogBuilder;
import in.rob.client.page.base.MessageStreamFragment;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.squareup.otto.Subscribe;

/**
 * Fragment used for displaying posts in a fragment list
 * @author callumtaylor
 */
public class MessagesPage extends MessageStreamFragment
{
	private Channel channel;
	private PrivateMessage centerMessage;

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null)
		{
			if (arguments.containsKey(Constants.EXTRA_CHANNEL))
			{
				channel = (Channel)arguments.getParcelable(Constants.EXTRA_CHANNEL);
			}
			else if (arguments.containsKey(Constants.EXTRA_CHANNEL_ID))
			{
				channel = new Channel();
				channel.setId(arguments.getString(Constants.EXTRA_CHANNEL_ID));
			}

			if (arguments.containsKey(Constants.EXTRA_CENTER_MESSAGE))
			{
				centerMessage = (PrivateMessage)arguments.getParcelable(Constants.EXTRA_CENTER_MESSAGE);
			}
		}
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_subscribed)
		{
			final ArrayList<User> loadedUsers = new ArrayList<User>();

			for (String s : channel.getReaders())
			{
				User u = User.loadUser(s);

				if (u != null)
				{
					loadedUsers.add(u);
				}
			}

			DialogBuilder.create(getContext())
				.setTitle(R.string.subscribers)
				.setAdapter(new AccountAdapter(getContext(), R.layout.user_dialog_list_item, loadedUsers), new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
						profileIntent.putExtra(Constants.EXTRA_USER, loadedUsers.get(which));
						startActivity(profileIntent);
						dialog.dismiss();
					}
				})
				.setNegativeButton(R.string.close, null)
			.show();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override public void onDataReady()
	{
		if (centerMessage == null)
		{
			centerMessage = (PrivateMessage)getAdapter().getItem(0);
		}

		((PrivateMessageAdapter)getAdapter()).setCenter(centerMessage);
		checkAdapterSizes();
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		showProgressLoader();
		MessagesResponseHandler handler = new MessagesResponseHandler(getApplicationContext(), append);
		handler.setResponseKey(getResponseKeys()[0]);
		ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);
		APIManager.getInstance().getMessages(channel.getId(), lastId, handler);
	}

	@Override public void onDestroyView()
	{
		if (getAdapter().getStreamMarker() != null)
		{
			APIManager.getInstance().updateMarker(getAdapter().getFirstId(), getAdapter().getStreamMarker().getName(), null);
		}

		super.onDestroyView();
	}

	@Override public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
	{
		centerMessage = (PrivateMessage)getAdapter().getItem(position - getListView().getHeaderViewsCount());
		((PrivateMessageAdapter)getAdapter()).setCenter(centerMessage);
		getAdapter().notifyDataSetChanged();
	}

	@Override public String getCacheFileName()
	{
		return String.format(Constants.CACHE_MESSAGE_LIST_NAME, channel.getId());
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{String.format(Constants.RESPONSE_MESSAGES, channel.getId())};
	}

	@Subscribe @Override public void onMessageRecieved(NewPrivateMessageEvent p)
	{
		if (p != null && p.getMessage().getChannelId().equals(channel.getId()))
		{
			super.onMessageRecieved(p);
		}
	}

	@Subscribe @Override public void onMessageDeleted(DeletePrivateMessageEvent p)
	{
		super.onMessageDeleted(p);
	}
}