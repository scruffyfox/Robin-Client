package in.rob.client.fragment;

import android.content.Intent;
import android.os.Parcelable;

import in.controller.adapter.ChannelAdapter;
import in.controller.handler.ChannelStreamResponseHandler;
import in.data.stream.ChannelStream;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.manager.UserManager;
import in.lib.utils.Views.Injectable;
import in.model.AdnModel;
import in.rob.client.ChannelMessagesActivity;
import in.rob.client.fragment.base.StreamFragment;

@Injectable
public class ChannelsFragment extends StreamFragment
{
	@Override public void setupAdapter()
	{
		setAdapter(new ChannelAdapter(getContext()));
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		ChannelStreamResponseHandler response = new ChannelStreamResponseHandler(append);
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
		APIManager.getInstance().getChannels(lastId, response);
	}

	@Override public void onListItemClick(AdnModel model)
	{
		Intent messagesIntent = new Intent(getContext(), ChannelMessagesActivity.class);
		messagesIntent.putExtra(Constants.EXTRA_CHANNEL, (Parcelable)model);
		getActivity().startActivity(messagesIntent);
	}

	@Override public Class getCacheClass()
	{
		return ChannelStream.class;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			String.format(Constants.RESPONSE_CHANNELS, UserManager.getInstance().getUser().getId())
		};
	}

	@Override public String getCacheKey()
	{
		return String.format(Constants.CACHE_CHANNELS, UserManager.getInstance().getUser().getId());
	}
}
