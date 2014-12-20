package in.lib.adapter;

import in.lib.Constants;
import in.lib.adapter.base.RobinAdapter;
import in.lib.handler.UserFollowResponseHandler;
import in.lib.handler.UserUnFollowResponseHandler;
import in.lib.helper.ThemeHelper;
import in.lib.holder.AccountHolder;
import in.lib.manager.APIManager;
import in.lib.utils.Dimension;
import in.model.SimpleUser;
import in.model.base.NetObject;
import in.rob.client.R;
import in.rob.client.SettingsActivity;

import java.util.List;

import lombok.Getter;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AccountAdapter extends RobinAdapter
{
	private final Dimension mDimension;
	private final int viewId;
	@Getter private final Context context;

	public AccountAdapter(Context context, List items)
	{
		this(context, R.layout.account_list_item, items);
	}

	public AccountAdapter(Context context, int viewId, List items)
	{
		super(context, items);

		this.context = context;
		this.viewId = viewId;
		this.mDimension = new Dimension(context);
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		AccountHolder holder;
		NetObject item = getItem(position);

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

		AccountHolder.populate(holder, convertView, item);

		if (holder.actionButton != null)
		{
			holder.actionButton.setTag(position);
		}

		return convertView;
	}

	@Override public void onClick(final View v)
	{
		final SimpleUser user = (SimpleUser)getItem((Integer)v.getTag());
		if (v.getId() == R.id.follow_button)
		{
			if (user.isYou())
			{
				Intent settings = new Intent(getContext(), SettingsActivity.class);
				settings.putExtra(Constants.EXTRA_START_PAGE, 0);
				context.startActivity(settings);
				return;
			}

			final int redButton = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_red_button);
			final int greyButton = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_grey_button);

			if (user.getYouFollow())
			{
				APIManager.getInstance().unfollowUser(user.getId(), new UserUnFollowResponseHandler()
				{
					@Override public void onFinish(boolean failed)
					{
						super.onFinish(failed);

						if (failed)
						{
							user.setYouFollow(true);
							((Button)v).setText(R.string.unfollow);
							((Button)v).setBackgroundResource(greyButton);
							Toast.makeText(getContext(), context.getString(R.string.unfollow_failed) + " @" + user.getMentionName(), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
			else
			{
				APIManager.getInstance().followUser(user.getId(), new UserFollowResponseHandler()
				{
					@Override public void onFinish(boolean failed)
					{
						super.onFinish(failed);

						if (failed)
						{
							user.setYouFollow(false);
							((Button)v).setText(R.string.follow);
							((Button)v).setBackgroundResource(redButton);
							Toast.makeText(getContext(), context.getString(R.string.follow_failed) + " @" + user.getMentionName(), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}

			user.setYouFollow(!user.getYouFollow());
			((Button)v).setText(user.getYouFollow() ? R.string.unfollow : R.string.follow);
			v.setBackgroundResource(user.getYouFollow() ? greyButton : redButton);
		}
		else if (v.getId() == R.id.remove_button)
		{
			removeItemAt((Integer)v.getTag());
			notifyDataSetChanged();
		}
	}
}