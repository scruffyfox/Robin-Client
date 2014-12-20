package in.lib.handler.base;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.writer.MultiFileCacheWriter;
import in.model.PrivateMessage;
import in.model.base.NetObject;
import in.obj.StreamMarker;
import in.rob.client.page.base.MessageStreamFragment;

import java.util.ArrayList;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Standard response handler for Message streams.
 *
 * Use {@link #getMessages()} in {@link #onCallback()} to get the returned Messages
 */
public abstract class MessageStreamResponseHandler extends StreamResponseHandler<MessageStreamFragment>
{
	public MessageStreamResponseHandler(Context c, boolean append)
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
				JsonArray jMessages = elements.getAsJsonObject().get("data").getAsJsonArray();

				int size = jMessages.size();
				setObjects(new ArrayList<NetObject>(size));

				for (int index = 0; index < size; index++)
				{
					JsonObject message = jMessages.get(index).getAsJsonObject();
					PrivateMessage p = new PrivateMessage().createFrom(message);

					// something wrong happened (maybe a deletion)
					if (p == null) continue;
					getObjects().add(p);
					writer.scheduleAsyncWrite(String.format(Constants.CACHE_USER, p.getPoster().getId()), p.getPoster());
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