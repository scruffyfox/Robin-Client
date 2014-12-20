package in.controller.adapter;

import android.content.Context;

import in.controller.adapter.base.RobinAdapter;
import in.model.Channel;
import in.model.Channel.Type;
import in.view.delegate.ChannelPmDelegate;

public class ChannelAdapter extends RobinAdapter<Channel>
{
	private static final int TYPE_PM = 0;
	private static final int TYPE_PATTER = 1;

	public ChannelAdapter(Context context)
	{
		super(context);

		getItemTypes().put(TYPE_PM, new ChannelPmDelegate(this));
		getItemTypes().put(TYPE_PATTER, new ChannelPmDelegate(this));
	}

	@Override public int getItemViewType(int position)
	{
		Channel item = getItem(position);
		return item.getType() == Type.PATTER_CHANNEL ? TYPE_PATTER : TYPE_PM;
	}
}
