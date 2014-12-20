package in.rob.client.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import in.controller.adapter.ChannelMessageAdapter;
import in.controller.handler.ChannelMessageStreamResponseHandler;
import in.data.stream.ChannelMessageStream;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.utils.Views.Injectable;
import in.model.Channel;
import in.rob.client.R;
import in.rob.client.dialog.NewMessageDialog;
import in.rob.client.fragment.base.StreamFragment;
import lombok.Getter;

@Injectable
public class ChannelMessagesFragment extends StreamFragment
{
	@Getter private Channel channel;

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null && arguments.containsKey(Constants.EXTRA_CHANNEL))
		{
			channel = (Channel)arguments.getParcelable(Constants.EXTRA_CHANNEL);
		}
		else
		{
			Toast.makeText(getContext(), R.string.channels_stream_fail, Toast.LENGTH_SHORT).show();
			getActivity().finish();
			return;
		}
	}

	@Override public void setupAdapter()
	{
		setAdapter(new ChannelMessageAdapter(getContext()));
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		ChannelMessageStreamResponseHandler response = new ChannelMessageStreamResponseHandler(append);
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
		APIManager.getInstance().getChannelMessages(channel.getId(), lastId, response);
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_channel_messages, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_new_message)
		{
			Intent newMessage = new Intent(getContext(), NewMessageDialog.class);
			newMessage.putExtra(Constants.EXTRA_CHANNEL, (Parcelable)getChannel());
			startActivity(newMessage);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override public Class getCacheClass()
	{
		return ChannelMessageStream.class;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			String.format(Constants.RESPONSE_CHANNEL_MESSAGES, channel.getId())
		};
	}

	@Override public String getCacheKey()
	{
		return String.format(Constants.CACHE_CHANNEL_MESSAGES, channel.getId());
	}
}
