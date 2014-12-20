package in.data.entity;

import android.os.Parcel;

import com.google.gson.JsonElement;

import java.util.List;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import lombok.Data;

@Data
public class StyledEntity extends Entity
{
	public static enum Type
	{
		ITALIC,
		BOLD,
		UNDERLINE;
	}

	private Type type;

	public List<? extends StyledEntity> createListFrom(String text)
	{
		return null;
	}

	@Override public StyledEntity createFrom(JsonElement element)
	{
		return null;
	}

	@Override public List<StyledEntity> createListFrom(JsonElement element)
	{
		return null;
	}

	@Override public StyledEntity createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "924b14d3-e113-4c18-a9ee-c587ceab40d0";
	}

	@Override public Entity read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				this.type = Type.values()[util.readInt()];

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
			util.writeInt(type.ordinal());
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public static final Creator<StyledEntity> CREATOR = new Creator<StyledEntity>()
	{
		@Override public StyledEntity[] newArray(int size)
		{
			return new StyledEntity[size];
		}

		@Override public StyledEntity createFromParcel(Parcel source)
		{
			return new StyledEntity().createFrom(source);
		}
	};
}
