package in.lib.handler.streams;

import in.lib.Debug;
import in.lib.handler.base.RobinResponseHandler;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class TrendingStreamResponseHandler extends RobinResponseHandler
{
	@Getter private List<String> trending = new ArrayList<String>();

	public TrendingStreamResponseHandler(Context c)
	{
		super(c);
	}

	@Override public void onSuccess()
	{
		JsonElement elements = getContent();

		try
		{
			JsonArray tags = elements.getAsJsonObject().get("hashtags").getAsJsonArray();

			int size = tags.size();
			for (int index = 0; index < size; index++)
			{
				JsonObject tag = tags.get(index).getAsJsonObject();
				trending.add(tag.get("term").getAsString());
			}
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		onCallback();
	}

	/**
	 * Implement this callback and use {@link #getPosts()} to get the returned List of posts
	 * For fragments, use onCallback to execute {@link RobinFragment.runWhenReady()} and pass <b>this</b>
	 * Then override {@link #run()} to finish the adapter stuff.
	 *
	 * See also: {@link #getHasMore()}, {@link #getLastId()}
	 */
	public abstract void onCallback();

	@Override public void onFinish()
	{

	}
}