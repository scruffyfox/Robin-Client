package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.model.Interaction;
import in.rob.client.R;
import in.view.delegate.base.AdapterDelegate;
import in.view.holder.InteractionFollowHolder;

public class InteractionFollowDelegate extends AdapterDelegate<Interaction>
{
	public InteractionFollowDelegate(RobinAdapter<Interaction> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.interaction_follow_view;
	}

	@Override public View getView(Interaction item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		InteractionFollowHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);

			holder = new InteractionFollowHolder(convertView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (InteractionFollowHolder)convertView.getTag();
		}

		convertView.setTag(R.id.TAG_POSITION, position);
		holder.populate(item);

		return convertView;
	}
}
