package in.data.entity;

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
public class MentionEntity extends Entity
{
	protected String id;
	protected String name;

	@Override public MentionEntity createFrom(JsonElement element)
	{
		if (super.createFrom(element) != null)
		{
			try
			{
				JsonObject mentionObject = element.getAsJsonObject();

				this.id = mentionObject.get("id").getAsString();
				this.name = mentionObject.get("name").getAsString();

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public MentionEntity createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public List<MentionEntity> createListFrom(JsonElement element)
	{
		try
		{
			JsonArray mentionsArray = element.getAsJsonArray();
			ArrayList<MentionEntity> mentions = new ArrayList<MentionEntity>(mentionsArray.size());

			for (JsonElement mentionElement : mentionsArray)
			{
				MentionEntity mention = new MentionEntity().createFrom(mentionElement);

				if (mention != null)
				{
					mentions.add(mention);
				}
			}

			return mentions;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public String getVersion()
	{
		return "37560027-e204-4657-8427-7a8248823ade";
	}

	@Override public Entity read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				name = util.readString();
				id = util.readString();

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
			util.writeString(name);
			util.writeString(id);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public static final Parcelable.Creator<MentionEntity> CREATOR = new Creator<MentionEntity>()
	{
		@Override public MentionEntity[] newArray(int size)
		{
			return new MentionEntity[size];
		}

		@Override public MentionEntity createFromParcel(Parcel source)
		{
			return new MentionEntity().createFrom(source);
		}
	};
}
