package in.view.holder;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import in.data.annotation.FileAnnotation;
import in.lib.manager.ImageOptionsManager;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.rob.client.R;
import in.view.holder.base.Holder;
import lombok.Getter;

@Injectable
public class FileHolder extends Holder<FileAnnotation>
{
	@Getter @InjectView(R.id.image) protected ImageView image;
	@Getter @InjectView(R.id.progress) protected ProgressBar progress;

	public FileHolder(View view)
	{
		super(view);
	}

	@Override public void populate(FileAnnotation model)
	{
		// reset
		image.setImageBitmap(null);
		ImageLoader.getInstance().cancelDisplayTask(image);

		if (model != null && !TextUtils.isEmpty(model.getThumbUrl()))
		{
			ImageLoader.getInstance().displayImage(model.getThumbUrl(), image, ImageOptionsManager.getInstance().getMediaImageOptions(), new SimpleImageLoadingListener()
			{
				@Override public void onLoadingStarted(String s, View view)
				{
					progress.setVisibility(View.VISIBLE);
				}

				@Override public void onLoadingComplete(String s, View view, Bitmap bitmap)
				{
					progress.setVisibility(View.GONE);
				}
			});
		}
	}
}
