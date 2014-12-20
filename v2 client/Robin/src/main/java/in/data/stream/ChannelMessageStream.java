package in.data.stream;

import android.os.Parcel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import in.data.Meta;
import in.data.stream.base.Stream;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.model.ChannelMessage;
import in.model.base.Model;

public class ChannelMessageStream extends Stream<ChannelMessage>
{
	@Override public ChannelMessageStream createFrom(JsonElement element)
	{
		if (super.createFrom(element) != null)
		{
			try
			{
				JsonObject streamObject = element.getAsJsonObject();
				JsonElement dataObject = streamObject.get("data");
				this.items = new ChannelMessage().createListFrom(dataObject);

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		return null;
	}

	@Override public ChannelMessageStream createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "52b2f83e-48b8-4702-9991-7207519f96a4";
	}

	@Override public ChannelMessageStream read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			meta = util.readModel(Meta.class);
			items = util.readModelList(ChannelMessage.class);

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	public static final Creator<ChannelMessageStream> CREATOR = new Creator<ChannelMessageStream>()
	{
		@Override public ChannelMessageStream[] newArray(int size)
		{
			return new ChannelMessageStream[size];
		}

		@Override public ChannelMessageStream createFromParcel(Parcel source)
		{
			return new ChannelMessageStream().createFrom(source);
		}
	};
}
