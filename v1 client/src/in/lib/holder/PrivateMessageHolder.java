package in.lib.holder;

import in.lib.adapter.PrivateMessageAdapter;
import in.lib.annotation.InjectView;
import in.lib.holder.base.MessageHolder;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.model.PrivateMessage;
import in.obj.annotation.Annotation;
import in.obj.annotation.Annotation.Type;
import in.obj.annotation.ImageAnnotation;
import in.obj.annotation.VideoAnnotation;
import in.rob.client.MainApplication;
import in.rob.client.R;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

/**
 * View holder for list item which holds references
 * to the views so its like uber quick
 * @author Robin
 */
public class PrivateMessageHolder extends MessageHolder
{
	@InjectView(R.id.location_image) public ImageView locationImage;
	@InjectView(R.id.is_convo) public ImageView isInConvo;
	@InjectView(R.id.star) public ImageView starButton;
	@InjectView(R.id.repost) public ImageView repostButton;
	@InjectView(R.id.more) public ImageView moreButton;
	@InjectView(R.id.media_image) public ImageView media;
	@InjectView(R.id.delete) public ImageView deleteButton;

	public PrivateMessageHolder(View convertView)
	{
		super(convertView);
		Views.inject(this, convertView);
	}

	@Override public void onViewDestroyed(View v)
	{
		super.onViewDestroyed(v);

		if (media != null)
		{
			ImageLoader.getInstance().cancelDisplayTask(media);
			media.setImageBitmap(null);
		}

		if (locationImage != null)
		{
			ImageLoader.getInstance().cancelDisplayTask(locationImage);
			locationImage.setImageBitmap(null);
		}
	}

	/**
	 * Method to use when populating a view's members with the
	 * data from message.
	 * @param inflater The inflater to use when creating new views
	 * @param holder The holder containing the views
	 * @param post The post object
	 */
	public void populate(PrivateMessage message, PrivateMessageAdapter adapter)
	{
		super.populate(message, adapter);

		mediaContainer.setVisibility(View.GONE);

		if (message.getPoster().isYou())
		{
			deleteButton.setVisibility(View.VISIBLE);
		}
		else
		{
			deleteButton.setVisibility(View.GONE);
		}

		boolean block = SettingsManager.isInlineImagesEnabled();
		block &= (!SettingsManager.isInlineImageWifiEnabled() || (SettingsManager.isInlineImageWifiEnabled() && MainApplication.isOnWifi()));
		block &= media != null;

		if (block)
		{
			String imageToLoad = "";
			boolean centerMessage = adapter.getCenter() == message;

			if (message.getAnnotations() != null)
			{
				if (message.getAnnotations().get(Type.IN_ORDER) != null && message.getAnnotations().get(Type.IN_ORDER).size() > 0)
				{
					Annotation image = message.getAnnotations().get(Type.IN_ORDER).get(0);
					imageToLoad = image.getPreviewUrl();

					if (!TextUtils.isEmpty(imageToLoad))
					{
						ImageLoader.getInstance().displayImage(imageToLoad, media, (centerMessage ? MainApplication.getCenterPostMediaOptions() : MainApplication.getInlineMediaImageOptions()), new SimpleImageLoadingListener()
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