package in.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.rob.client.R;
import lombok.Data;
import lombok.Getter;

@Data
public class Interaction extends AdnModel
{
	public enum Type
	{
		REPOST(R.drawable.ic_repost_light),
		STAR(R.drawable.ic_unstar_light),
		FOLLOW(R.drawable.ic_follower_light);

		@Getter private int icon;

		private Type(int icon)
		{
			this.icon = icon;
		}
	}

	protected Type type;
	protected long date;
	protected AdnModel object;
	protected List<User> users;

	@Override public Interaction createFrom(JsonElement element)
	{
		try
		{
			JsonObject interactionObject = element.getAsJsonObject();
			String action = interactionObject.get("action").getAsString();

			if (action.equals("repost"))
			{
				this.type = Type.REPOST;
			}
			else if (action.equals("star"))
			{
				this.type = Type.STAR;
			}
			else if (action.equals("follow"))
			{
				this.type = Type.FOLLOW;
			}

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			Date postDate = format.parse(interactionObject.get("event_date").getAsString());
			this.date = postDate.getTime();

			JsonElement objectObject = interactionObject.get("objects").getAsJsonArray().get(0);

			if (type == Type.STAR || type == Type.REPOST)
			{
				this.object = new Post().createFrom(objectObject);

				if (((Post)this.object).isDeleted())
				{
					return null;
				}
			}
			else
			{
				this.object = new User().createFrom(objectObject);
			}

			this.users = new User().createListFrom(interactionObject.get("users"));

			return this;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override public List<Interaction> createListFrom(JsonElement element)
	{
		try
		{
			JsonArray interactionArray = element.getAsJsonArray();
			ArrayList<Interaction> interactions = new ArrayList<Interaction>(interactionArray.size());

			for (JsonElement interactionElement : interactionArray)
			{
				Interaction interaction = new Interaction().createFrom(interactionElement);

				if (interaction != null)
				{
					interactions.add(interaction);
				}
			}

			return interactions;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public Interaction createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "346bdf8b-d494-4654-b82f-5b95898f8662";
	}

	@Override public void write(SerialWriterUtil util)
	{
		super.write(util);

		try
		{
			util.writeString(getVersion());
			util.writeInt(type.ordinal());
			util.writeLong(date);
			util.writeModel(object);
			util.writeModelList(users);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public Interaction read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				type = Type.values()[util.readInt()];
				date = util.readLong();
				object = util.readModel(type == Type.REPOST || type == Type.STAR ? Post.class : User.class);
				users = util.readModelList(User.class);

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	public static final Parcelable.Creator<Interaction> CREATOR = new Creator<Interaction>()
	{
		@Override public Interaction[] newArray(int size)
		{
			return new Interaction[size];
		}

		@Override public Interaction createFromParcel(Parcel source)
		{
			return new Interaction().createFrom(source);
		}
	};
}
