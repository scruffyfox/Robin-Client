package in.model;

import in.lib.Debug;
import in.lib.manager.UserManager;
import in.model.base.Message;
import in.obj.annotation.Annotation.Type;
import in.obj.annotation.CrosspostAnnotation;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import android.os.Parcel;
import android.os.Parcelable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Serializable class for posts
 */
@ToString(includeFieldNames = true, callSuper = true)
public class Post extends Message
{
	@Tag(0x01) @Getter @Setter private String originalId = "";
	@Tag(0x02) @Getter @Setter private String threadId = "";
	@Tag(0x03) @Getter @Setter private SimpleUser reposter;
	@Tag(0x04) @Getter @Setter private boolean starred;
	@Tag(0x05) @Getter @Setter private boolean repost;
	@Tag(0x06) @Getter @Setter private Boolean hasReplies = false;
	@Tag(0x07) @Getter @Setter private int replyCount;
	@Tag(0x08) @Getter @Setter private int repostCount;
	@Tag(0x09) @Getter @Setter private int starCount;
	@Tag(0x0A) @Getter @Setter private boolean crossPost;
	@Tag(0x0B) @Getter @Setter private String crossPostUrl = "";
	@Tag(0x0C) @Getter @Setter private SimpleUser[] reposters = {};
	@Tag(0x0D) @Getter @Setter private SimpleUser[] starrers = {};

	@Override public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null || (obj != null && getClass() != obj.getClass()))
		{
			return false;
		}

		Post other = (Post)obj;
		if (originalId == null)
		{
			if (other.originalId != null)
			{
				return false;
			}
		}
		else if (!originalId.equals(other.originalId))
		{
			return false;
		}

		return true;
	}

	@Override public Post createFrom(JsonObject post)
	{
		return createFrom(post, UserManager.getUser(), false);
	}

	@Override public Post createFrom(JsonObject post, boolean allowDeleted)
	{
		return createFrom(post, UserManager.getUser(), allowDeleted);
	}

	@Override public Post createFrom(JsonObject post, User acc, boolean allowDeleted)
	{
		if (post.has("repost_of"))
		{
			setReposter(new SimpleUser().createFrom(post.get("user").getAsJsonObject()));
			setOriginalId(post.get("id").getAsString());
			setRepost(true);
			post = post.get("repost_of").getAsJsonObject();
		}
		else
		{
			setOriginalId(post.get("id").getAsString());
		}

		if (super.createFrom(post, acc, allowDeleted) == null)
		{
			return null;
		}

		try
		{
			setThreadId(post.get("thread_id").getAsString());
			setReplyCount(post.get("num_replies").getAsInt());
			setHasReplies(replyCount > 0 || post.has("reply_to"));
			setStarCount(post.get("num_stars").getAsInt());
			setRepostCount(post.get("num_reposts").getAsInt());
			setCanonicalUrl(post.get("canonical_url").getAsString());

			if (post.has("you_starred"))
			{
				setStarred(post.get("you_starred").getAsBoolean());
			}

			if (getAnnotations() != null)
			{
				if (getAnnotations().containsKey(Type.CROSS_POST) && getAnnotations().get(Type.CROSS_POST).size() > 0)
				{
					CrosspostAnnotation crossPost = (CrosspostAnnotation)getAnnotations().get(Type.CROSS_POST).get(0);
					setCrossPost(true);
					setCrossPostUrl(crossPost.getUrl());
				}
			}

			if (post.has("reposters"))
			{
				JsonArray reposters = post.get("reposters").getAsJsonArray();

				SimpleUser[] postReposters = new SimpleUser[reposters.size()];
				int index = 0;
				for (JsonElement user : reposters)
				{
					SimpleUser reposter = new SimpleUser().createFrom(user.getAsJsonObject());
					postReposters[index++] = reposter;
				}

				setReposters(postReposters);
			}

			if (post.has("starred_by"))
			{
				JsonArray starrers = post.get("starred_by").getAsJsonArray();

				SimpleUser[] postStarrers = new SimpleUser[starrers.size()];
				int index = 0;
				for (JsonElement user : starrers)
				{
					SimpleUser starrer = new SimpleUser().createFrom(user.getAsJsonObject());
					postStarrers[index++] = starrer;
				}

				setStarrers(postStarrers);
			}

			return this;
		}
		catch(Exception e)
		{
			Debug.out(e);
			return null;
		}
	}

	@Override public Post createFrom(Parcel object)
	{
		setOriginalId(object.readString());
		setThreadId(object.readString());
		setReposter((SimpleUser)object.readParcelable(SimpleUser.class.getClassLoader()));
		setStarred((Boolean)object.readValue(null));
		setRepost((Boolean)object.readValue(null));
		setHasReplies((Boolean)object.readValue(null));
		setReplyCount(object.readInt());
		setRepostCount(object.readInt());
		setStarCount(object.readInt());
		setCrossPost((Boolean)object.readValue(null));
		setCrossPostUrl(object.readString());

		setReposters(new SimpleUser[object.readInt()]);
		setStarrers(new SimpleUser[object.readInt()]);
		object.readTypedArray(getReposters(), SimpleUser.CREATOR);
		object.readTypedArray(getStarrers(), SimpleUser.CREATOR);

		super.createFrom(object);
		return this;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getOriginalId());
		dest.writeString(getThreadId());
		dest.writeParcelable(getReposter(), 0);
		dest.writeValue(isStarred());
		dest.writeValue(isRepost());
		dest.writeValue(getHasReplies());
		dest.writeInt(getReplyCount());
		dest.writeInt(getRepostCount());
		dest.writeInt(getStarCount());
		dest.writeValue(isCrossPost());
		dest.writeString(getCrossPostUrl());
		dest.writeInt(getReposters().length);
		dest.writeInt(getStarrers().length);
		dest.writeTypedArray(getReposters(), 0);
		dest.writeTypedArray(getStarrers(), 0);
		super.writeToParcel(dest, flags);
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
}