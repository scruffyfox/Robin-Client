package in.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.model.base.Model;
import lombok.Data;

@Data
public class Meta extends Model
{
	protected String minId = "-1";
	protected String maxId = "-1";
	protected boolean moreAvailable;

	@Override public Meta createFrom(JsonElement element)
	{
		try
		{
			JsonObject metaObject = element.getAsJsonObject();
			this.minId = metaObject.get("min_id").getAsString();
			this.maxId = metaObject.get("max_id").getAsString();
			this.moreAvailable = metaObject.get("more").getAsBoolean();

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public Meta createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		// Not used
		return null;
	}

	@Override public String getVersion()
	{
		return "0a7eb386-b54a-4d33-aeb6-15f1fa3de452";
	}

	@Override public Meta read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			minId = util.readString();
			maxId = util.readString();
			moreAvailable = util.readBoolean();

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public void write(SerialWriterUtil util)
	{
		try
		{
			util.writeString(getVersion());
			util.writeString(minId);
			util.writeString(maxId);
			util.writeBoolean(moreAvailable);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public static final Parcelable.Creator<Meta> CREATOR = new Creator<Meta>()
	{
		@Override public Meta[] newArray(int size)
		{
			return new Meta[size];
		}

		@Override public Meta createFromParcel(Parcel source)
		{
			return new Meta().createFrom(source);
		}
	};
}
