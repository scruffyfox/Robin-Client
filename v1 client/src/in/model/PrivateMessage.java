package in.model;

import in.lib.Debug;
import in.lib.manager.UserManager;
import in.model.base.Message;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import android.os.Parcel;
import android.os.Parcelable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.google.gson.JsonObject;

/**
 * Serializable class for messages
 */
@ToString(includeFieldNames = true, callSuper = true)
public class PrivateMessage extends Message
{
	@Tag(0x01) @NonNull @Getter @Setter private String channelId = "";

	@Override public PrivateMessage createFrom(JsonObject message)
	{
		return createFrom(message, UserManager.getUser(), false);
	}

	@Override public PrivateMessage createFrom(JsonObject post, boolean allowDeleted)
	{
		return createFrom(post, UserManager.getUser(), allowDeleted);
	}

	/**
	 * Parses the return API object into a message class using the specified user
	 * @param user The jsonobject message from the API
	 * @param acc The user to use when parsing the object
	 * @return The new Post object
	 */
	@Override public PrivateMessage createFrom(JsonObject message, User acc, boolean allowDeleted)
	{
		super.createFrom(message, acc, allowDeleted);

		try
		{
			channelId = message.get("channel_id").getAsString();
			return this;
		}
		catch(Exception e)
		{
			Debug.out(e);
			return null;
		}
	}

	@Override public PrivateMessage createFrom(Parcel object)
	{
		setChannelId(object.readString());
		super.createFrom(object);
		return this;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getChannelId());
		super.writeToParcel(dest, flags);
	}

	public static final Parcelable.Creator<PrivateMessage> CREATOR = new Creator<PrivateMessage>()
	{
		@Override public PrivateMessage[] newArray(int size)
		{
			return new PrivateMessage[size];
		}

		@Override public PrivateMessage createFromParcel(Parcel source)
		{
			return new PrivateMessage().createFrom(source);
		}
	};
}