package in.data.annotation;

import android.os.Parcel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.model.base.Model;
import lombok.Data;

@Data
public class CheckinAnnotation extends LocationAnnotation
{
	protected String name;
	protected String address;
	protected String website;
	protected String countryCode;

	public CheckinAnnotation()
	{

	}

	@Override public CheckinAnnotation createFrom(JsonElement element)
	{
		try
		{
			JsonObject value = element.getAsJsonObject();

			this.name = value.get("name").getAsString();

			if (value.has("address") && !value.get("address").isJsonNull())
			{
				this.address = value.get("address").getAsString();
			}

			if (value.has("website") && !value.get("website").isJsonNull())
			{
				this.website = value.get("website").getAsString();
			}

			if (value.has("country_code") && !value.get("country_code").isJsonNull())
			{
				this.countryCode = value.get("country_code").getAsString();
			}

			return this;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override public CheckinAnnotation createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		return null;
	}

	@Override public String getVersion()
	{
		return "47f810fc-42d8-4c28-8653-37a0bacfc4c4";
	}

	@Override public CheckinAnnotation read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				this.name = util.readString();
				this.address = util.readString();
				this.website = util.readString();
				this.countryCode = util.readString();

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
			util.writeString(address);
			util.writeString(website);
			util.writeString(countryCode);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public JsonElement toAnnotation()
	{
		return null;
	}

	@Override public String getAnnotationKey()
	{
		return "net.app.core.checkin";
	}

	public static final Creator<CheckinAnnotation> CREATOR = new Creator<CheckinAnnotation>()
	{
		@Override public CheckinAnnotation[] newArray(int size)
		{
			return new CheckinAnnotation[size];
		}

		@Override public CheckinAnnotation createFromParcel(Parcel source)
		{
			return new CheckinAnnotation().createFrom(source);
		}
	};
}