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
public class CrossPostAnnotation extends Annotation
{
	protected String canonicalUrl;

	@Override public JsonElement toAnnotation()
	{
		JsonObject object = new JsonObject();
		JsonObject values = new JsonObject();

		values.addProperty("canonical_url", getCanonicalUrl());

		object.addProperty("type", getAnnotationKey());
		object.add("value", values);

		return object;
	}

	@Override public CrossPostAnnotation createFrom(JsonElement element)
	{
		try
		{
			JsonObject value = element.getAsJsonObject();

			this.canonicalUrl = value.get("canonical_url").getAsString();

			return this;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override public CrossPostAnnotation createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "f863008e-0246-43cf-b1c5-d4c3cc9099d3";
	}

	@Override public CrossPostAnnotation read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				this.canonicalUrl = util.readString();

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
			util.writeString(this.canonicalUrl);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		return null;
	}

	@Override public String getAnnotationKey()
	{
		return "net.app.core.crosspost";
	}

	public static final Creator<CrossPostAnnotation> CREATOR = new Creator<CrossPostAnnotation>()
	{
		@Override public CrossPostAnnotation[] newArray(int size)
		{
			return new CrossPostAnnotation[size];
		}

		@Override public CrossPostAnnotation createFromParcel(Parcel source)
		{
			return new CrossPostAnnotation().createFrom(source);
		}
	};
}