package in.lib.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.Collection;
import java.util.List;

import in.model.SimpleUser;
import in.rob.client.R;
import in.view.holder.AccountHolder;
import lombok.Getter;
import lombok.NonNull;

public class AccountAdapter extends BaseAdapter
{
	@Getter private final Context context;
	@Getter private final List<? extends SimpleUser> items;

	public AccountAdapter(Context context, @NonNull List<? extends SimpleUser> items)
	{
		this.context = context;
		this.items = items;
	}

	public void setItems(Collection users)
	{
		this.items.clear();
		this.items.addAll(users);
	}

	@Override public int getCount()
	{
		return items.size();
	}

	@Override public SimpleUser getItem(int position)
	{
		return items.get(position);
	}

	@Override public long getItemId(int position)
	{
		return items.get(position).hashCode();
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		AccountHolder holder;
		SimpleUser item = getItem(position);

		if (convertView == null)
		{
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.popup_user_view, parent, false);
			holder = new AccountHolder(convertView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (AccountHolder)convertView.getTag();
		}

		holder.populate(item);
		convertView.setTag(R.id.TAG_POSITION, position);

		return convertView;
	}
}