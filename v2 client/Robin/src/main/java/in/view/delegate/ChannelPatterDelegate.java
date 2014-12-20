package in.view.delegate;

import in.controller.adapter.base.RobinAdapter;
import in.model.Channel;
import in.rob.client.R;

public class ChannelPatterDelegate extends ChannelPmDelegate
{
	public ChannelPatterDelegate(RobinAdapter<Channel> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.channel_patter_view;
	}
}
