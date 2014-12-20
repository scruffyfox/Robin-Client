package in.lib.holder;
import in.lib.adapter.PostAdapter;
import in.lib.annotation.InjectView;
import in.lib.helper.ThemeHelper;
import in.lib.holder.base.MessageHolder;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.model.Post;
import in.obj.annotation.Annotation;
import in.obj.annotation.Annotation.Type;
import in.obj.annotation.ImageAnnotation;
import in.obj.annotation.VideoAnnotation;
import in.rob.client.MainApplication;
import in.rob.client.R;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;


/**
 * View holder for list item which holds references
 * to the views so its like uber quick
 * @author Robin
 */
public class PostHolder extends MessageHolder
{
	@InjectView(R.id.reposted_by) public TextView repostedBy;
	@InjectView(R.id.crosspost) public TextView crosspost;
	@InjectView(R.id.is_convo) public ImageView isInConvo;
	@InjectView(R.id.star) public ImageView starButton;
	@InjectView(R.id.repost) public ImageView repostButton;
	@InjectView(R.id.more) public ImageView moreButton;
	@InjectView(R.id.media_image) public ImageView media;
	@InjectView(R.id.missing_posts) public View missingPosts;
	@InjectView(R.id.missing_posts_top) public View missingPostsTop;
	@InjectView(R.id.stream_marker) public View streamMarker;

	public PostHolder(View convertView)
	{
		super(convertView);
		Views.inject(this, convertView);
	}

	@Override public void onViewDestroyed(View v)
	{
		super.onViewDestroyed(v);

		if (media != null)
		{
			media.setImageBitmap(null);
			ImageLoader.getInstance().cancelDisplayTask(media);
			//media.setTag(null);
		}
	}

	/**
	 * Method to use when populating a view's members with the
	 * data from post.
	 * @param holder The holder containing the views
	 * @param post The post object
	 */
	public void populate(Post post, PostAdapter adapter, boolean inThread)
	{
		super.populate(post, adapter);

		if (media != null)
		{
			ImageLoader.getInstance().cancelDisplayTask(media);
			media.setImageBitmap(null);
			mediaContainer.setVisibility(View.GONE);
		}

		if (streamMarker != null)
		{
			streamMarker.setVisibility(View.GONE);
			if (SettingsManager.isStreamMarkerEnabled())
			{
				if (adapter.getStreamMarker().getId().equals(post.getId()))
				{
					streamMarker.setVisibility(View.VISIBLE);
				}
			}
		}

		if (post.getHasReplies() && !inThread)
		{
			isInConvo.setVisibility(View.VISIBLE);
		}
		else
		{
			isInConvo.setVisibility(View.GONE);
		}

		if (post.isRepost() && post.getReposter() != null && !inThread)
		{
			repostedBy.setVisibility(View.VISIBLE);
			String repostedStr = new StringBuilder()
				.append(repostedBy.getContext().getString(R.string.reposted_by))
				.append(" @")
				.append(post.getReposter().getMentionName())
				.append(" ")
				.append(post.getReposter().getUserName())
			.toString();
			repostedBy.setText(repostedStr);
		}
		else
		{
			repostedBy.setVisibility(View.GONE);
		}

		if (post.isCrossPost() && !TextUtils.isEmpty(post.getCrossPostUrl()))
		{
			String crosspostStr = new StringBuilder()
				.append(crosspost.getContext().getString(R.string.crosspost))
				.append(" ")
				.append(Uri.parse(post.getCrossPostUrl()).getHost())
			.toString();
			crosspost.setVisibility(View.VISIBLE);
			crosspost.setText(crosspostStr);
		}
		else
		{
			crosspost.setVisibility(View.GONE);
		}

		if (post.getPoster().isYou())
		{
			repostButton.setVisibility(View.GONE);
		}

		if (post.isStarred())
		{
			int starred = ThemeHelper.getDrawableResource(starButton.getContext(), R.attr.rbn_icon_starred);
			starButton.setImageResource(starred);
		}
		else
		{
			int unstarred = ThemeHelper.getDrawableResource(starButton.getContext(), R.attr.rbn_icon_unstarred);
			starButton.setImageResource(unstarred);
		}

		ImageLoader.getInstance().cancelDisplayTask(media);
		//media.setImageBitmap(null);

		boolean block = SettingsManager.isInlineImagesEnabled();
		block &= (!SettingsManager.isInlineImageWifiEnabled() || (SettingsManager.isInlineImageWifiEnabled() && MainApplication.isOnWifi()));
		block &= !inThread && media != null;

		if (block)
		{
			String imageToLoad = "";

			if (post.getAnnotations() != null)
			{
				if (post.getAnnotations().get(Type.IN_ORDER) != null && post.getAnnotations().get(Type.IN_ORDER).size() > 0)
				{
					Annotation image = post.getAnnotations().get(Type.IN_ORDER).get(0);
					imageToLoad = image.getPreviewUrl();

					if (!TextUtils.isEmpty(imageToLoad))
					{
						ImageLoader.getInstance().displayImage(imageToLoad, media, MainApplication.getInlineMediaImageOptions(), new SimpleImageLoadingListener()
						{
							@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
							{
								mediaProgress.setVisibility(View.GONE);
							}

							@Override public void onLoadingStarted(String imageUri, View view)
							{
								mediaProgress.setVisibility(View.VISIBLE);
							}
						});

						media.setTag(R.id.TAG_ENTITY, image);
						media.setTag(R.id.TAG_IMAGE_URL, imageToLoad);

						if (image.getClass() == ImageAnnotation.class)
						{
							videoMediaButton.setVisibility(View.GONE);
						}

						if (image.getClass() == VideoAnnotation.class || image.getPreviewUrl().endsWith(".gif"))
						{
							videoMediaButton.setVisibility(View.VISIBLE);
						}

						mediaContainer.setVisibility(View.VISIBLE);
					}
				}
			}
		}
	}
}