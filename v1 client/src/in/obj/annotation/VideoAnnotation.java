package in.obj.annotation;

import in.lib.utils.URLUtils;
import lombok.ToString;
import android.net.Uri;

@ToString(callSuper=true) public class VideoAnnotation extends ImageAnnotation
{
	private static final long serialVersionUID = 8987571728715831243L;

	@Override public void setUrl(String url)
	{
		this.url = url;

		Uri uri = Uri.parse(url);
		if (URLUtils.isYoutubeVideo(uri))
		{
			setPreviewUrl(URLUtils.getYoutubeThumbnail(uri));
		}
	}
}