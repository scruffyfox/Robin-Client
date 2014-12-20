package in.data.stream;

import android.os.Parcel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import in.data.Meta;
import in.data.annotation.FileAnnotation;
import in.data.stream.base.Stream;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.model.base.Model;

public class FileStream extends Stream<FileAnnotation>
{
	@Override public FileStream createFrom(JsonElement element)
	{
		if (super.createFrom(element) != null)
		{
			try
			{
				JsonObject streamObject = element.getAsJsonObject();
				JsonElement dataObject = streamObject.get("data");
				this.items = new FileAnnotation().createListFrom(dataObject);

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public FileStream createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		return null;
	}

	@Override public String getVersion()
	{
		return "64bc421c-f580-47de-a06c-6b7f426cb04c";
	}

	@Override public FileStream read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			meta = util.readModel(Meta.class);
			items = util.readModelList(FileAnnotation.class);

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	public static final Creator<FileStream> CREATOR = new Creator<FileStream>()
	{
		@Override public FileStream[] newArray(int size)
		{
			return new FileStream[size];
		}

		@Override public FileStream createFromParcel(Parcel source)
		{
			return new FileStream().createFrom(source);
		}
	};
}
