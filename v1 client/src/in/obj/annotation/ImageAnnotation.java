package in.obj.annotation;

import in.lib.utils.URLUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@ToString(callSuper=true) public class ImageAnnotation extends Annotation
{
	private static final long serialVersionUID = 8987571728715810203L;

	@Getter @Setter protected int width = 0, height = 0;
	@Getter @Setter protected String url = "";
	@Getter @Setter protected String textUrl = "";
	@Getter @Setter protected String thumbUrl = "";
	@Getter @Setter protected int thumbWidth = 0, thumbHeight = 0;
	@Getter @Setter protected String embeddableUrl = "";

	private void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeInt(width);
		oos.writeInt(height);
		oos.writeUTF(url);
		oos.writeUTF(textUrl);
		oos.writeUTF(thumbUrl);
		oos.writeInt(thumbWidth);
		oos.writeInt(thumbHeight);
		oos.writeUTF(embeddableUrl);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		width = ois.readInt();
		height = ois.readInt();
		url = ois.readUTF();
		textUrl = ois.readUTF();
		thumbUrl = ois.readUTF();
		thumbWidth = ois.readInt();
		thumbHeight = ois.readInt();
		embeddableUrl = ois.readUTF();
	}

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

	@Override public String getAnnotationKey()
	{
		return "net.app.core.oembed";
	}
}