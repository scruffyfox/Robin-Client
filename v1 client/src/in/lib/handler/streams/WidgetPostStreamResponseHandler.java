package in.lib.handler.streams;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.handler.base.WidgetResponseHandler;
import in.lib.manager.CacheManager;
import in.lib.writer.MultiFileCacheWriter;
import in.model.Post;
import in.model.Stream;
import in.obj.StreamMarker;
import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Standard response handler for Post streams.
 *
 * Use {@link #getPosts()} in {@link #onCallback()} to get the returned posts
 */
public abstract class WidgetPostStreamResponseHandler extends WidgetResponseHandler
{
	public WidgetPostStreamResponseHandler(Context c, String cacheFile)
	{
		super(c, cacheFile);
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
			}
			catch (Exception e)
			{
				Debug.out(getConnectionInfo());
				e.printStackTrace();
			}

			cacheWriter.executeAsyncWriteList();

			Stream s = new Stream();
			s.setObjects(getObjects());
			s.setMarker(getMarker());
			s.setHasMore(getHasMore());
			s.setMaxId(getFirstId());
			s.setMinId(getLastId());

			CacheManager.getInstance().writeFile(getCacheFileName(), s);
			onCallback();
		}
		else
		{
			onFinish(true);
		}
	}

	@Override public void onFailure()
	{
		Debug.out(getConnectionInfo());
	}

	/**
	 * Implement this callback and use {@link #getPosts()} to get the returned List of posts
	 * For fragments, use onCallback to execute {@link RobinFragment.runWhenReady()} and pass <b>this</b>
	 * Then override {@link #run()} to finish the adapter stuff.
	 *
	 * See also: {@link #getHasMore()}, {@link #getLastId()}
	 */
	public abstract void onCallback();
}