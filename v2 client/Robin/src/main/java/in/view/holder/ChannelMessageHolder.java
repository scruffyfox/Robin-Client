package in.view.holder;

import android.view.View;
import android.widget.TextView;

import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.lib.view.LinkedTextView;
import in.lib.view.TextChronometer;
import in.model.ChannelMessage;
import in.rob.client.R;
import in.view.holder.base.Holder;
import lombok.Getter;

@Injectable
public class ChannelMessageHolder extends Holder<ChannelMessage>
{
	@Getter @InjectView(R.id.avatar) protected AvatarImageView avatar;
	@Getter @InjectView(R.id.date) protected TextChronometer date;
	@Getter @InjectView(R.id.username_title) protected TextView usernameTitle;
	@Getter @InjectView(R.id.username_subtitle) protected TextView usernameSubtitle;
	@Getter @InjectView(R.id.post_text) protected LinkedTextView postText;

	public ChannelMessageHolder(View view)
	{
		super(view);
	}

	@Override public void populate(ChannelMessage model)
	{
		date.setTime(model.getDate());

		usernameTitle.setText(model.getPoster().getFormattedMentionNameTitle());
		usernameSubtitle.setText(model.getPoster().getFormattedMentionNameSubTitle());
		postText.setText(model.getPostText());
		postText.setLinkMovementMethod();
		avatar.setUser(model.getPoster());
	}
}
