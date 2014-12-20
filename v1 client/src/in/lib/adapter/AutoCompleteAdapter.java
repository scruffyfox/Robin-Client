package in.lib.adapter;

import in.lib.adapter.base.RobinAdapter;
import in.lib.utils.Dimension;
import in.model.SimpleUser;
import in.model.base.NetObject;
import in.rob.client.R;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AutoCompleteAdapter extends RobinAdapter implements Filterable
{
	private final DisplayImageOptions options;
	private final Dimension mDimension;
	private List<NetObject> originalItems;
	private String typedConstraint = "";

	private class ViewHolder
	{
		public ImageView avatar;
		public TextView username, mentionName;
	}

	public AutoCompleteAdapter(Context context, List<NetObject> items)
	{
		super(context, items);

		originalItems = new ArrayList<NetObject>(items);
		options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.default_avatar).showStubImage(R.drawable.default_avatar).cacheInMemory().build();
		mDimension = new Dimension(context);
	}

	@Override public void setItems(List items)
	{
		super.setItems(items);
		originalItems = new ArrayList<NetObject>(items);
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		NetObject item = getItem(position);

		if (convertView == null)
		{
			convertView = getLayoutInflater().inflate(R.layout.auto_suggest_list_item, null);
			holder = new ViewHolder();
			holder.avatar = (ImageView)convertView.findViewById(R.id.avatar);
			holder.username = (TextView)convertView.findViewById(R.id.username);
			holder.mentionName = (TextView)convertView.findViewById(R.id.mention_name);
			convertView.setTag(R.id.TAG_VIEW_HOLDER, holder);
		}

		holder = (ViewHolder)convertView.getTag(R.id.TAG_VIEW_HOLDER);

		if (item instanceof SimpleUser)
		{
			holder.username.setText(((SimpleUser)item).getUserName());
			holder.mentionName.setText("@" + ((SimpleUser)item).getMentionName());
			holder.mentionName.setVisibility(View.VISIBLE);

			holder.avatar.setLayoutParams(new LinearLayout.LayoutParams(mDimension.densityPixel(50), mDimension.densityPixel(50)));
			holder.avatar.setVisibility(View.VISIBLE);
			ImageLoader.getInstance().displayImage(((SimpleUser)item).getAvatarUrl(), holder.avatar, options);
		}
		else
		{
			holder.username.setText(item.getFilterTag());
			holder.username.setPadding(0, 10, 0, 10);
			holder.mentionName.setVisibility(View.GONE);
			holder.avatar.setVisibility(View.GONE);
		}

		return convertView;
	}

	@Override public Filter getFilter()
	{
		Filter myFilter = new Filter()
		{
			@Override protected FilterResults performFiltering(CharSequence constraint)
			{
				FilterResults filterResults = new FilterResults();
				if (constraint != null)
				{
					typedConstraint = constraint.toString();
					List<NetObject> items = new ArrayList<NetObject>();
					for (NetObject item : originalItems)
					{
						if (item instanceof SimpleUser)
						{
							if (("@" + ((SimpleUser)item).getMentionName().toLowerCase()).startsWith(constraint.toString().toLowerCase()))
							{
								items.add(item);
							}
						}
						else
						{
							if (item.getFilterTag().toString().toLowerCase().startsWith(constraint.toString().toLowerCase()))
							{
								items.add(item);
							}
						}
					}

					// Now assign the values and count to the FilterResults
					// object
					filterResults.values = items;
					filterResults.count = items.size();
				}

				return filterResults;
			}

			@Override protected void publishResults(CharSequence contraint, FilterResults results)
			{
				if (results != null && results.count > 0)
				{
					clear();
					addItems((List<NetObject>)results.values);

					notifyDataSetChanged();
				}
			}
		};

		return myFilter;
	}
}