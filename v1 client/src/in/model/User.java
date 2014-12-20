package in.model;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.utils.BitmapUtils;
import in.lib.utils.CodeUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.google.gson.JsonObject;

/**
 * Serializable class for users
 * @author CallumTaylor
 */
@ToString(includeFieldNames = true, callSuper = true)
public class User extends SimpleUser
{
	@Tag(0x01) @Getter @Setter private String formattedDescription;
	@Tag(0x02) @Getter @Setter private String locale;
	@Tag(0x03) @Getter @Setter private String timeZone;
	@Tag(0x04) @Getter @Setter private boolean followingYou;
	@Tag(0x05) @Getter @Setter private boolean muted;
	@Tag(0x06) @Getter @Setter private int followersCount;
	@Tag(0x07) @Getter @Setter private int followingCount;
	@Tag(0x08) @Getter @Setter private int postCount;
	@Tag(0x09) @Getter @Setter private int starredCount;
	@Tag(0x0A) @Getter @Setter private boolean isCoverDefault = true;
	@Tag(0x0B) @Getter @Setter private boolean isAvatarDefault = true;
	@Tag(0x0C) @Getter @Setter private boolean blocked;
	@Tag(0x0D) @Getter @Setter private String verifiedDomain;
	@Tag(0x0E) @Getter @Setter private String[] formattedMentionName = {"", ""};

	/**
	 * Saves the user into cache
	 */
	public void save()
	{
		if (TextUtils.isEmpty(getId())) return;
		CacheManager.getInstance().asyncWriteFile(String.format(Constants.CACHE_USER, getId()), this);
	}

	public static Bitmap loadAvatar(Context c, String userId)
	{
		int width = c.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
		int height = c.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

		return loadAvatar(c, userId, width, height);
	}

	public static Bitmap loadAvatar(Context c, final String userId, int width, int height)
	{
		final File f = new File(c.getExternalCacheDir() + "/uil-images/");
		String[] files = f.list(new FilenameFilter()
		{
			@Override public boolean accept(File dir, String filename)
			{
				return filename != null && filename.startsWith("avatar_" + userId + "_");
			}
		});

		if (files != null)
		{
			Arrays.sort(files, new Comparator<String>()
			{
				@Override public int compare(String f1, String f2)
				{
					if (new File(f, f1).lastModified() > new File(f, f2).lastModified())
					{
						return -1;
					}
					else if (new File(f, f1).lastModified() < new File(f, f2).lastModified())
					{
						return +1;
					}
					else
					{
						return 0;
					}
				}
			});

			String filename = files.length > 0 ? files[0] : "";

			if (!TextUtils.isEmpty(filename))
			{
				try
				{
					filename = c.getExternalCacheDir() + "/uil-images/" + filename;

					Options o = new Options();
					o.inSampleSize = BitmapUtils.recursiveSample(filename, width, height);
					Bitmap b = BitmapFactory.decodeFile(filename, o);

					if (b != null)
					{
						return BitmapUtils.resize(b, width, height);
					}
				}
				catch (OutOfMemoryError e){}
			}
		}

		return null;
	}

