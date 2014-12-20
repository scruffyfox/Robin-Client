package in.controller.adapter;

import android.content.Context;

public class ProfilePostAdapter extends PostAdapter
{
	public ProfilePostAdapter(Context context)
	{
		super(context);
	}

	public ProfilePostAdapter(Context context, String userId)
	{
		super(context, userId);
	}

	@Override public boolean isEmpty()
	{
		return false;
	}
}
