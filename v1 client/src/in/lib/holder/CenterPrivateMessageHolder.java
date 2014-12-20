package in.lib.holder;

import in.lib.adapter.PrivateMessageAdapter;
import in.lib.annotation.InjectView;
import in.lib.manager.SettingsManager;
import in.lib.utils.Dimension;
import in.lib.utils.URLUtils;
import in.lib.utils.Views;
import in.lib.view.LinkTouchMovementMethod;
import in.model.PrivateMessage;
import in.obj.annotation.Annotation.Type;
import in.obj.annotation.LocationAnnotation;
import in.rob.client.MainApplication;
import in.rob.client.R;
import lombok.ToString;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

@ToString(callSuper = true)
public class CenterPrivateMessageHolder extends PrivateMessageHolder
{
	@InjectView(R.id.location_container) public View locationContainer;
	@InjectView(R.id.location_image) public ImageView locationImage;
	@InjectView(R.id.location_progress) public ProgressBar locationProgress;
	@InjectView(R.id.replies_container) public View repliesContainer;
	@InjectView(R.id.reply_to_container) public View replyToContainer;
	@InjectView(R.id.user_container) public View userContainer;

	public CenterPrivateMessageHolder(View convertView)
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
	@Override public void populate(PrivateMessage message, PrivateMessageAdapter adapter)
	{
		super.populate(message, adapter);

		repliesContainer.setVisibility(View.GONE);
		replyToContainer.setVisibility(View.GONE);
		locationContainer.setVisibility(View.GONE);
		optionsContainer.setVisibility(View.VISIBLE);
		deleteButton.setVisibility(View.GONE);
		text.setLinkMovementMethod(new LinkTouchMovementMethod());

		if (adapter.indexOf(message) < adapter.getCount() - 1)
		{
			repliesContainer.setVisibility(View.VISIBLE);
		}

		if (adapter.indexOf(message) > 0)
		{
			replyToContainer.setVisibility(View.VISIBLE);
		}

		if (message.getPoster().isYou())
		{
			deleteButton.setVisibility(View.VISIBLE);
		}

		boolean block = !SettingsManager.isInlineImageWifiEnabled() || (SettingsManager.isInlineImageWifiEnabled() && MainApplication.isOnWifi());
		block &= message.getAnnotations() != null;

		if (block)
		{
			if (message.getAnnotations().get(Type.LOCATION) != null && message.getAnnotations().get(Type.LOCATION).size() > 0)
			{
				LocationAnnotation location = (LocationAnnotation)message.getAnnotations().get(Type.LOCATION).get(0);
				Dimension d = new Dimension(locationImage.getContext());
				String mapImage = URLUtils.getMapThumbnail(location.getLat(), location.getLng(), d.getScreenWidth(), d.densityPixel(100));

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

				locationContainer.setVisibility(View.VISIBLE);
				locationImage.setTag(R.id.TAG_ENTITY, location);
			}
		}
	}
}