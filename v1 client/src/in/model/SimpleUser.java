package in.model;

import in.lib.Debug;
import in.lib.manager.CacheManager;
import in.lib.manager.UserManager;
import in.model.base.NetObject;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import android.os.Parcel;
import android.os.Parcelable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.google.gson.JsonObject;

/**
 * Serializable class for users
 * @author CallumTaylor
 */
@ToString(includeFieldNames = true, callSuper = true)
public class SimpleUser extends NetObject
{
	@Tag(0x01) @Getter @Setter private String avatarUrl = "";
	@Tag(0x02) @Getter @Setter private String coverUrl = "";
	@Tag(0x03) @Getter @Setter private String mentionName = "";
	@Tag(0x04) @Getter @Setter private String firstName = "";
	@Tag(0x05) @Getter @Setter private String lastName = "";
	@Tag(0x06) @Getter @Setter private Boolean youFollow = Boolean.FALSE;

	public String getUserName()
	{
		return new StringBuilder().append(firstName).append(" ").append(lastName).toString().trim();
	}

	/**
	 * Parses the return API object into a user class using the specified user
	 * @param user The jsonobject user from the API
	 * @param acc The user to use when parsing
	 * @return The new User object
	 */
	@Override public SimpleUser createFrom(JsonObject user)
	{
		try
		{
			setId(user.get("id").getAsString());
			setAvatarUrl(user.get("avatar_image").getAsJsonObject().get("url").getAsString());
			setCoverUrl(user.get("cover_image").getAsJsonObject().get("url").getAsString());

			if (user.has("name"))
			{
				String userName = user.get("name").getAsString();
				String[] parts = userName.split("\\s");
				setFirstName(parts.length > 0 ? parts[0] : getUserName());

				if (parts.length > 1)
				{
					for (int index = 1; index < parts.length; index++)
					{
						lastName += parts[index] + " ";
					}

					setLastName(lastName.substring(0, lastName.length() - 1));
				}
			}
			else
			{
				setFirstName("");
				setLastName("");
			}

			setMentionName(user.get("username").getAsString());

			if (!isYou())
			{
				setYouFollow(user.has("you_follow") ? user.get("you_follow").getAsBoolean() : false);
			}

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
			return null;
		}
	}

	/**
	 * Gets if the user is you or not
	 * @return True if it is, false if not
	 */
	public boolean isYou()
	{
		return getId().equals(UserManager.getUserId());
	}

	public static boolean containsUser(List<SimpleUser> list, SimpleUser user)
	{
		if (user != null)
		{
			for (SimpleUser u : list)
			{
				if (u != null && u.getId().equals(user.getId()))
				{
					return true;
				}
			}
		}

		return false;
	}

	public static SimpleUser deserialize(byte[] data)
	{
		return CacheManager.getInstance().deserialize(data, SimpleUser.class);
	}

	public static SimpleUser parseFromUser(User user)
	{
		if (user != null)
		{
			SimpleUser u = new SimpleUser();
			u.setAvatarUrl(user.getAvatarUrl());
			u.setCoverUrl(user.getCoverUrl());
			u.setFilterTag(user.getFilterTag());
			u.setFirstName(user.getFirstName());
			u.setId(user.getId());
			u.setLastName(user.getLastName());
			u.setMentionName(user.getMentionName());
			u.setYouFollow(user.getYouFollow());
			return u;
		}

		return null;
	}

	@Override public SimpleUser createFrom(Parcel object)
	{
		setAvatarUrl(object.readString());
		setCoverUrl(object.readString());
		setMentionName(object.readString());
		setFirstName(object.readString());
		setLastName(object.readString());
		setYouFollow((Boolean)object.readValue(null));
		super.createFrom(object);
		return this;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getAvatarUrl());
		dest.writeString(getCoverUrl());
		dest.writeString(getMentionName());
		dest.writeString(getFirstName());
		dest.writeString(getLastName());
		dest.writeValue(getYouFollow());
		super.writeToParcel(dest, flags);
	}

	public static final Parcelable.Creator<SimpleUser> CREATOR = new Creator<SimpleUser>()
	{
		@Override public SimpleUser[] newArray(int size)
		{
			return new SimpleUser[size];
		}

		@Override public SimpleUser createFromParcel(Parcel source)
		{
			return new SimpleUser().createFrom(source);
		}
	};
}