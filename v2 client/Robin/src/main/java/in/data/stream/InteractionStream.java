package in.data.stream;

import android.os.Parcel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import in.data.Meta;
import in.data.stream.base.Stream;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.model.Interaction;
import in.model.base.Model;

public class InteractionStream extends Stream<Interaction>
{
	@Override public InteractionStream createFrom(JsonElement element)
	{
		if (super.createFrom(element) != null)
		{
			try
			{
				JsonObject streamObject = element.getAsJsonObject();
				JsonElement dataObject = streamObject.get("data");
				this.items = new Interaction().createListFrom(dataObject);

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public InteractionStream createFrom(Parcel parcel)
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
		return "e0b02477-be4e-40b3-ba6b-b67a8fe74428";
	}

	@Override public InteractionStream read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			meta = util.readModel(Meta.class);
			items = util.readModelList(Interaction.class);

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	public static final Creator<InteractionStream> CREATOR = new Creator<InteractionStream>()
	{
		@Override public InteractionStream[] newArray(int size)
		{
			return new InteractionStream[size];
		}

		@Override public InteractionStream createFromParcel(Parcel source)
		{
			return new InteractionStream().createFrom(source);
		}
	};
}
