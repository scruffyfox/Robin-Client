package in.controller.adapter;

import android.content.Context;

import in.controller.adapter.base.RobinAdapter;
import in.model.ChannelMessage;
import in.view.delegate.ChannelMessageDelegate;
import lombok.Getter;
import lombok.Setter;

public class ChannelMessageAdapter extends RobinAdapter<ChannelMessage>
{
	private static final int TYPE_MESSAGE = 0;
	private static final int TYPE_MESSAGE_SELECTED = 1;

	@Getter @Setter private ChannelMessage selectedMessage;

	public ChannelMessageAdapter(Context context)
	{
		super(context);

		getItemTypes().put(TYPE_MESSAGE, new ChannelMessageDelegate(this));
		getItemTypes().put(TYPE_MESSAGE_SELECTED, new ChannelMessageDelegate(this));
	}

	@Override public int getItemViewType(int position)
	{
		ChannelMessage item = getItem(position);

		if (getCount() == 1 || item.equals(selectedMessage))
		{
			return TYPE_MESSAGE_SELECTED;
		}

		return TYPE_MESSAGE;
	}
}
