package in.lib.adapter;

import in.lib.Constants;
import in.lib.adapter.base.RobinAdapter;
import in.lib.handler.UserFollowResponseHandler;
import in.lib.handler.UserUnFollowResponseHandler;
import in.lib.helper.AnimationHelper;
import in.lib.helper.ThemeHelper;
import in.lib.holder.UserHolder;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.view.AvatarView;
import in.lib.view.LinkifiedTextView;
import in.model.User;
import in.rob.client.R;
import in.rob.client.SettingsActivity;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

public class UserAdapter extends RobinAdapter implements OnClickListener, OnLongClickListener
{
	@Getter private final Context context;
	@Getter private final LayoutInflater layoutInflater;
	@Setter @Getter private int userLayoutResource;
	@Getter private final int[] buttonRes;

	public UserAdapter(Context context, List<User> items)
	{
		super(context, items, Order.DESC);

		this.context = context;
		this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.userLayoutResource = R.layout.user_list_item;
		this.setItems(items);

		int redButton = ThemeHelper.getDrawableResource(context, R.attr.rbn_red_button);
		int greyButton = ThemeHelper.getDrawableResource(context, R.attr.rbn_grey_button);
		buttonRes = new int[]{redButton, greyButton};
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		super.getView(position, convertView, parent);

		User user = (User)getItem(position);
		UserHolder currentHolder;

		if (convertView == null)
		{
			convertView = layoutInflater.inflate(userLayoutResource, parent, false);

			currentHolder = new UserHolder(convertView);
			convertView.setTag(currentHolder);

			currentHolder.followButton.setOnClickListener(this);
			currentHolder.muteButton.setOnClickListener(this);
		}
		else
		{
			currentHolder = (UserHolder)convertView.getTag();
		}

		// populate the view
		currentHolder.populate(user, this);

		currentHolder.avatar.setTag(position);
		currentHolder.followButton.setTag(position);
		currentHolder.muteButton.setTag(position);

		/**
		 * Set the animation if it hasn't been played
		 */
		if (getLastPositionAnimated() < position && (isAnimationsEnabled() && SettingsManager.isListAnimationEnabled()))
		{
			AnimationHelper.slideUp(convertView);
		}

		if (position > getLastPositionAnimated())
		{
			setLastPositionAnimated(position);
		}

		return convertView;
	}

	@Override public boolean onItemLongClick(AdapterView arg0, View v, int position, long arg3)
	{
		// weird conflicts with LinkifiedTextView on Jelly Bean
		if (v instanceof LinkifiedTextView)
		{
			return false;
		}

		User user = (User)getItem(position - getListView().getHeaderViewsCount());
		((AvatarView)v.findViewById(R.id.avatar)).triggerLongPress(user);

		return true;
	}

	@Override public void onClick(final View v)
	{
		final User user = (User)getItem((Integer)v.getTag());
		if (v.getId() == R.id.follow_button)
		{
			if (user.isYou())
			{
				Intent settings = new Intent(getContext(), SettingsActivity.class);
				settings.putExtra(Constants.EXTRA_START_PAGE, 0);
				context.startActivity(settings);
				return;
			}

			if (user.getYouFollow())
			{
				APIManager.getInstance().unfollowUser(user.getId(), new UserUnFollowResponseHandler()
				{
					@Override public void onFinish(boolean failed)
					{
						super.onFinish(failed);

						if (failed)
						{
							Context c = v.getContext();
							user.setYouFollow(true);
							((Button)v).setText(R.string.unfollow);
							((Button)v).setBackgroundResource(buttonRes[1]);

							if (c != null)
							{
								Toast.makeText(getContext(), context.getString(R.string.unfollow_failed) + " @" + user.getMentionName(), Toast.LENGTH_SHORT).show();
							}
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
							Context c = v.getContext();
							user.setYouFollow(false);
							((Button)v).setText(R.string.follow);
							((Button)v).setBackgroundResource(buttonRes[0]);

							if (c != null)
							{
								String message = c.getString(R.string.follow_failed) + " @" + user.getMentionName();
								if (getConnectionInfo().responseCode == 507)
								{
									message = c.getString(R.string.too_many_follow);
								}

								Toast.makeText(c, message, Toast.LENGTH_LONG).show();
							}
						}
					}
				});
			}

			user.setYouFollow(!user.getYouFollow());
			((Button)v).setText(user.getYouFollow() ? R.string.unfollow : R.string.follow);
			v.setBackgroundResource(user.getYouFollow() ? buttonRes[1] : buttonRes[0]);
		}
		else if (v.getId() == R.id.mute_button)
		{
			if (user.isMuted())
			{
				APIManager.getInstance().unMuteUser(user.getId(), new JsonResponseHandler()
				{
					@Override public void onSuccess(){}
					@Override public void onFinish(boolean failed)
					{
						if (failed)
						{
							Context c = v.getContext();
							user.setMuted(true);
							((Button)v).setText(R.string.unmute);
							((Button)v).setBackgroundResource(buttonRes[1]);

							if (c != null)
							{
								Toast.makeText(c, c.getString(R.string.unmute_failed) + " @" + user.getMentionName(), Toast.LENGTH_SHORT).show();
							}
						}
					}
				});
			}
			else
			{
				APIManager.getInstance().muteUser(user.getId(), new JsonResponseHandler()
				{
					@Override public void onSuccess(){}
					@Override public void onFinish(boolean failed)
					{
						if (failed)
						{
							Context c = v.getContext();
							user.setMuted(false);
							((Button)v).setText(R.string.mute);
							((Button)v).setBackgroundResource(buttonRes[0]);

							if (c != null)
							{
								Toast.makeText(c, c.getString(R.string.mute_failed) + " @" + user.getMentionName(), Toast.LENGTH_SHORT).show();
							}
						}
					}
				});
			}

			user.setMuted(!user.isMuted());
			((Button)v).setText(user.isMuted() ? R.string.unmute : R.string.mute);
			v.setBackgroundResource(user.isMuted() ? buttonRes[1] : buttonRes[0]);
		}
	}

	@Override public boolean onLongClick(View v)
	{
		return false;
	}
}