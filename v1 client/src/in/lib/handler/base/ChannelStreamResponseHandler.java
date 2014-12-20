package in.lib.handler.base;

import in.lib.Debug;
import in.lib.manager.APIManager;
import in.model.Channel;
import in.model.SimpleUser;
import in.model.User;
import in.model.base.NetObject;
import in.obj.StreamMarker;
import in.rob.client.page.base.ChannelStreamFragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Standard response handler for Channel streams.
 *
 * Use {@link #getChannels()} in {@link #onCallback()} to get the returned channels
 */
public abstract class ChannelStreamResponseHandler extends StreamResponseHandler<ChannelStreamFragment>
{
	public ChannelStreamResponseHandler(Context c, boolean append)
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
				JsonArray jPosts = elements.getAsJsonObject().get("data").getAsJsonArray();
				List<String> downloadIds = new ArrayList<String>();
				int size = jPosts.size();

				setObjects(new ArrayList<NetObject>(size));

				for (int index = 0; index < size; index++)
				{
					JsonObject channel = jPosts.get(index).getAsJsonObject();
					Channel p = new Channel().createFrom(channel);

					if (p == null)
					{
						continue;
					}

					for (String uId : p.getReaders())
					{
						if (!User.userSaved(uId))
						{
							if (!downloadIds.contains(uId))
							{
								downloadIds.add(uId);
							}
						}
						else
						{
							p.getUsers().add(SimpleUser.parseFromUser(User.loadUser(uId)));
						}
					}

					getObjects().add(p);
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

				// download all user's which we dont already have cached
				if (downloadIds.size() > 0)
				{
					final ChannelStreamResponseHandler that = this;
					APIManager.getInstance().getUsers(getContext(), downloadIds, new UserStreamResponseHandler(getContext(), false)
					{
						@Override public void onCallback()
						{
							int index = 0;
							for (NetObject channelObject : that.getObjects())
							{
								Channel channel = (Channel)channelObject;

								for (NetObject userObject : this.getObjects())
								{
									if (channel.getReaders().contains(userObject.getId()))
									{
										channel.getUsers().add(SimpleUser.parseFromUser((User)userObject));
									}
								}
							}

							that.onCallback();
						}

						@Override public void onFailure()
						{
							Debug.out(getConnectionInfo());
							that.onFailure();
						}
					});
				}
				else
				{
					onCallback();
				}
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

		return;
	}
}