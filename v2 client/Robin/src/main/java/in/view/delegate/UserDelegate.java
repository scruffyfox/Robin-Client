package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.model.User;
import in.rob.client.R;
import in.view.delegate.base.AdapterDelegate;
import in.view.holder.UserHolder;

public class UserDelegate extends AdapterDelegate<User>
{
	public UserDelegate(RobinAdapter<User> adapter)
	{
		super(adapter);
	}

	@Override public View getView(User item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		UserHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.user_view, parent, false);
			holder = new UserHolder(convertView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (UserHolder)convertView.getTag();
		}

		holder.populate(item);
		return convertView;
	}
}
