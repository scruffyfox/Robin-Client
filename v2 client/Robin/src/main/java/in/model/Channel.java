package in.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import lombok.Data;
import lombok.Getter;

@Data
public class Channel extends AdnModel
{
	public enum Type
	{
		PRIVATE_MESSAGE("net.app.core.pm"),
		PATTER_CHANNEL("net.patter-app.room");

		@Getter private final String typeId;
		private Type(String typeId)
		{
			this.typeId = typeId;
		}

		public static Type getTypeById(String id)
		{
			Type[] values = Type.values();
			for (Type t : values)
			{
				if (t.getTypeId().equals(id))
				{
					return t;
				}
			}

			return null;
		}
	}

	protected String title;
	protected User owner;
	protected Type type = Type.PRIVATE_MESSAGE;
	protected ChannelMessage recentMessage;
	protected boolean unread;
	protected boolean editable;
	protected boolean subscribed;
	protected boolean writable;
	protected boolean isPublic;
	protected List<String> readers = new ArrayList<String>();
	protected List<User> users = new ArrayList<User>();

	@Override public Channel createFrom(JsonElement element)
	{
		try
		{
			JsonObject channel = element.getAsJsonObject();

			this.id = channel.get("id").getAsString();
			this.unread = channel.get("has_unread").getAsBoolean();

			if (channel.has("owner"))
			{
				this.owner = new User().createFrom(channel.get("owner").getAsJsonObject());
			}
			else
			{
				this.owner = new User();
			}

			this.type = Type.getTypeById(channel.get("type").getAsString());
			this.editable = channel.get("you_can_edit").getAsBoolean();
			this.subscribed = channel.get("you_subscribed").getAsBoolean();
			this.writable = channel.get("writers").getAsJsonObject().get("you").getAsBoolean();

			JsonArray readerIds = channel.get("writers").getAsJsonObject().get("user_ids").getAsJsonArray();

			if (this.type == Type.PATTER_CHANNEL)
			{
				if (channel.get("readers").getAsJsonObject().has("public")
				&& channel.get("writers").getAsJsonObject().has("any_user"))
				{
					boolean readerPublic = channel.get("readers").getAsJsonObject().get("public").getAsBoolean();
					boolean writerPublic = channel.get("writers").getAsJsonObject().get("any_user").getAsBoolean();
					this.isPublic = readerPublic && writerPublic;
				}
			}

			if (channel.has("recent_message"))
			{
				this.recentMessage = new ChannelMessage().createFrom(channel.get("recent_message"));

				if (this.recentMessage != null && this.recentMessage.getPoster() != null)
				{
					this.readers.add(this.recentMessage.getPoster().getId());
				}
			}

			if (!this.readers.contains(this.owner.getId()) &&!TextUtils.isEmpty(this.owner.getId()))
			{
				this.readers.add(this.owner.getId());
			}

			for (JsonElement reader : readerIds)
			{
				if (!this.readers.contains(reader.getAsString()))
				{
					this.readers.add(reader.getAsString());
				}
			}

			if (channel.has("annotations"))
			{
				JsonArray annotations = channel.get("annotations").getAsJsonArray();
				for (JsonElement annotation : annotations)
				{
					if (annotation.getAsJsonObject().get("type").getAsString().equals("net.patter-app.settings"))
					{
						if (annotation.getAsJsonObject().get("value").getAsJsonObject().has("name"))
						{
							this.title = annotation.getAsJsonObject().get("value").getAsJsonObject().get("name").getAsString();
							break;
						}
					}
				}
			}

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
		
		return null;
	}

	@Override public List<Channel> createListFrom(JsonElement element)
	{
		try
		{
			JsonArray channelArray = element.getAsJsonArray();
			ArrayList<Channel> channels = new ArrayList<Channel>(channelArray.size());

			for (JsonElement channelElement : channelArray)
			{
				Channel channel = new Channel().createFrom(channelElement);

				if (channel != null)
				{
					channels.add(channel);
				}
			}

			return channels;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public String getVersion()
	{
		return "691299ea-8ca6-40ba-b831-a36a72877bb1";
	}

	@Override public void write(SerialWriterUtil util)
	{
		super.write(util);

		try
		{
			util.writeString(getVersion());
			util.writeString(title);
			util.writeModel(owner);
			util.writeInt(type.ordinal());
			util.writeModel(recentMessage);
			util.writeBoolean(unread);
			util.writeBoolean(editable);
			util.writeBoolean(subscribed);
			util.writeBoolean(writable);
			util.writeBoolean(isPublic);
			util.writeStringList(readers);
			util.writeModelList(users);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public Channel read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				title = util.readString();
				owner = util.readModel(User.class);
				type = Type.values()[util.readInt()];
				recentMessage = util.readModel(ChannelMessage.class);
				unread = util.readBoolean();
				editable = util.readBoolean();
				subscribed = util.readBoolean();
				writable = util.readBoolean();
				isPublic = util.readBoolean();
				readers = util.readStringList();
				users = util.readModelList(User.class);

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public Channel createFrom(Parcel object)
	{
		super.createFrom(object);
		return this;
	}

	public static final Parcelable.Creator<Channel> CREATOR = new Creator<Channel>()
	{
		@Override public Channel[] newArray(int size)
		{
			return new Channel[size];
		}

		@Override public Channel createFromParcel(Parcel source)
		{
			return new Channel().createFrom(source);
		}
	};
}