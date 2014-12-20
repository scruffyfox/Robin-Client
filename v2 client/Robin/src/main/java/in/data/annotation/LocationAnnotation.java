package in.data.annotation;

import android.location.Location;
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
public class LocationAnnotation extends Annotation
{
	protected double lat = 0.0, lng = 0.0;
	protected float accuracy = Float.MIN_VALUE;
	protected long time = 0L;

	public LocationAnnotation()
	{

	}

	public LocationAnnotation(Location l)
	{
		if (l != null)
		{
			lat = l.getLatitude();
			lng = l.getLongitude();
			accuracy = l.getAccuracy();
			time = l.getTime();
		}
	}

	public boolean hasAccuracy()
	{
		return accuracy > Float.MIN_VALUE;
	}

	@Override public LocationAnnotation createFrom(JsonElement element)
	{
		return null;
	}

	@Override public LocationAnnotation createFrom(Parcel parcel)
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
		return "dc32493e-6bb7-4566-91c8-470ebc5d9352";
	}

	@Override public LocationAnnotation read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				lat = util.readDouble();
				lng = util.readDouble();
				accuracy = util.readFloat();
				time = util.readLong();

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
			util.writeDouble(lat);
			util.writeDouble(lng);
			util.writeFloat(accuracy);
			util.writeLong(time);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public JsonElement toAnnotation()
	{
		JsonObject object = new JsonObject();
		JsonObject values = new JsonObject();
		values.addProperty("latitude", getLat());
		values.addProperty("longitude", getLng());
		object.addProperty("type", getAnnotationKey());
		object.add("value", values);

		return object;
	}

	@Override public String getAnnotationKey()
	{
		return "net.app.core.geolocation";
	}

	public static final Parcelable.Creator<LocationAnnotation> CREATOR = new Creator<LocationAnnotation>()
	{
		@Override public LocationAnnotation[] newArray(int size)
		{
			return new LocationAnnotation[size];
		}

		@Override public LocationAnnotation createFromParcel(Parcel source)
		{
			return new LocationAnnotation().createFrom(source);
		}
	};
}