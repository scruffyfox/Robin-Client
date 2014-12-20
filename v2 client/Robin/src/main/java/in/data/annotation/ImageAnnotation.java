package in.data.annotation;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.lib.utils.URLUtils;
import in.model.base.Model;
import lombok.Data;

@Data
public class ImageAnnotation extends Annotation
{
	protected int width = 0, height = 0;
	protected String url = "";
	protected String textUrl = "";
	protected String thumbUrl = "";
	protected int thumbWidth = 0, thumbHeight = 0;
	protected String embeddableUrl = "";

	@Override public JsonElement toAnnotation()
	{
		JsonObject object = new JsonObject();
		JsonObject values = new JsonObject();

		values.addProperty("url", getUrl());
		values.addProperty("width", getWidth());
		values.addProperty("height", getHeight());
		values.addProperty("version", "1.0");
		values.addProperty("type", "photo");

		if (!TextUtils.isEmpty(getThumbUrl()))
		{
			values.addProperty("thumbnail_url", getThumbUrl());
			values.addProperty("thumbnail_width", getThumbWidth());
			values.addProperty("thumbnail_height", getThumbHeight());
		}

		if (!TextUtils.isEmpty(getEmbeddableUrl()))
		{
			values.addProperty("embeddable_url", getEmbeddableUrl());
		}

		object.addProperty("type", getAnnotationKey());
		object.add("value", values);

		return object;
	}

	public void setUrl(String url)
	{
		this.url = url;

		if (!TextUtils.isEmpty(getUrl()))
		{
			setPreviewUrl(getUrl());

			Uri uri = Uri.parse(getPreviewUrl());
			setPreviewUrl(URLUtils.fixInlineImage(uri));
		}
	}

	@Override public ImageAnnotation createFrom(JsonElement element)
	{
		try
		{
			JsonObject value = element.getAsJsonObject();

			this.url = value.get("url").getAsString();
			this.textUrl = value.get("url").getAsString();
			this.width = value.get("width").getAsInt();
			this.height = value.get("height").getAsInt();

			if (value.has("thumbnail_large_url_immediate"))
			{
				this.thumbUrl = value.get("thumbnail_large_url_immediate").getAsString();
				this.previewUrl = this.getThumbUrl();
			}
			else if (value.has("thumbnail_url"))
			{
				this.thumbUrl = value.get("thumbnail_url").getAsString();
				this.previewUrl = this.getThumbUrl();
			}

			if (value.has("embeddable_url"))
			{
				this.embeddableUrl = value.get("embeddable_url").getAsString();
			}

			return this;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override public ImageAnnotation createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "98164fa5-0b12-4c1c-a8f4-3a7e503b5c17";
	}

	@Override public ImageAnnotation read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				width = util.readInt();
				height = util.readInt();
				url = util.readString();
				textUrl = util.readString();
				thumbUrl = util.readString();
				thumbWidth = util.readInt();
				thumbHeight = util.readInt();
				embeddableUrl = util.readString();

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
			util.writeInt(width);
			util.writeInt(height);
			util.writeString(url);
			util.writeString(textUrl);
			util.writeString(thumbUrl);
			util.writeInt(thumbWidth);
			util.writeInt(thumbHeight);
			util.writeString(embeddableUrl);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		return null;
	}

	@Override public String getAnnotationKey()
	{
		return "net.app.core.oembed";
	}

	public static final Parcelable.Creator<ImageAnnotation> CREATOR = new Creator<ImageAnnotation>()
	{
		@Override public ImageAnnotation[] newArray(int size)
		{
			return new ImageAnnotation[size];
		}

		@Override public ImageAnnotation createFromParcel(Parcel source)
		{
			return new ImageAnnotation().createFrom(source);
		}
	};
}