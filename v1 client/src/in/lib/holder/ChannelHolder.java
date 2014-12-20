package in.lib.holder;

import in.lib.adapter.ChannelAdapter;
import in.lib.annotation.InjectView;
import in.lib.helper.ThemeHelper;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.lib.view.LinkifiedTextView;
import in.model.Channel;
import in.model.Channel.Type;
import in.model.SimpleUser;
import in.rob.client.MainApplication;
import in.rob.client.R;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * View holder for list item which holds references
 * to the views so its like uber quick
 * @author Robin
 */
public class ChannelHolder
{
	public View view;
	@InjectView(R.id.title) public TextView title;
	@InjectView(R.id.time) public TextView time;
	@InjectView(R.id.avatar_container) public ViewGroup avatarContainer;
	@InjectView(R.id.text) public LinkifiedTextView text;
	@InjectView(R.id.padlock) public View padlock;

	public ChannelHolder(View convertView)
	{
		view = convertView;
		Views.inject(this, convertView);
	}

	/**
	 * Method to use when populating a view's members with the
	 * data from post.
	 * @param inflater The inflater to use when creating new views
	 * @param holder The holder containing the views
	 * @param post The post object
	 */
	public void populate(Channel channel, final ChannelAdapter adapter)
	{
		StringBuilder channelTitle = new StringBuilder(channel.getReaders().size() * 20);
		padlock.setVisibility(View.GONE);
		avatarContainer.removeAllViews();
		time.setText("");

		if (channel.isUnread())
		{
			int newItem = ThemeHelper.getDrawableResource(view.getContext(), R.attr.rbn_channel_new_item_bg);
			view.setBackgroundResource(newItem);
		}
		else
		{
			int normal = ThemeHelper.getDrawableResource(view.getContext(), R.attr.rbn_channel_item_bg);
			view.setBackgroundResource(normal);
		}

		if (channel.getType() == Type.PRIVATE_MESSAGE)
		{
			padlock.setVisibility(View.VISIBLE);
		}

		text.setLinkMovementMethod(null);

		if (channel.getRecentMessage() != null && !channel.getRecentMessage().getId().equals("-1"))
		{
			time.setText(channel.getRecentMessage().getDateStr());
			text.setText("</b><a href=\"\">@" + channel.getRecentMessage().getPoster().getMentionName() + "</a>:</b> " + channel.getRecentMessage().getFormattedText());
			text.setVisibility(View.VISIBLE);
		}
		else
		{
			time.setText("");
			text.setVisibility(View.GONE);
		}

		// avatar
		if (SettingsManager.getShowAvatars())
		{
			avatarContainer.setVisibility(View.VISIBLE);

			if (channel.getUsers() != null && channel.getUsers().size() > 0)
			{
				for (final SimpleUser user : channel.getUsers())
				{
					if (user == null || user.getId().equals("-1")) continue;

					if (channelTitle.length() > 0)
					{
						channelTitle.append(", ");
					}

					channelTitle.append("@").append(user.getMentionName());
					ImageView image = (ImageView)adapter.getLayoutInflater().inflate(R.layout.channel_list_stub_avatar, avatarContainer, false);
					avatarContainer.addView(image);
					image.setTag(R.id.TAG_USER_ID, user);
					image.setContentDescription(user.getMentionName());

					ImageLoader.getInstance().displayImage(user.getAvatarUrl() + "?avatar=1&id=" + user.getId(), image, MainApplication.getAvatarImageOptions());
				}
			}
		}
		else
		{
			if (channel.getUsers() != null && channel.getUsers().size() > 0)
			{
				for (final SimpleUser user : channel.getUsers())
				{
					if (user == null || user.getId().equals("-1")) continue;

					if (channelTitle.length() > 0)
					{
						channelTitle.append(", ");
					}

					channelTitle.append("@").append(user.getMentionName());
				}
			}

			avatarContainer.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(channel.getTitle()))
		{
			channelTitle = new StringBuilder(channelTitle.length() + 20).append(channel.getTitle()).append(" - ").append(channelTitle);
		}

		title.setText(channelTitle.toString());

		if (SettingsManager.isCustomFontsEnabled())
		{
			title.setTypeface(Typeface.defaultFromStyle(0));
			text.setTypeface(Typeface.defaultFromStyle(0));
		}
	}
}