package in.data.annotation;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import lombok.Data;

@Data
public class FileAnnotation extends Annotation
{
	protected String fileToken;
	protected String thumbUrl = "";
	protected int thumbWidth = 0, thumbHeight = 0;

	@Override public FileAnnotation createFrom(JsonElement element)
	{
		try
		{
			JsonObject value = element.getAsJsonObject();

			if (value.has("data"))
			{
				value = value.get("data").getAsJsonObject();
			}

			if (value.has("kind") && !value.get("kind").getAsString().equals("image"))
			{
				return null;
			}

			this.id = value.get("id").getAsString();
			this.fileToken = value.get("file_token").getAsString();

			if (value.has("derived_files"))
			{
				JsonObject files = value.get("derived_files").getAsJsonObject();
				JsonObject thumb = null;

				if (files.has("image_thumb_960r"))
				{
					thumb = files.get("image_thumb_960r").getAsJsonObject();
				}
				else if (files.has("image_thumb_200s"))
				{
					thumb = files.get("image_thumb_200s").getAsJsonObject();
				}

				if (thumb != null)
				{
					JsonObject thumbInfo = thumb.get("image_info").getAsJsonObject();
					this.thumbUrl = thumb.get("url").getAsString();
					this.thumbWidth = thumbInfo.get("width").getAsInt();
					this.thumbHeight = thumbInfo.get("height").getAsInt();
				}
			}

			return this;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override public FileAnnotation createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public List<FileAnnotation> createListFrom(JsonElement element)
	{
		try
		{
			JsonArray fileArray = element.getAsJsonArray();
			ArrayList<FileAnnotation> files = new ArrayList<FileAnnotation>(fileArray.size());

			for (JsonElement fileElement : fileArray)
			{
				FileAnnotation file = new FileAnnotation().createFrom(fileElement);

				if (file != null)
				{
					files.add(file);
				}
			}

			return files;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public String getVersion()
	{
		return "53297329-2081-4804-9c76-d2949b5039f5";
	}

	@Override public FileAnnotation read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				id = util.readString();
				fileToken = util.readString();
				thumbUrl = util.readString();
				thumbWidth = util.readInt();
				thumbHeight = util.readInt();

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
			util.writeString(id);
			util.writeString(fileToken);
			util.writeString(thumbUrl);
			util.writeInt(thumbWidth);
			util.writeInt(thumbHeight);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public JsonElement toAnnotation()
	{
		JsonObject object = new JsonObject();
		JsonObject file = new JsonObject();
		JsonObject value = new JsonObject();
		file.addProperty("file_id", getId());
		file.addProperty("file_token", getFileToken());
		file.addProperty("format", "oembed");
		object.addProperty("type", "net.app.core.oembed");
		value.add("+net.app.core.file", file);
		object.add("value", value);

		return object;
	}

	@Override public String getAnnotationKey()
	{
		return "";
	}

	public static final Parcelable.Creator<FileAnnotation> CREATOR = new Creator<FileAnnotation>()
	{
		@Override public FileAnnotation[] newArray(int size)
		{
			return new FileAnnotation[size];
		}

		@Override public FileAnnotation createFromParcel(Parcel source)
		{
			return new FileAnnotation().createFrom(source);
		}
	};
}