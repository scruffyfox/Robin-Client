package in.data.annotation;

import android.net.Uri;
import android.os.Parcel;

import com.google.gson.JsonElement;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.lib.utils.URLUtils;
import lombok.Data;

@Data
public class VideoAnnotation extends ImageAnnotation
{
	@Override public void setUrl(String url)
	{
		this.url = url;

		Uri uri = Uri.parse(url);
		if (URLUtils.isYoutubeVideo(uri))
		{
			setPreviewUrl(URLUtils.getYoutubeThumbnail(uri));
		}
	}

	@Override public VideoAnnotation createFrom(JsonElement element)
	{
		super.createFrom(element);
		return this;
	}

	@Override public VideoAnnotation createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "7395328a-4ee4-48f5-a7e9-c1d06bb0770a";
	}

	@Override public VideoAnnotation read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public void write(SerialWriterUtil util)
	{
		super.write(util);

		try
		{
			util.writeString(getVersion());
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public static final Creator<VideoAnnotation> CREATOR = new Creator<VideoAnnotation>()
	{
		@Override public VideoAnnotation[] newArray(int size)
		{
			return new VideoAnnotation[size];
		}

		@Override public VideoAnnotation createFromParcel(Parcel source)
		{
			return new VideoAnnotation().createFrom(source);
		}
	};
}