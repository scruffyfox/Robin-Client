package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.model.Channel;
import in.rob.client.R;
import in.view.delegate.base.AdapterDelegate;
import in.view.holder.ChannelHolder;

public class ChannelPmDelegate extends AdapterDelegate<Channel>
{
	public ChannelPmDelegate(RobinAdapter<Channel> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.channel_pm_view;
	}

	@Override public View getView(Channel item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		ChannelHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);
			holder = new ChannelHolder(convertView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ChannelHolder)convertView.getTag();
		}

		holder.populate(item);
		return convertView;
	}
}
