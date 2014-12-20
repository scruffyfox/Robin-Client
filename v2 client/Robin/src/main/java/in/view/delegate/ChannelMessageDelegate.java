package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.model.ChannelMessage;
import in.rob.client.R;
import in.view.delegate.base.AdapterDelegate;
import in.view.holder.ChannelMessageHolder;

public class ChannelMessageDelegate extends AdapterDelegate<ChannelMessage>
{
	public ChannelMessageDelegate(RobinAdapter<ChannelMessage> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.channel_message_view;
	}

	@Override public View getView(ChannelMessage item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		ChannelMessageHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);

			holder = new ChannelMessageHolder(convertView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ChannelMessageHolder)convertView.getTag();
		}

		convertView.setTag(R.id.TAG_POSITION, position);
		holder.populate(item);

		return convertView;
	}

	@Override public boolean onItemLongClick(int position, View view)
	{
		return true;
	}
}
