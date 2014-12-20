package in.lib.adapter;

import in.lib.Constants;
import in.lib.adapter.base.RobinAdapter;
import in.lib.helper.AnimationHelper;
import in.lib.holder.ChannelHolder;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.view.LinkifiedTextView;
import in.model.Channel;
import in.model.User;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.dialog.NewMessageDialog;
import in.rob.client.dialog.base.DialogBuilder;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;

public class ChannelAdapter extends RobinAdapter implements OnLongClickListener
{
	@Getter private final Context context;
	@Getter private final LayoutInflater layoutInflater;

	private int mLastPositionAnimated = 5;

	public ChannelAdapter(Context context, List<Channel> items)
	{
		super(context, items, Order.DESC);

		this.context = context;
		this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		super.getView(position, convertView, parent);

		Channel channel = (Channel)getItem(position);
		ChannelHolder currentHolder;

		if (convertView == null)
		{
			convertView = this.layoutInflater.inflate(R.layout.channel_list_item, parent, false);
			currentHolder = new ChannelHolder(convertView);
			convertView.setTag(R.id.TAG_VIEW_HOLDER, currentHolder);

			currentHolder.avatarContainer.setOnClickListener(this);
		}
		else
		{
			currentHolder = (ChannelHolder)convertView.getTag(R.id.TAG_VIEW_HOLDER);
		}

		currentHolder.populate(channel, this);

		int avatarCount = channel.getReaders().size();
		for (int index = 0; index < avatarCount; index++)
		{
			if (currentHolder.avatarContainer == null || currentHolder.avatarContainer.getChildAt(index) == null) continue;
			currentHolder.avatarContainer.getChildAt(index).setTag(position);
			currentHolder.avatarContainer.getChildAt(index).setOnClickListener(this);
			currentHolder.avatarContainer.getChildAt(index).setOnLongClickListener(this);
		}

		if (currentHolder.avatarContainer != null)
		{
			currentHolder.avatarContainer.setTag(position);
		}

		/**
		 * Set the animation if it hasn't been played
		 */
		if (mLastPositionAnimated < position && (isAnimationsEnabled() && SettingsManager.isListAnimationEnabled()))
		{
			AnimationHelper.slideUp(convertView);
		}

		if (position > mLastPositionAnimated)
		{
			mLastPositionAnimated = position;
		}

		return convertView;
	}

	@Override public void onClick(View v)
	{
		if (v.getId() == R.id.avatar_container)
		{
			getListView().performItemClick(v.getRootView(), (Integer)v.getTag() + getListView().getHeaderViewsCount(), 0);
		}
		else if (v.getId() == R.id.avatar)
		{
			getListView().performItemClick(v.getRootView(), (Integer)v.getTag() + getListView().getHeaderViewsCount(), 0);
		}
	}

	@Override public boolean onLongClick(View v)
	{
//		if (v.getId() == R.id.avatar)
//		{
//			String userId = (String)v.getTag(R.id.TAG_USER_ID);
//			User u = User.loadUser(userId);
//			((AvatarView)v).triggerLongPress(u);
//
//			return true;
//		}

		return false;
	}

	@Override public boolean onItemLongClick(AdapterView arg0, View v, int position, long arg3)
	{
		// weird conflicts with LinkifiedTextView on Jelly Bean
		if (v instanceof LinkifiedTextView)
		{
			return false;
		}

		final Channel channel = (Channel)getItem(position - getListView().getHeaderViewsCount());

		String subscribeText = channel.isSubscribed() ? getContext().getString(R.string.unsubscribe) : getContext().getString(R.string.subscribe);
		CharSequence[] options = {getContext().getString(R.string.new_message), getContext().getString(R.string.subscribers), subscribeText};

		DialogBuilder.create(getContext())
			.setTitle(R.string.pick_option)
			.setItems(options, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (which == 0)
					{
						Intent messageDialog = new Intent(getContext(), NewMessageDialog.class);
						messageDialog.putExtra(Constants.EXTRA_CHANNEL_ID, channel.getId());
						messageDialog.putExtra(Constants.EXTRA_CHANNEL_NAME, channel.getTitle());
						messageDialog.putExtra(Constants.EXTRA_IS_PUBLIC, channel.isPublic());
						getContext().startActivity(messageDialog);
					}
					else if (which == 1)
					{
						final ArrayList<User> loadedUsers = new ArrayList<User>();
						for (String s : channel.getReaders())
						{
							User u = User.loadUser(s);

							if (u != null)
							{
								loadedUsers.add(u);
							}
						}

						DialogBuilder.create(getContext())
							.setTitle(R.string.subscribers)
							.setAdapter(new AccountAdapter(getContext(), R.layout.user_dialog_list_item, loadedUsers), new DialogInterface.OnClickListener()
							{
								@Override public void onClick(DialogInterface dialog, int which)
								{
									Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
									profileIntent.putExtra(Constants.EXTRA_USER, loadedUsers.get(which));
									getContext().startActivity(profileIntent);
									dialog.dismiss();
								}
							})
							.setNegativeButton(R.string.close, null)
						.show();
					}
					else if (which == 2)
					{
						if (channel.isSubscribed())
						{
							channel.setSubscribed(false);
							APIManager.getInstance().unsubscribeChannel(channel.getId(), null);
						}
						else
						{
							channel.setSubscribed(true);
							APIManager.getInstance().subscribeChannel(channel.getId(), null);
						}
					}
				}
			})
			.show();

		return true;
	}
}