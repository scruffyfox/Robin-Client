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
import in.model.Post;
import in.model.base.Model;

public class PostStream extends Stream<Post>
{
	@Override public PostStream createFrom(JsonElement element)
	{
		if (super.createFrom(element) != null)
		{
			try
			{
				JsonObject streamObject = element.getAsJsonObject();
				JsonElement dataObject = streamObject.get("data");
				this.items = new Post().createListFrom(dataObject);

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

	@Override public PostStream createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "1d9cb565-dce4-470c-98a4-50ebfd12363a";
	}

	@Override public PostStream read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			meta = util.readModel(Meta.class);
			items = util.readModelList(Post.class);

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	public static final Parcelable.Creator<PostStream> CREATOR = new Creator<PostStream>()
	{
		@Override public PostStream[] newArray(int size)
		{
			return new PostStream[size];
		}

		@Override public PostStream createFromParcel(Parcel source)
		{
			return new PostStream().createFrom(source);
		}
	};
}
