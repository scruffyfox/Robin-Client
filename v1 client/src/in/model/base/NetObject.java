package in.model.base;

import in.lib.manager.CacheManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import android.os.Parcel;
import android.os.Parcelable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.google.gson.JsonObject;

/**
 * Base class for Posts and Users
 */
@ToString(includeFieldNames = true)
public class NetObject implements Parcelable
{
	@Tag(0x01) @NonNull @Getter @Setter private String id = "-1";
	@Tag(0x02) @NonNull @Getter @Setter private String filterTag = "";

	/**
	 * Stub method. You should implement this in your subclass to
	 * populate the class' members from @param object
	 * @param object The json object to populate
	 */
	public NetObject createFrom(JsonObject object)
	{
		return null;
	}

	/**
	 * Stub method. You should implement this in your subclass to
	 * handle Parcealable serialization.
	 * @param object
	 */
	public NetObject createFrom(Parcel object)
	{
		setId(object.readString());
		setFilterTag(object.readString());
		return this;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getId());
		dest.writeString(filterTag);
	}

	@Override public int describeContents()
	{
		return 0;
	}

	public byte[] serialize()
	{
		return CacheManager.getInstance().serialize(this);
	}

	public static NetObject deserialize(byte[] data)
	{
		return CacheManager.getInstance().deserialize(data, NetObject.class);
	}

	public static final Parcelable.Creator<NetObject> CREATOR = new Creator<NetObject>()
	{
		@Override public NetObject[] newArray(int size)
		{
			return new NetObject[size];
		}

		@Override public NetObject createFromParcel(Parcel source)
		{
			return new NetObject().createFrom(source);
		}
	};
}