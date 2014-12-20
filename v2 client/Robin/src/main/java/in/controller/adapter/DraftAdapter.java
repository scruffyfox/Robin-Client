package in.controller.adapter;

import android.content.Context;

import in.controller.adapter.base.RobinAdapter;
import in.model.base.Draft;
import in.view.delegate.DraftPostDelegate;

public class DraftAdapter extends RobinAdapter<Draft>
{
	private static final int TYPE_POST = 0;
	private static final int TYPE_CHANNEL_MESSAGE = 1;

	public DraftAdapter(Context context)
	{
		super(context);

		getItemTypes().put(TYPE_POST, new DraftPostDelegate(this));
//		getItemTypes().put(TYPE_CHANNEL_MESSAGE, new PostDelegate(this));
	}

	@Override public int getItemViewType(int position)
	{
		Draft item = getItem(position);

//		if (item instanceof DraftPost)
//		{
//			return TYPE_POST;
//		}
//		else if (item instanceof DraftMessage)
//		{
//			return TYPE_CHANNEL_MESSAGE;
//		}

		return TYPE_POST;
	}
}
