package in.obj.annotation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import android.location.Location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@ToString public class LocationAnnotation extends Annotation
{
	private static final long serialVersionUID = 895718951230000123L;

	@Getter @Setter private double lat = 0.0, lng = 0.0;
	@Getter private float accuracy = Float.MIN_VALUE;
	@Getter @Setter private long time = 0L;

	private void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeDouble(lat);
		oos.writeDouble(lng);
		oos.writeFloat(accuracy);
		oos.writeLong(time);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		lat = ois.readDouble();
		lng = ois.readDouble();
		accuracy = ois.readFloat();
		time = ois.readLong();
	}

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

	/*@Override public String getPreviewUrl()
	{
		Uri uri = Uri.parse(data.toString());
		return URLUtils.getMapThumbnail(uri);
	}*/

	public boolean hasAccuracy()
	{
		return accuracy > Float.MIN_VALUE;
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
}