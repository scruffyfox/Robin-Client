package in.lib.manager;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import in.rob.client.R;
import lombok.Getter;

public class ImageOptionsManager
{
	@Getter private final DisplayImageOptions avatarImageOptions;
	@Getter private final DisplayImageOptions coverImageOptions;
	@Getter private final DisplayImageOptions mediaImageOptions;
	@Getter private final DisplayImageOptions inlineMediaImageOptions;
	@Getter private final DisplayImageOptions centerPostMediaOptions;
	@Getter private final DisplayImageOptions threadAvatarImageOptions;

	private static ImageOptionsManager instance;

	public static ImageOptionsManager getInstance()
	{
		if (instance == null)
		{
			synchronized (ImageOptionsManager.class)
			{
				if (instance == null)
				{
					instance = new ImageOptionsManager();
				}
			}
		}

		return instance;
	}

	private ImageOptionsManager()
	{
		ImageFader avatarFader = new ImageFader(400);

		Builder avatarImageOptionsBuilder = new DisplayImageOptions.Builder();
		avatarImageOptionsBuilder.cacheInMemory(true);
		avatarImageOptionsBuilder.cacheOnDisc(true);
		avatarImageOptionsBuilder.displayer(avatarFader);
		avatarImageOptionsBuilder.bitmapConfig(Config.RGB_565);
		avatarImageOptionsBuilder.showStubImage(R.drawable.default_avatar);
		avatarImageOptionsBuilder.showImageForEmptyUri(R.drawable.default_avatar);
		avatarImageOptionsBuilder.imageScaleType(ImageScaleType.EXACTLY_STRETCHED);
		avatarImageOptionsBuilder.resetViewBeforeLoading(true);

		Builder coverImageOptionsBuilder = new DisplayImageOptions.Builder();
		coverImageOptionsBuilder.cacheInMemory(true);
		coverImageOptionsBuilder.cacheOnDisc(true);
		coverImageOptionsBuilder.showStubImage(R.drawable.default_cover);
		coverImageOptionsBuilder.showImageForEmptyUri(R.drawable.default_cover);
		coverImageOptionsBuilder.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2);
		coverImageOptionsBuilder.resetViewBeforeLoading(true);

		Builder threadAvatarImageOptionsBuilder = new DisplayImageOptions.Builder();
		threadAvatarImageOptionsBuilder.cacheInMemory(true);
		threadAvatarImageOptionsBuilder.cacheOnDisc(true);
		threadAvatarImageOptionsBuilder.bitmapConfig(Config.RGB_565);
		threadAvatarImageOptionsBuilder.showStubImage(R.drawable.default_avatar);
		threadAvatarImageOptionsBuilder.showImageForEmptyUri(R.drawable.default_avatar);
		threadAvatarImageOptionsBuilder.imageScaleType(ImageScaleType.EXACTLY_STRETCHED);
		threadAvatarImageOptionsBuilder.resetViewBeforeLoading(true);

		Builder mediaImageOptionsBuilder = new DisplayImageOptions.Builder();
		mediaImageOptionsBuilder.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2);
		mediaImageOptionsBuilder.bitmapConfig(Config.RGB_565);
		mediaImageOptionsBuilder.resetViewBeforeLoading(true);
		mediaImageOptionsBuilder.cacheInMemory(true);
		mediaImageOptionsBuilder.cacheOnDisc(true);

		Builder inlineMediaImageOptionsBuilder = new DisplayImageOptions.Builder();
		inlineMediaImageOptionsBuilder.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2);
		inlineMediaImageOptionsBuilder.bitmapConfig(Config.RGB_565);
		inlineMediaImageOptionsBuilder.resetViewBeforeLoading(true);
		inlineMediaImageOptionsBuilder.displayer(avatarFader);
		inlineMediaImageOptionsBuilder.cacheInMemory(true);

		Builder centerPostMediaOptionsBuilder = new DisplayImageOptions.Builder();
		centerPostMediaOptionsBuilder.imageScaleType(ImageScaleType.EXACTLY_STRETCHED);
		centerPostMediaOptionsBuilder.bitmapConfig(Config.RGB_565);
		centerPostMediaOptionsBuilder.cacheInMemory(true);

		avatarImageOptions = avatarImageOptionsBuilder.build();
		coverImageOptions = coverImageOptionsBuilder.build();
		threadAvatarImageOptions = threadAvatarImageOptionsBuilder.build();
		mediaImageOptions = mediaImageOptionsBuilder.build();
		inlineMediaImageOptions = inlineMediaImageOptionsBuilder.build();
		centerPostMediaOptions = centerPostMediaOptionsBuilder.build();
	}

	public static class ImageFader extends FadeInBitmapDisplayer
	{
		public ImageFader(int delay)
		{
			super(delay);
		}

		@Override public Bitmap display(Bitmap bitmap, ImageView imageView, LoadedFrom loadedFrom)
		{
			if (loadedFrom != LoadedFrom.MEMORY_CACHE)
			{
				return super.display(bitmap, imageView, loadedFrom);
			}
			else
			{
				imageView.setImageBitmap(bitmap);
				return bitmap;
			}
		}
	}
}
