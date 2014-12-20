package in.rob.client.page.base;

import in.lib.adapter.ChannelAdapter;
import in.lib.event.NewPrivateMessageEvent;
import in.model.Channel;

import java.util.ArrayList;

import android.os.Bundle;

import com.squareup.otto.Subscribe;

public abstract class ChannelStreamFragment extends StreamFragment
{
	@Override public void onDataReady()
	{

	}

	@Override public void retrieveArguments(Bundle arguments)
	{

	}

	@Override public void setupAdapters()
	{
		if (getAdapter() == null)
		{
			setAdapter(new ChannelAdapter(getContext(), new ArrayList<Channel>()));
		}
		else
		{
			setAdapter(getAdapter());
		}
	}

	/**
	 * Override this to handle when a message has been posted.
	 * @param m
	 */
	@Subscribe public void onMessageReceived(NewPrivateMessageEvent m){}
}