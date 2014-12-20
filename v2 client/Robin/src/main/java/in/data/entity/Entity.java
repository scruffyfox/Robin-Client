package in.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.model.AdnModel;
import in.model.base.Model;
import lombok.Data;

@Data
public class Entity extends AdnModel
{
	protected int length;
	protected int pos;

	@Override public Entity createFrom(JsonElement element)
	{
		try
		{
			JsonObject entityObject = element.getAsJsonObject();

			this.length = entityObject.get("len").getAsInt();
			this.pos = entityObject.get("pos").getAsInt();

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		return null;
	}

	@Override public Entity createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}


	@Override public String getVersion()
	{
		return "0c7bb556-4358-435a-8d4a-fab5695076f0";
	}

	@Override public Entity read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			length = util.readInt();
			pos = util.readInt();

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
			util.writeInt(length);
			util.writeInt(pos);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public static final Parcelable.Creator<Entity> CREATOR = new Creator<Entity>()
	{
		@Override public Entity[] newArray(int size)
		{
			return new Entity[size];
		}

		@Override public Entity createFromParcel(Parcel source)
		{
			return new Entity().createFrom(source);
		}
	};
}
