package in.model;

import android.os.Parcel;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.model.base.Model;
import lombok.Data;

@Data
public abstract class AdnModel extends Model
{
	protected String id = "-1";

	@Override public AdnModel createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "824c8eff-47ff-41cf-a3ed-e63b18c37e8f";
	}

	@Override public void write(SerialWriterUtil util)
	{
		try
		{
			util.writeString(getVersion());
			util.writeString(id);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public AdnModel read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			id = util.readString();

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}
}
