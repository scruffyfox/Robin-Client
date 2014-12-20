package in.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import in.data.Text;
import in.lib.manager.CacheManager;
import in.lib.utils.BitmapUtils;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import lombok.Data;

@Data
public class User extends SimpleUser
{
	protected String coverUrl;
	protected boolean following;
	protected boolean follower;
	protected boolean muted;
	protected boolean blocked;
	protected int followingCount;
	protected int followerCount;
	protected int postCount;
	protected int starredCount;
	protected Text description;
	protected boolean coverDefault = true;
	protected boolean avatarDefault = true;
	protected String verifiedDomain;
	protected String token;

	@Override public User createFrom(JsonElement element)
	{
		try
		{
			JsonObject userObject = element.getAsJsonObject();

			if (super.createFrom(userObject) != null)
			{
				this.coverUrl = userObject.get("cover_image").getAsJsonObject().get("url").getAsString();
				this.following = userObject.has("you_follow") ? userObject.get("you_follow").getAsBoolean() : false;
				this.follower = userObject.has("follows_you") ? userObject.get("follows_you").getAsBoolean() : false;
				this.muted = userObject.has("you_muted") ? userObject.get("you_muted").getAsBoolean() : false;
				this.blocked = userObject.has("you_blocked") ? userObject.get("you_blocked").getAsBoolean() : false;
				this.followingCount = userObject.get("counts").getAsJsonObject().get("following").getAsInt();
				this.followerCount = userObject.get("counts").getAsJsonObject().get("followers").getAsInt();
				this.postCount = userObject.get("counts").getAsJsonObject().get("posts").getAsInt();
				this.starredCount = userObject.get("counts").getAsJsonObject().get("stars").getAsInt();
				this.verifiedDomain = userObject.has("verified_domain") ? userObject.get("verified_domain").getAsString() : "";
				this.avatarDefault = userObject.get("avatar_image").getAsJsonObject().get("is_default").getAsBoolean();
				this.coverDefault = userObject.get("cover_image").getAsJsonObject().get("is_default").getAsBoolean();

				if (userObject.get("description") != null)
				{
					this.description = new Text().createFrom(userObject.get("description"));
				}

				return this;
			}
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public User createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public List<User> createListFrom(JsonElement element)
	{
		if (element != null)
		{
			try
			{
				JsonArray userArray = element.getAsJsonArray();
				ArrayList<User> users = new ArrayList<User>(userArray.size());

				for (JsonElement userElement : userArray)
				{
					User user = new User().createFrom(userElement);

					if (user != null)
					{
						users.add(user);
					}
				}

				return users;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public String getVersion()
	{
		return "165c50c0-ac40-11e3-a5e2-0800200c9a66";
	}

	@Override public User read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();

				this.coverUrl = util.readString();
				this.following = util.readBoolean();
				this.follower = util.readBoolean();
				this.muted = util.readBoolean();
				this.blocked = util.readBoolean();
				this.followingCount = util.readInt();
				this.followerCount = util.readInt();
				this.postCount = util.readInt();
				this.starredCount = util.readInt();
				this.description = util.readModel(Text.class);
				this.coverDefault = util.readBoolean();
				this.avatarDefault = util.readBoolean();
				this.verifiedDomain = util.readString();

				if (version.equals(getVersion()))
				{
					this.token = util.readString();
				}

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public void write(SerialWriterUtil util)
	{
		super.write(util);

		try
		{
			util.writeString(getVersion());
			util.writeString(coverUrl);
			util.writeBoolean(following);
			util.writeBoolean(follower);
			util.writeBoolean(muted);
			util.writeBoolean(blocked);
			util.writeInt(followingCount);
			util.writeInt(followerCount);
			util.writeInt(postCount);
			util.writeInt(starredCount);
			util.writeModel(description);
			util.writeBoolean(coverDefault);
			util.writeBoolean(avatarDefault);
			util.writeString(verifiedDomain);
			util.writeString(token);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public void save()
	{
		CacheManager.getInstance().writeFile("user_" + getId(), this);
	}

	public static User load(String userId)
	{
		return CacheManager.getInstance().readFile("user_" + userId, User.class);
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

	@Override public boolean equals(Object object)
	{
		if (object == null)
		{
			return false;
		}

		if ((object == this) || (object instanceof User && ((User)object).getId().equals(getId())))
		{
			return true;
		}

		return false;
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
