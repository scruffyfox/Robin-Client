package in.data.stream;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import in.data.Meta;
import in.data.stream.base.Stream;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.model.User;
import in.model.base.Model;

public class UserStream extends Stream<User>
{
	@Override public UserStream createFrom(JsonElement element)
	{
		if (super.createFrom(element) != null)
		{
			try
			{
				JsonObject streamObject = element.getAsJsonObject();
				JsonElement dataObject = streamObject.get("data");
				this.items = new User().createListFrom(dataObject);

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		return null;
	}

	@Override public UserStream createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "33f53b07-aee9-4c73-ad52-384ba0e0a52d";
	}

	@Override public UserStream read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			meta = util.readModel(Meta.class);
			items = util.readModelList(User.class);

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	public static final Parcelable.Creator<UserStream> CREATOR = new Creator<UserStream>()
	{
		@Override public UserStream[] newArray(int size)
		{
			return new UserStream[size];
		}

		@Override public UserStream createFromParcel(Parcel source)
		{
			return new UserStream().createFrom(source);
		}
	};
}
