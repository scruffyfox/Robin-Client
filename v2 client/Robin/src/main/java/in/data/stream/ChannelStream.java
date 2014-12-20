package in.data.stream;

import android.os.Parcel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import in.data.Meta;
import in.data.stream.base.Stream;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.model.Channel;
import in.model.base.Model;

public class ChannelStream extends Stream<Channel>
{
	@Override public ChannelStream createFrom(JsonElement element)
	{
		if (super.createFrom(element) != null)
		{
			try
			{
				JsonObject streamObject = element.getAsJsonObject();
				JsonElement dataObject = streamObject.get("data");
				this.items = new Channel().createListFrom(dataObject);

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public ChannelStream createFrom(Parcel parcel)
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
		return "58295b27-acd6-4b16-8694-e03cff484d9b";
	}

	@Override public ChannelStream read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			meta = util.readModel(Meta.class);
			items = util.readModelList(Channel.class);

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	public static final Creator<ChannelStream> CREATOR = new Creator<ChannelStream>()
	{
		@Override public ChannelStream[] newArray(int size)
		{
			return new ChannelStream[size];
		}

		@Override public ChannelStream createFromParcel(Parcel source)
		{
			return new ChannelStream().createFrom(source);
		}
	};
}
