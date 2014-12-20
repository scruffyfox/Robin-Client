package in.model;

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
public class ChannelMessage extends Message
{
	protected  String channelId = "-1";

	@Override public ChannelMessage createFrom(JsonElement element)
	{
		super.createFrom(element);

		try
		{
			JsonObject messageObject = element.getAsJsonObject();

			this.channelId = messageObject.get("channel_id").getAsString();

			return this;
		}
		catch(Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public List<ChannelMessage> createListFrom(JsonElement element)
	{
		try
		{
			JsonArray messageArray = element.getAsJsonArray();
			ArrayList<ChannelMessage> messages = new ArrayList<ChannelMessage>(messageArray.size());

			for (JsonElement messageElement : messageArray)
			{
				ChannelMessage message = new ChannelMessage().createFrom(messageElement);

				if (message != null)
				{
					messages.add(message);
				}
			}

			return messages;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public ChannelMessage createFrom(Parcel object)
	{
		super.createFrom(object);
		return this;
	}

	@Override public String getVersion()
	{
		return "99cd5bfc-2fcd-44e4-be69-d753f495d5e4";
	}

	@Override public void write(SerialWriterUtil util)
	{
		super.write(util);

		try
		{
			util.writeString(getVersion());
			util.writeString(channelId);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public Message read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				channelId = util.readString();
				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	public static final Parcelable.Creator<ChannelMessage> CREATOR = new Creator<ChannelMessage>()
	{
		@Override public ChannelMessage[] newArray(int size)
		{
			return new ChannelMessage[size];
		}

		@Override public ChannelMessage createFromParcel(Parcel source)
		{
			return new ChannelMessage().createFrom(source);
		}
	};
}