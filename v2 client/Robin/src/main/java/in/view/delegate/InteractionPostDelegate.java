package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.model.Interaction;
import in.rob.client.R;
import in.view.delegate.base.AdapterDelegate;
import in.view.holder.InteractionPostHolder;

public class InteractionPostDelegate extends AdapterDelegate<Interaction>
{
	public InteractionPostDelegate(RobinAdapter<Interaction> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.interaction_post_view;
	}

	@Override public View getView(Interaction item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		InteractionPostHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);

			holder = new InteractionPostHolder(convertView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (InteractionPostHolder)convertView.getTag();
		}

		convertView.setTag(R.id.TAG_POSITION, position);
		holder.populate(item);

		return convertView;
	}
}
