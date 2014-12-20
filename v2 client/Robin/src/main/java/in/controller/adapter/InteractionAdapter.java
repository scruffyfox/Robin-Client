package in.controller.adapter;

import android.content.Context;

import in.controller.adapter.base.RobinAdapter;
import in.model.Interaction;
import in.model.Interaction.Type;
import in.view.delegate.InteractionFollowDelegate;
import in.view.delegate.InteractionPostDelegate;

public class InteractionAdapter extends RobinAdapter<Interaction>
{
	private static final int TYPE_FOLLOW = 0;
	private static final int TYPE_POST = 1;

	public InteractionAdapter(Context context)
	{
		super(context);

		getItemTypes().put(TYPE_FOLLOW, new InteractionFollowDelegate(this));
		getItemTypes().put(TYPE_POST, new InteractionPostDelegate(this));
	}

	@Override public int getItemViewType(int position)
	{
		Interaction interaction = getItem(position);

		if (interaction.getType() == Type.FOLLOW)
		{
			return TYPE_FOLLOW;
		}
		else
		{
			return TYPE_POST;
		}
	}
}
