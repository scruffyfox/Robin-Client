package in.lib.handler.base;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.writer.MultiFileCacheWriter;
import in.model.User;
import in.model.base.NetObject;
import in.obj.StreamMarker;
import in.rob.client.page.base.UserStreamFragment;

import java.util.ArrayList;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Standard response handler for User streams.
 *
 * Use {@link #getUsers()} in {@link #onCallback()} to get the returned users
 */
public abstract class UserStreamResponseHandler extends StreamResponseHandler<UserStreamFragment>
{
	public UserStreamResponseHandler(Context c, boolean append)
	{
		super(c, append);
	}

	@Override public void onSuccess()
	{
		JsonElement elements = getContent();

		if (elements != null)
		{
			try
			{
				MultiFileCacheWriter writer = new MultiFileCacheWriter();

				JsonArray jUsers = elements.getAsJsonObject().get("data").getAsJsonArray();
				int size = jUsers.size();

				setObjects(new ArrayList<NetObject>(size));

				for (int index = 0; index < size; index++)
				{
					JsonObject userObj = jUsers.get(index).getAsJsonObject();
					User user = new User().createFrom(userObj);

					if (user == null) continue;
					getObjects().add(user);
					writer.scheduleAsyncWrite(String.format(Constants.CACHE_USER, user.getId()), user);
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

				onCallback();
				writer.executeAsyncWriteList();
			}
			catch (Exception e)
			{
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