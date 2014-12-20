package in.view.holder;

import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import in.lib.manager.ImageOptionsManager;
import in.lib.manager.UserManager;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.lib.view.LinkTouchMovementMethod;
import in.lib.view.LinkedTextView;
import in.model.User;
import in.rob.client.R;
import in.view.holder.base.Holder;

@Injectable
public class ProfileHeaderHolder extends Holder<User>
{
	@InjectView private TextView usernameTitle;
	@InjectView private TextView usernameSubtitle;
	@InjectView private TextView followsYou;
	@InjectView private TextView followCount;
	@InjectView private LinkedTextView bio;
	@InjectView private AvatarImageView avatar;
	@InjectView private ImageView cover;
	@InjectView private Button followButton;
	@InjectView private Button editButton;

	public ProfileHeaderHolder(View view)
	{
		super(view);
	}

	@Override public void populate(User model)
	{
		avatar.setUser(model);

		usernameTitle.setText(model.getFormattedMentionNameTitle());
		usernameSubtitle.setText(model.getFormattedMentionNameSubTitle());
		bio.setText(model.getDescription());
		bio.setLinkMovementMethod(LinkTouchMovementMethod.getInstance());

		StringBuilder followCountText = new StringBuilder();
		followCountText.append("<b>").append(model.getFollowingCount()).append("</b> following<br />");
		followCountText.append("<b>").append(model.getFollowerCount()).append("</b> followers<br />");
		followCountText.append("<b>").append(model.getStarredCount()).append("</b> starred<br />");
		followCountText.append("<b>").append(model.getPostCount()).append("</b> posts");
		followCount.setText(Html.fromHtml(followCountText.toString()));
		followsYou.setText(model.isFollower() ? R.string.follows_you : R.string.doesnt_follow_you);

		if (UserManager.getInstance().getUser().equals(model))
		{
			editButton.setVisibility(View.VISIBLE);
			followButton.setVisibility(View.GONE);
		}
		else
		{
			editButton.setVisibility(View.GONE);
			followButton.setVisibility(View.VISIBLE);
		}

		ImageLoader.getInstance().cancelDisplayTask(cover);

		if (!model.isCoverDefault())
		{
			ImageLoader.getInstance().displayImage(model.getCoverUrl(), cover, ImageOptionsManager.getInstance().getCoverImageOptions());
		}
	}
}
