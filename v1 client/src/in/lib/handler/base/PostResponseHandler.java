package in.lib.handler.base;

import in.lib.Debug;
import in.model.Post;
import lombok.Getter;
import lombok.Setter;
import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class PostResponseHandler extends RobinResponseHandler
{
	@Getter @Setter private Post post;

	public PostResponseHandler(Context c)
	{
		super(c);
	}

	@Override public void onSuccess()
	{
		JsonElement elements = getContent();

		if (elements != null)
		{
			try
			{
				JsonObject jPost = elements.getAsJsonObject().get("data").getAsJsonObject();
				post = new Post().createFrom(jPost);

				if (post == null) return;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}

			// listen for when the fragment gets attached again
			onCallback();
		}
	}

	/**
	 * Implement this callback and use {@link #getUser()} to get the returned List of posts
	 * For fragments, use onCallback to execute {@link RobinFragment.runWhenReady()} and pass <b>this</b>
	 * Then override {@link #run()} to finish the adapter stuff.
	 */
	public abstract void onCallback();
}