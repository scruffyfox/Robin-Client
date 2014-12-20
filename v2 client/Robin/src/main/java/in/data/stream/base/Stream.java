package in.data.stream.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import in.data.Meta;
import in.lib.utils.Debug;
import in.lib.utils.SerialWriterUtil;
import in.model.AdnModel;
import in.model.base.Model;
import lombok.Data;

@Data
public abstract class Stream<T extends AdnModel> extends Model
{
	protected Meta meta;
	protected List<T> items = new ArrayList<T>();

	@Override public Stream createFrom(JsonElement element)
	{
		try
		{
			JsonObject streamObject = element.getAsJsonObject();
			JsonElement metaObject = streamObject.get("meta");
			this.meta = new Meta().createFrom(metaObject);

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public String getVersion()
	{
		return "dbaa8ba9-1820-4480-8635-80934837b77f";
	}

	@Override public void write(SerialWriterUtil util)
	{
		try
		{
			util.writeString(getVersion());
			util.writeModel(meta);
			util.writeModelList(items);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

//	public static final Parcelable.Creator<Stream> CREATOR = new Creator<Stream>()
//	{
//		@Override public Stream[] newArray(int size)
//		{
//			return new Stream[size];
//		}
//
//		@Override public Stream createFromParcel(Parcel source)
//		{
//			return new Stream().createFrom(source);
//		}
//	};
}
