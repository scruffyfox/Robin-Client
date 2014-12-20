package in.lib.adapter;

import in.lib.adapter.base.RobinAdapter;
import in.lib.holder.AccountHolder;
import in.lib.utils.Dimension;
import in.model.SimpleUser;
import in.rob.client.MainApplication;
import in.rob.client.R;

import java.util.List;

import lombok.Getter;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CreateChannelAdapter extends RobinAdapter
{
	private final DisplayImageOptions options;
	private final Dimension mDimension;
	private final int viewId;
	@Getter private final Context context;

	public CreateChannelAdapter(Context context, List items)
	{
		this(context, R.layout.account_list_item, items);
	}

	public CreateChannelAdapter(Context context, int viewId, List items)
	{
		super(context, items);

		this.context = context;
		this.viewId = viewId;
		this.options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.default_avatar).showStubImage(R.drawable.default_avatar).build();
		this.mDimension = new Dimension(context);
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		AccountHolder holder;
		SimpleUser item = (SimpleUser)getItem(position);

		if (convertView == null)
		{
			convertView = getLayoutInflater().inflate(viewId, null);
			holder = new AccountHolder();
			holder.avatar = (ImageView)convertView.findViewById(R.id.avatar);
			holder.username = (TextView)convertView.findViewById(R.id.username);
			holder.mentionName = (TextView)convertView.findViewById(R.id.mention_name);
			holder.actionButton = (Button)convertView.findViewById(R.id.follow_button);
			holder.actionButton = holder.actionButton == null ? (Button)convertView.findViewById(R.id.remove_button) : holder.actionButton;
			convertView.setTag(R.id.TAG_VIEW_HOLDER, holder);

			if (holder.actionButton != null)
			{
				holder.actionButton.setOnClickListener(this);
			}
		}
		else
		{
			holder = (AccountHolder)convertView.getTag(R.id.TAG_VIEW_HOLDER);
		}

		holder.actionButton.setVisibility(View.GONE);
		holder.username.setText(item.getUserName());

		if (!TextUtils.isEmpty(item.getMentionName()))
		{
			holder.mentionName.setText("@" + item.getMentionName());
		}

		ImageLoader.getInstance().cancelDisplayTask(holder.avatar);
		ImageLoader.getInstance().displayImage(item.getAvatarUrl() + "?avatar=1&id=" + item.getId(), holder.avatar, MainApplication.getAvatarImageOptions());

		return convertView;
	}

	@Override public void onClick(final View v)
	{
		if (v.getId() == R.id.remove_button)
		{
			removeItemAt((Integer)v.getTag());
			notifyDataSetChanged();
		}
	}
}