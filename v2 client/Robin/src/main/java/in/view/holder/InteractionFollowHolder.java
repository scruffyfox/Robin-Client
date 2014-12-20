package in.view.holder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.model.Interaction;
import in.model.User;
import in.rob.client.R;
import lombok.Getter;

@Injectable
public class InteractionFollowHolder extends InteractionPostHolder
{
	@Getter @InjectView(R.id.title) protected TextView title;

	public InteractionFollowHolder(View view)
	{
		super(view);
	}

	@Override public void populate(Interaction model)
	{
		title.setVisibility(View.GONE);
		date.setTime(model.getDate());
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
				title.setVisibility(View.VISIBLE);
			}
			else
			{
				View userStub = LayoutInflater.from(userContainer.getContext()).inflate(R.layout.interaction_stub, userContainer, false);
				userContainer.addView(userStub);

				UserStubHolder holder = new UserStubHolder(userStub);
				holder.populate(user);
				holder.actionText.setText("New follower");
			}
		}
	}
}
