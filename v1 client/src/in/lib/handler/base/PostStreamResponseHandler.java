package in.lib.handler.base;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.writer.MultiFileCacheWriter;
import in.model.Post;
import in.model.base.NetObject;
import in.obj.StreamMarker;
import in.rob.client.page.base.PostStreamFragment;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Standard response handler for Post streams.
 *
 * Use {@link #getPosts()} in {@link #onCallback()} to get the returned posts
 */
public abstract class PostStreamResponseHandler extends StreamResponseHandler<PostStreamFragment>
{
	@Getter @Setter private int newPostCount = 0;

	public PostStreamResponseHandler(Context c, boolean append)
	{
		super(c, append);
	}

	@Override public void onSuccess()
	{
		JsonElement elements = getContent();

		if (elements != null)
		{
			MultiFileCacheWriter cacheWriter = new MultiFileCacheWriter();

			try
			{
				JsonArray jPosts = elements.getAsJsonObject().get("data").getAsJsonArray();
				int size = jPosts.size();

				setObjects(new ArrayList<NetObject>(size));

				for (int index = 0; index < size; index++)
				{
					JsonObject post = jPosts.get(index).getAsJsonObject();
					Post p = new Post().createFrom(post);

					// something wrong happened (maybe a deletion)
					if (p == null) continue;
					getObjects().add(p);
					cacheWriter.scheduleAsyncWrite(String.format(Constants.CACHE_USER, p.getPoster().getId()), p.getPoster());
				}

				if (elements.getAsJsonObject().has("meta"))
				{
					JsonObject meta = elements.getAsJsonObject().get("meta").getAsJsonObject();

					if (meta.has("max_id"))
					{
						setFirstId(meta.get("max_id").getAsString());
					}
					else if (getObjects().size() > 0)
					{
						setFirstId(getObjects().get(0).getId());
					}

					if (meta.has("min_id"))
					{
						setLastId(meta.get("min_id").getAsString());
					}
					else if (getObjects().size() > 0)
					{
						setLastId(getObjects().get(getObjects().size() - 1).getId());
					}

					if (meta.has("more"))
					{
						setHasMore(meta.get("more").getAsBoolean());
					}

					setMarker(StreamMarker.fromObject(meta));
				}

				cacheWriter.executeAsyncWriteList();
				onCallback();
			}
			catch (Exception e)
			{
				Debug.out(getConnectionInfo());
				Debug.out(e);
				setDidFail(true);
			}
		}
		else
		{
			setDidFail(true);
		}
	}
}