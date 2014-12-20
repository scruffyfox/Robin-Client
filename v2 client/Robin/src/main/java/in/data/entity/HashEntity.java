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
public class HashEntity extends Entity
{
	protected String name;

	@Override public HashEntity createFrom(JsonElement element)
	{
		if (super.createFrom(element) != null)
		{
			try
			{
				JsonObject hashObject = element.getAsJsonObject();

				this.name = hashObject.get("name").getAsString();

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public HashEntity createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public List<HashEntity> createListFrom(JsonElement element)
	{
		try
		{
			JsonArray hashArray = element.getAsJsonArray();
			ArrayList<HashEntity> hashs = new ArrayList<HashEntity>(hashArray.size());

			for (JsonElement hashElement : hashArray)
			{
				HashEntity hash = new HashEntity().createFrom(hashElement);

				if (hash != null)
				{
					hashs.add(hash);
				}
			}

			return hashs;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public String getVersion()
	{
		return "bacd059b-796f-42d6-8f26-279afdf4fd85";
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
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public boolean equals(Object object)
	{
		if (object == null)
		{
			return false;
		}

		if ((object == this) || (object instanceof HashEntity && ((HashEntity)object).getName().equals(getName())))
		{
			return true;
		}

		return false;
	}

	public static final Parcelable.Creator<HashEntity> CREATOR = new Creator<HashEntity>()
	{
		@Override public HashEntity[] newArray(int size)
		{
			return new HashEntity[size];
		}

		@Override public HashEntity createFromParcel(Parcel source)
		{
			return new HashEntity().createFrom(source);
		}
	};
}
