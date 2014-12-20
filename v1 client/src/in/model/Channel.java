package in.model;


import in.lib.Debug;
import in.model.base.NetObject;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@ToString(includeFieldNames = true, callSuper = true)
public class Channel extends NetObject
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
			Type[] vals = Type.values();
			for (Type t : vals)
			{
				if (t.getTypeId().equals(id))
				{
					return t;
				}
			}

			return null;
		}
	}

	@Tag(0x01) @Getter @Setter private String title;
	@Tag(0x02) @Getter @Setter private boolean isUnread;
	@Tag(0x03) @Getter @Setter private SimpleUser owner;
	@Tag(0x04) @Getter @Setter private Type type = Type.PRIVATE_MESSAGE;
	@Tag(0x05) @Getter @Setter private boolean editable = true;
	@Tag(0x06) @Getter @Setter private boolean subscribed = true;
	@Tag(0x07) @Getter @Setter private boolean canWrite = true;
	@Tag(0x08) @Getter @Setter private PrivateMessage recentMessage;
	@Tag(0x09) @Getter @Setter private List<String> readers = new ArrayList<String>();
	@Tag(0x0A) @Getter @Setter private boolean isPublic;
	@Tag(0x0B) @Getter @Setter private List<SimpleUser> users = new ArrayList<SimpleUser>();

	/**
	 * Parses the return API object into a post class using the default logged in user
	 * @param user The jsonobject post from the API
	 * @return The new Post object
	 */
	@Override public Channel createFrom(JsonObject channel)
	{
		try
		{
			setId(channel.get("id").getAsString());
			isUnread = channel.get("has_unread").getAsBoolean();

			if (channel.has("owner"))
			{
				owner = new User().createFrom(channel.get("owner").getAsJsonObject());
			}
			else
			{
				owner = new User();
			}

			type = Type.getTypeById(channel.get("type").getAsString());
			editable = channel.get("you_can_edit").getAsBoolean();
			subscribed = channel.get("you_subscribed").getAsBoolean();
			canWrite = channel.get("writers").getAsJsonObject().get("you").getAsBoolean();

			JsonArray readerIds = channel.get("writers").getAsJsonObject().get("user_ids").getAsJsonArray();

			if (type == Type.PATTER_CHANNEL)
			{
				if (channel.get("readers").getAsJsonObject().has("public")
				&& channel.get("writers").getAsJsonObject().has("any_user"))
				{
					boolean readerPublic = channel.get("readers").getAsJsonObject().get("public").getAsBoolean();
					boolean writerPublic = channel.get("writers").getAsJsonObject().get("any_user").getAsBoolean();
					isPublic = readerPublic && writerPublic;
				}
			}

			if (channel.has("recent_message"))
			{
				recentMessage = new PrivateMessage().createFrom(channel.get("recent_message").getAsJsonObject());

				if (recentMessage != null && recentMessage.getPoster() != null)
				{
					readers.add(recentMessage.getPoster().getId());
				}
			}

			if (!readers.contains(owner.getId()) &&!TextUtils.isEmpty(owner.getId()))
			{
				readers.add(owner.getId());
			}

			for (JsonElement reader : readerIds)
			{
				if (!readers.contains(reader.getAsString()))
				{
					readers.add(reader.getAsString());
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
							title = annotation.getAsJsonObject().get("value").getAsJsonObject().get("name").getAsString();
						}
					}
				}
			}

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
			return null;
		}
	}

	@Override public Channel createFrom(Parcel object)
	{
		setTitle(object.readString());
		setUnread((Boolean)object.readValue(null));
		setOwner((SimpleUser)object.readParcelable(SimpleUser.class.getClassLoader()));
		setType((Type)object.readValue(null));
		setEditable((Boolean)object.readValue(null));
		setSubscribed((Boolean)object.readValue(null));
		setCanWrite((Boolean)object.readValue(null));
		setRecentMessage((PrivateMessage)object.readParcelable(PrivateMessage.class.getClassLoader()));
		setReaders(object.readArrayList(String.class.getClassLoader()));
		setPublic((Boolean)object.readValue(null));
		setUsers(object.readArrayList(SimpleUser.class.getClassLoader()));

		super.createFrom(object);
		return this;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getTitle());
		dest.writeValue(isUnread());
		dest.writeParcelable(getOwner(), 0);
		dest.writeValue(getType());
		dest.writeValue(isEditable());
		dest.writeValue(isSubscribed());
		dest.writeValue(isCanWrite());
		dest.writeParcelable(getRecentMessage(), 0);
		dest.writeList(getReaders());
		dest.writeValue(isPublic());
		dest.writeList(getUsers());
		super.writeToParcel(dest, flags);
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