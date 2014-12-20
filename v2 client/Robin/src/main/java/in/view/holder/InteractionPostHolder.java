package in.view.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.lib.view.FlowLayout;
import in.lib.view.LinkedTextView;
import in.lib.view.TextChronometer;
import in.model.Interaction;
import in.model.Interaction.Type;
import in.model.Post;
import in.model.User;
import in.rob.client.R;
import in.view.holder.base.Holder;
import lombok.Getter;

@Injectable
public class InteractionPostHolder extends Holder<Interaction>
{
	@Getter @InjectView(R.id.user_container) protected FlowLayout userContainer;
	@Getter @InjectView(R.id.post_text) protected LinkedTextView postText;
	@Getter @InjectView(R.id.date) protected TextChronometer date;
	@Getter @InjectView(R.id.icon) protected ImageView icon;

	public InteractionPostHolder(View view)
	{
		super(view);
	}

	@Override public void populate(Interaction model)
	{
		date.setTime(model.getDate());
		postText.setText(((Post)model.getObject()).getPostText());
		postText.setLinkMovementMethod();
		icon.setImageResource(model.getType().getIcon());

		userContainer.removeAllViews();
		int count = model.getUsers().size();

		for (User user : model.getUsers())
		{
			if (count > 1)
			{
				AvatarImageView userStub = (AvatarImageView)LayoutInflater.from(userContainer.getContext()).inflate(R.layout.interaction_avatar_stub, userContainer, false);
				userStub.setUser(user);
				userContainer.addView(userStub);
			}
			else
			{
				View userStub = LayoutInflater.from(userContainer.getContext()).inflate(R.layout.interaction_stub, userContainer, false);
				userContainer.addView(userStub);

				UserStubHolder holder = new UserStubHolder(userStub);
				holder.populate(user);

				if (model.getType() == Type.REPOST)
				{
					holder.actionText.setText("Reposted by");
				}
				else if (model.getType() == Type.STAR)
				{
					holder.actionText.setText("Starred by");
				}
			}
		}
	}

	@Injectable
	protected static class UserStubHolder
	{
		@Getter @InjectView protected TextView usernameTitle;
		@Getter @InjectView protected TextView usernameSubtitle;
		@Getter @InjectView protected AvatarImageView avatar;
		@Getter @InjectView protected TextView actionText;

		public UserStubHolder(View v)
		{
			Views.inject(this, v);
		}

		public void populate(User user)
		{
			avatar.setUser(user);

			usernameTitle.setText(user.getFormattedMentionNameTitle());
			usernameSubtitle.setText(user.getFormattedMentionNameSubTitle());
		}
	}
}
