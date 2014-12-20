package in.lib.holder;

import in.lib.adapter.PostAdapter;
import in.lib.annotation.InjectView;
import in.lib.manager.SettingsManager;
import in.lib.utils.Dimension;
import in.lib.utils.URLUtils;
import in.lib.utils.Views;
import in.lib.view.LinkTouchMovementMethod;
import in.model.Post;
import in.obj.annotation.Annotation;
import in.obj.annotation.Annotation.Type;
import in.obj.annotation.LocationAnnotation;
import in.obj.annotation.VideoAnnotation;
import in.rob.client.MainApplication;
import in.rob.client.R;
import lombok.ToString;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

@ToString(callSuper = true) public class CenterPostHolder extends PostHolder
{
	@InjectView(R.id.location_container) public View locationContainer;
	@InjectView(R.id.location_image) public ImageView locationImage;
	@InjectView(R.id.location_progress) public ProgressBar locationProgress;
	@InjectView(R.id.replies_container) public View repliesContainer;
	@InjectView(R.id.reply_to_container) public View replyToContainer;
	@InjectView(R.id.repost_count) public TextView repostCount;
	@InjectView(R.id.starred_count) public TextView starredCount;
	@InjectView(R.id.user_container) public View userContainer;

	public CenterPostHolder(View convertView)
	{
		super(convertView);
		Views.inject(this, convertView);
	}

	/**
	 * Method to use when populating a view's members with the
	 * data from post.
	 * @param inflater The inflater to use when creating new views
	 * @param holder The holder containing the views
	 * @param post The post object
	 */
	@Override public void populate(Post post, PostAdapter adapter, boolean inThread)
	{
		super.populate(post, adapter, inThread);

		isInConvo.setVisibility(View.GONE);
		locationContainer.setVisibility(View.GONE);
		repliesContainer.setVisibility(View.GONE);
		replyToContainer.setVisibility(View.GONE);
		optionsContainer.setVisibility(View.VISIBLE);

		text.setLinkMovementMethod(new LinkTouchMovementMethod());
		repostCount.setText(post.getRepostCount() + " ");
		starredCount.setText(post.getStarCount() + " ");

		if (adapter.indexOf(post) < adapter.getCount() - 1)
		{
			repliesContainer.setVisibility(View.VISIBLE);
		}

		if (adapter.indexOf(post) > 0)
		{
			replyToContainer.setVisibility(View.VISIBLE);
		}

		boolean block = !SettingsManager.isInlineImageWifiEnabled() || (SettingsManager.isInlineImageWifiEnabled() && MainApplication.isOnWifi());
		block &= post.getAnnotations() != null;

		if (block)
		{
			if (post.getAnnotations().get(Type.IN_ORDER) != null && post.getAnnotations().get(Type.IN_ORDER).size() > 0)
			{
				String imageToLoad = "";
				Annotation image = post.getAnnotations().get(Type.IN_ORDER).get(0);
				imageToLoad = image.getPreviewUrl();

				if (!TextUtils.isEmpty(imageToLoad))
				{
					media.setImageBitmap(null);

					if (adapter.isReady())
					{
						ImageLoader.getInstance().displayImage(imageToLoad, this.media, MainApplication.getCenterPostMediaOptions(), new SimpleImageLoadingListener()
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
					}

					media.setTag(R.id.TAG_ENTITY, image);
					media.setTag(R.id.TAG_IMAGE_URL, imageToLoad);

					if (image instanceof VideoAnnotation || image.getPreviewUrl().endsWith(".gif"))
					{
						videoMediaButton.setVisibility(View.VISIBLE);
					}

					mediaContainer.setVisibility(View.VISIBLE);
				}
			}

			if (post.getAnnotations().get(Type.LOCATION) != null && post.getAnnotations().get(Type.LOCATION).size() > 0)
			{
				LocationAnnotation location = (LocationAnnotation)post.getAnnotations().get(Type.LOCATION).get(0);
				Dimension d = new Dimension(locationImage.getContext());
				String mapImage = URLUtils.getMapThumbnail(location.getLat(), location.getLng(), d.getScreenWidth(), d.densityPixel(100));

				if (adapter.isReady())
				{
					ImageLoader.getInstance().displayImage(mapImage, locationImage, MainApplication.getCenterPostMediaOptions(), new SimpleImageLoadingListener()
					{
						@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
						{
							locationProgress.setVisibility(View.GONE);
						}

						@Override public void onLoadingStarted(String imageUri, View view)
						{
							locationProgress.setVisibility(View.VISIBLE);
						}
					});
				}

				locationContainer.setVisibility(View.VISIBLE);
				locationImage.setTag(R.id.TAG_ENTITY, location);
			}
		}
	}
}