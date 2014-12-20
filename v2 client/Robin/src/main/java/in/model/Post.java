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
public class Post extends Message
{
	protected String originalId;
	protected String threadId;
	protected String replyTo;
	protected User reposter;
	protected boolean starred;
	protected boolean repost;
	protected Boolean hasReplies = false;
	protected int replyCount;
	protected int repostCount;
	protected int starCount;
	protected List<User> reposters;
	protected List<User> starrers;

	@Override public Post createFrom(JsonElement element)
	{
		if (element != null)
		{
			try
			{
				JsonObject postObject = element.getAsJsonObject();
				setOriginalId(postObject.get("id").getAsString());

				if (postObject.has("repost_of"))
				{
					setRepost(true);
					setReposter(new User().createFrom(postObject.get("user").getAsJsonObject()));
					postObject = postObject.get("repost_of").getAsJsonObject();
				}

				if (super.createFrom(postObject) != null)
				{
					this.threadId = postObject.get("thread_id").getAsString();
					this.replyCount = postObject.get("num_replies").getAsInt();
					this.hasReplies = replyCount > 0 || postObject.has("reply_to");
					this.starCount = postObject.get("num_stars").getAsInt();
					this.repostCount = postObject.get("num_reposts").getAsInt();
					this.starred = postObject.has("you_starred") && postObject.get("you_starred").getAsBoolean();
					this.reposters = new User().createListFrom(postObject.get("reposters"));
					this.starrers = new User().createListFrom(postObject.get("starred_by"));

					if (postObject.has("reply_to"))
					{
						this.replyTo = postObject.get("reply_to").getAsString();
					}
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

	@Override public List<Post> createListFrom(JsonElement element)
	{
		try
		{
			JsonArray postArray = element.getAsJsonArray();
			ArrayList<Post> posts = new ArrayList<Post>(postArray.size());

			for (JsonElement postElement : postArray)
			{
				Post post = new Post().createFrom(postElement);

				if (post != null)
				{
					posts.add(post);
				}
			}

			return posts;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public Post createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "59abe07a-0ff2-4896-9991-e7a6bdf9f7f8";
	}

	@Override public Post read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				originalId = util.readString();
				threadId = util.readString();
				replyTo = util.readString();
				reposter = util.readModel(User.class);
				starred = util.readBoolean();
				repost = util.readBoolean();
				hasReplies = util.readBoolean();
				replyCount = util.readInt();
				repostCount = util.readInt();
				starCount = util.readInt();
				reposters = util.readModelList(User.class);
				starrers = util.readModelList(User.class);

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
			util.writeString(originalId);
			util.writeString(threadId);
			util.writeString(replyTo);
			util.writeModel(reposter);
			util.writeBoolean(starred);
			util.writeBoolean(repost);
			util.writeBoolean(hasReplies);
			util.writeInt(replyCount);
			util.writeInt(repostCount);
			util.writeInt(starCount);
			util.writeModelList(reposters);
			util.writeModelList(starrers);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public static final Parcelable.Creator<Post> CREATOR = new Creator<Post>()
	{
		@Override public Post[] newArray(int size)
		{
			return new Post[size];
		}

		@Override public Post createFromParcel(Parcel source)
		{
			return new Post().createFrom(source);
		}
	};

	@Override public boolean equals(Object object)
	{
		if (object == null)
		{
			return false;
		}

		if ((object == this)
		|| (object instanceof Post
		&& ((((Post)object).getId().equals(getId()) || ((Post)object).getOriginalId().equals(getOriginalId())))))
		{
			return true;
		}

		return false;
	}
}
