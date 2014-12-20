package in.view.holder;

import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import in.data.annotation.CheckinAnnotation;
import in.data.annotation.ImageAnnotation;
import in.lib.manager.ImageOptionsManager;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.LinkTouchMovementMethod;
import in.model.Post;
import in.rob.client.R;
import lombok.Getter;

@Injectable
public class ThreadPostSelectedHolder extends ThreadPostHolder
{
	@Getter @InjectView(R.id.star_count) protected TextView starCount;
	@Getter @InjectView(R.id.repost_count) protected TextView repostCount;

	@Getter @InjectView(R.id.reposted_by) protected TextView repostedBy;
	@Getter @InjectView(R.id.crosspost) protected TextView crosspost;
	@Getter @InjectView(R.id.checkin) protected TextView checkin;

	public ThreadPostSelectedHolder(View view)
	{
		super(view);
	}

	@Override public void populate(Post model)
	{
		super.populate(model);

		repostedBy.setVisibility(View.GONE);
		crosspost.setVisibility(View.GONE);
		checkin.setVisibility(View.GONE);

		postText.setLinkMovementMethod(new LinkTouchMovementMethod(null));
		mediaContainer.setVisibility(View.GONE);
		mediaImage.setImageBitmap(null);

		optionsContainer.setVisibility(View.VISIBLE);
		starCount.setText(String.valueOf(model.getStarCount()));
		repostCount.setText(String.valueOf(model.getRepostCount()));

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

		ImageLoader.getInstance().cancelDisplayTask(getMediaImage());

		if (model.getAnnotations() != null && model.getAnnotations().getImages() != null && model.getAnnotations().getImages().size() > 0)
		{
			ImageAnnotation image = (ImageAnnotation)model.getAnnotations().getImages().get(0);
			ImageLoader.getInstance().displayImage(image.getPreviewUrl(), mediaImage, ImageOptionsManager.getInstance().getCenterPostMediaOptions());
			mediaContainer.setVisibility(View.VISIBLE);
		}
	}
}
