package in.data.annotation;

import android.os.Parcel;

import com.google.gson.JsonElement;

import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.model.AdnModel;
import lombok.Data;

@Data
public abstract class Annotation extends AdnModel
{
	protected String previewUrl;

	public abstract JsonElement toAnnotation();
	public abstract String getAnnotationKey();

	@Override public Annotation createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "49cc5140-2dc2-44af-8412-973724b8244e";
	}

	@Override public Annotation read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				previewUrl = util.readString();

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
			util.writeString(previewUrl);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}
}
