package in.controller.adapter;

import android.content.Context;

import in.controller.adapter.base.RobinAdapter;
import in.model.User;
import in.view.delegate.UserDelegate;

public class UserAdapter extends RobinAdapter<User>
{
	private static final int TYPE_USER = 0;

	public UserAdapter(Context context)
	{
		super(context);

		getItemTypes().put(TYPE_USER, new UserDelegate(this));
	}

	@Override public int getItemViewType(int position)
	{
		User item = getItem(position);
		return TYPE_USER;
	}
}