	/**
	 * Parses the return API object into a user class using the specified user
	 * @param user The jsonobject user from the API
	 * @param acc The user to use when parsing
	 * @return The new User object
	 */
	@Override public User createFrom(JsonObject user)
	{
		try
		{
			if (super.createFrom(user) == null)
			{
				return null;
			}

			setAvatarDefault(user.get("avatar_image").getAsJsonObject().get("is_default").getAsBoolean());
			setCoverDefault(user.get("cover_image").getAsJsonObject().get("is_default").getAsBoolean());
			setFollowingCount(user.get("counts").getAsJsonObject().get("following").getAsInt());
			setFollowersCount(user.get("counts").getAsJsonObject().get("followers").getAsInt());
			setPostCount(user.get("counts").getAsJsonObject().get("posts").getAsInt());
			setStarredCount(user.get("counts").getAsJsonObject().get("stars").getAsInt());
			setFormattedMentionName(CodeUtils.nameOrderParse(SettingsManager.getNameDisplayOrder(), this));

			if (user.has("description") && user.get("description").getAsJsonObject().has("html"))
			{
				String deschtml = user.get("description").getAsJsonObject().get("html").getAsString().trim();
				deschtml = deschtml.replaceAll("\\n", "<br/>").replaceAll("(?:\\<br\\s*/?\\>)*$", "");
				setFormattedDescription(deschtml);
			}

			setLocale(user.get("locale").getAsString());
			setTimeZone(user.get("timezone").getAsString());

			if (user.has("verified_domain"))
			{
				setVerifiedDomain(user.get("verified_domain").getAsString());
			}

			if (!isYou())
			{
				setFollowingYou(user.has("follows_you") ? user.get("follows_you").getAsBoolean() : false);
				setMuted(user.has("you_muted") ? user.get("you_muted").getAsBoolean() : false);
			}

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
			return null;
		}
	}

	@Override public User createFrom(Parcel object)
	{
		setFormattedDescription(object.readString());
		setLocale(object.readString());
		setTimeZone(object.readString());
		setFollowingYou((Boolean)object.readValue(null));
		setMuted((Boolean)object.readValue(null));
		setFollowersCount(object.readInt());
		setFollowingCount(object.readInt());
		setPostCount(object.readInt());
		setStarredCount(object.readInt());
		setCoverDefault((Boolean)object.readValue(null));
		setAvatarDefault((Boolean)object.readValue(null));
		setBlocked((Boolean)object.readValue(null));
		setVerifiedDomain(object.readString());

		String[] name = new String[2];
		object.readStringArray(name);
		setFormattedMentionName(name);

		super.createFrom(object);
		return this;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getFormattedDescription());
		dest.writeString(getLocale());
		dest.writeString(getTimeZone());
		dest.writeValue(isFollowingYou());
		dest.writeValue(isMuted());
		dest.writeInt(getFollowersCount());
		dest.writeInt(getFollowingCount());
		dest.writeInt(getPostCount());
		dest.writeInt(getStarredCount());
		dest.writeValue(isCoverDefault());
		dest.writeValue(isAvatarDefault());
		dest.writeValue(isBlocked());
		dest.writeString(getVerifiedDomain());
		dest.writeStringArray(getFormattedMentionName());
		super.writeToParcel(dest, flags);
	}

	public SimpleUser toSimpleUser()
	{
		SimpleUser u = new SimpleUser();
		u.setAvatarUrl(getAvatarUrl());
		u.setCoverUrl(getCoverUrl());
		u.setFilterTag(getFilterTag());
		u.setFirstName(getFirstName());
		u.setId(getId());
		u.setLastName(getLastName());
		u.setMentionName(getMentionName());
		u.setYouFollow(getYouFollow());
		return u;
	}

	/**
	 * Loads the user from cache
	 * @param c The context to load the data from cache
	 * @param userId The user's ID to load
	 * @return The user or null
	 */
	public static User loadUser(String userId)
	{
		User u = CacheManager.getInstance().readFileAsObject(String.format(Constants.CACHE_USER, userId), User.class);
		return u;
	}

	/**
	 * Checks if a user is saved in cache
	 * @param c The context to check the data from cache
	 * @param userId The user's ID to check
	 * @return True/false
	 */
	public static boolean userSaved(String userId)
	{
		return CacheManager.getInstance().fileExists(String.format(Constants.CACHE_USER, userId));
	}

	public static User deserialize(byte[] data)
	{
		return CacheManager.getInstance().deserialize(data, User.class);
	}

	public static final Parcelable.Creator<User> CREATOR = new Creator<User>()
	{
		@Override public User[] newArray(int size)
		{
			return new User[size];
		}

		@Override public User createFromParcel(Parcel source)
		{
			return new User().createFrom(source);
		}
	};
}