package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import in.model.AdnModel;
import in.model.SimpleUser;
import in.rob.client.R;
import in.view.delegate.base.AdapterDelegate;
import in.view.holder.AccountHolder;

public class AutoCompleteUserDelegate extends AdapterDelegate
{
	public AutoCompleteUserDelegate(BaseAdapter adapter)
	{
		super(null);
	}

	@Override public View getView(AdnModel item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		if (item instanceof SimpleUser)
		{
			AccountHolder holder;

			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.popup_user_view, parent, false);
				holder = new AccountHolder(convertView);
				convertView.setTag(holder);
			}
			else
			{
				holder = (AccountHolder)convertView.getTag();
			}

			holder.populate((SimpleUser)item);
			convertView.setTag(R.id.TAG_POSITION, position);
		}

		return convertView;
	}
}
