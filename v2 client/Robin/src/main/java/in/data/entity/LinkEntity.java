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
public class LinkEntity extends Entity
{
	protected String text;
	protected String url;
	protected int amendedLength;

	@Override public LinkEntity createFrom(JsonElement element)
	{
		if (super.createFrom(element) != null)
		{
			try
			{
				JsonObject linkObject = element.getAsJsonObject();

				this.text = linkObject.get("text").getAsString();
				this.url = linkObject.get("url").getAsString();

				if (linkObject.has("amended_len"))
				{
					this.amendedLength = linkObject.get("amended_len").getAsInt();
				}

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public LinkEntity createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public List<LinkEntity> createListFrom(JsonElement element)
	{
		try
		{
			JsonArray linksArray = element.getAsJsonArray();
			ArrayList<LinkEntity> links = new ArrayList<LinkEntity>(linksArray.size());

			for (JsonElement linkElement : linksArray)
			{
				LinkEntity link = new LinkEntity().createFrom(linkElement);

				if (link != null)
				{
					links.add(link);
				}
			}

			return links;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public String getVersion()
	{
		return "eb860f29-d2fd-4466-a70b-856713597c53";
	}

	@Override public Entity read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				text = util.readString();
				url = util.readString();
				amendedLength = util.readInt();

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
			util.writeString(text);
			util.writeString(url);
			util.writeInt(amendedLength);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public static final Parcelable.Creator<LinkEntity> CREATOR = new Creator<LinkEntity>()
	{
		@Override public LinkEntity[] newArray(int size)
		{
			return new LinkEntity[size];
		}

		@Override public LinkEntity createFromParcel(Parcel source)
		{
			return new LinkEntity().createFrom(source);
		}
	};
}
