package in.view.holder;

import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import in.data.annotation.CheckinAnnotation;
import in.data.annotation.ImageAnnotation;
import in.lib.Constants;
import in.lib.manager.ImageOptionsManager;
import in.lib.manager.SettingsManager;
import in.lib.utils.BitUtils;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.lib.view.LinkedTextView;
import in.lib.view.TextChronometer;
import in.model.Post;
import in.rob.client.R;
import in.view.holder.base.Holder;
import lombok.Getter;

@Injectable
public class PostHolder extends Holder<Post>
{
	@Getter @InjectView(R.id.avatar) protected AvatarImageView avatar;
	@Getter @InjectView(R.id.date) protected TextChronometer date;
	@Getter @InjectView(R.id.username_title) protected TextView usernameTitle;
	@Getter @InjectView(R.id.username_subtitle) protected TextView usernameSubtitle;
	@Getter @InjectView(R.id.post_text) protected LinkedTextView postText;

	@Getter @InjectView(R.id.conversation_indicator) protected View conversationIndicator;

	@Getter @InjectView(R.id.reposted_by) protected TextView repostedBy;
	@Getter @InjectView(R.id.crosspost) protected TextView crosspost;
	@Getter @InjectView(R.id.checkin) protected TextView checkin;

	@Getter @InjectView(R.id.options_container) protected View optionsContainer;
	@Getter @InjectView(R.id.reply) protected View reply;
	@Getter @InjectView(R.id.reply_all) protected View replyAll;
	@Getter @InjectView(R.id.repost) protected View repost;
	@Getter @InjectView(R.id.star) protected View star;
	@Getter @InjectView(R.id.share) protected View share;
	@Getter @InjectView(R.id.more) protected View more;

	@Getter @InjectView(R.id.media_container) protected View mediaContainer;
	@Getter @InjectView(R.id.media_image) protected ImageView mediaImage;
	@Getter @InjectView(R.id.media_progress) protected ProgressBar mediaProgress;

	public PostHolder(View view)
	{
		super(view);
	}

	@Override public void populate(Post model)
	{
		// reset
		repostedBy.setVisibility(View.GONE);
		crosspost.setVisibility(View.GONE);
		checkin.setVisibility(View.GONE);
		mediaContainer.setVisibility(View.GONE);
		optionsContainer.setVisibility(View.GONE);
		conversationIndicator.setVisibility(View.GONE);
		mediaImage.setImageBitmap(null);

		date.setTime(model.getDate());

		if (model.getReplyCount() > 0 || !TextUtils.isEmpty(model.getReplyTo()))
		{
			conversationIndicator.setVisibility(View.VISIBLE);
		}

		if (model.isRepost())
		{
			repostedBy.setText(String.format("%s %s %s", repostedBy.getResources().getString(R.string.reposted_by), model.getReposter().getFormattedMentionNameTitle(), model.getReposter().getFormattedMentionNameSubTitle()));
			repostedBy.setVisibility(View.VISIBLE);
		}

		if (model.getAnnotations() != null && model.getAnnotations().getCrossposts().size() > 0)
		{
			crosspost.setText(String.format("%s %s", repostedBy.getResources().getString(R.string.crosspost), Uri.parse(model.getAnnotations().getCrossposts().get(0).getCanonicalUrl()).getAuthority()));
			crosspost.setVisibility(View.VISIBLE);
		}

		if (model.getAnnotations() != null && model.getAnnotations().getLocations().size() > 0)
		{
			if (model.getAnnotations().getLocations().get(0) instanceof CheckinAnnotation)
			{
				CheckinAnnotation annotation = (CheckinAnnotation)model.getAnnotations().getLocations().get(0);
				checkin.setText("Check-in at " + annotation.getName() + (annotation.getAddress() == null ? "" : " " + annotation.getAddress()));
				checkin.setVisibility(View.VISIBLE);
			}
		}

		usernameTitle.setText(model.getPoster().getFormattedMentionNameTitle());
		usernameSubtitle.setText(model.getPoster().getFormattedMentionNameSubTitle());
		postText.setText(model.getPostText());
		postText.setLinkMovementMethod();
		avatar.setUser(model.getPoster());

		ImageLoader.getInstance().cancelDisplayTask(mediaImage);
		boolean showImage = BitUtils.contains(SettingsManager.getInstance().getShowHideBit(), Constants.BIT_SHOWHIDE_INLINE_IMAGES);

		if (showImage && model.getAnnotations() != null && model.getAnnotations().getImages() != null && model.getAnnotations().getImages().size() > 0)
		{
			ImageAnnotation image = (ImageAnnotation)model.getAnnotations().getImages().get(0);
			ImageLoader.getInstance().displayImage(image.getPreviewUrl(), mediaImage, ImageOptionsManager.getInstance().getInlineMediaImageOptions());
			mediaContainer.setVisibility(View.VISIBLE);
		}
	}
}
