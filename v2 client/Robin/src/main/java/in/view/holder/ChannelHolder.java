package in.view.holder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import in.lib.Constants;
import in.lib.manager.ImageOptionsManager;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.lib.view.FlowLayout;
import in.lib.view.LinkedTextView;
import in.lib.view.TextChronometer;
import in.model.Channel;
import in.rob.client.R;
import in.view.holder.base.Holder;
import lombok.Getter;

@Injectable
public class ChannelHolder extends Holder<Channel>
{
	@Getter @InjectView(R.id.recent_message) protected View recentMessage;
	@Getter @InjectView(R.id.title) protected TextView title;
	@Getter @InjectView(R.id.post_text) protected LinkedTextView postText;
	@Getter @InjectView(R.id.avatar) protected AvatarImageView avatar;
	@Getter @InjectView(R.id.date) protected TextChronometer date;
	@Getter @InjectView(R.id.username_title) protected TextView usernameTitle;
	@Getter @InjectView(R.id.username_subtitle) protected TextView usernameSubtitle;
	@Getter @InjectView(R.id.avatar_container) protected FlowLayout avatarContainer;

	public ChannelHolder(View view)
	{
		super(view);
	}

	@Override public void populate(Channel model)
	{
		if (!TextUtils.isEmpty(model.getTitle()))
		{
			title.setText(model.getTitle());
		}

		recentMessage.setVisibility(View.GONE);
		avatarContainer.setVisibility(View.GONE);

		if (model.getRecentMessage() != null && model.getRecentMessage().getPostText() != null && !model.getRecentMessage().isDeleted())
		{
			postText.setText(model.getRecentMessage().getPostText());
			postText.setLinkMovementMethod();
			postText.setMaxLines(4);
			avatar.setUser(model.getRecentMessage().getPoster());
			date.setTime(model.getRecentMessage().getDate());
			usernameTitle.setText(model.getRecentMessage().getPoster().getFormattedMentionNameTitle());
			usernameSubtitle.setText(model.getRecentMessage().getPoster().getFormattedMentionNameSubTitle());
			recentMessage.setVisibility(View.VISIBLE);
		}

		int count = avatarContainer.getChildCount();
		if (count > 0)
		{
			for (int index = 0; index < count; index++)
			{
				ImageLoader.getInstance().cancelDisplayTask((ImageView)avatarContainer.getChildAt(index));
			}
		}

		avatarContainer.removeAllViews();

		if (model.getReaders() != null && model.getReaders().size() > 2)
		{
			avatarContainer.setVisibility(View.VISIBLE);

			int counter = 0;
			for (final String user : model.getReaders())
			{
				if (model.getRecentMessage() != null && user.equals(model.getRecentMessage().getPoster().getId()))
				{
					continue;
				}

				ImageView image = (ImageView)LayoutInflater.from(avatarContainer.getContext()).inflate(R.layout.channel_avatar_stub, avatarContainer, false);
				avatarContainer.addView(image);

				ImageLoader.getInstance().displayImage(Constants.API_URL + Constants.API_STREAM + Constants.API_VERSION + "users/" + user + "/avatar?avatar=1&w=60&id=" + user, image, ImageOptionsManager.getInstance().getAvatarImageOptions());

				if (counter++ > 15) break;
			}
		}
	}
}
