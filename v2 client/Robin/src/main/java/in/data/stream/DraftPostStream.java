package in.data.stream;

import android.os.Parcel;

import com.google.gson.JsonElement;

import java.util.List;

import in.data.Meta;
import in.data.stream.base.Stream;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.model.DraftPost;
import in.model.base.Model;

public class DraftPostStream extends Stream<DraftPost>
{
	@Override public DraftPostStream createFrom(Parcel parcel)
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
		return "776f712b-0549-4f56-a3bf-5dea1ff9f9d8";
	}

	@Override public DraftPostStream read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			meta = util.readModel(Meta.class);
			items = util.readModelList(DraftPost.class);

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	public static final Creator<DraftPostStream> CREATOR = new Creator<DraftPostStream>()
	{
		@Override public DraftPostStream[] newArray(int size)
		{
			return new DraftPostStream[size];
		}

		@Override public DraftPostStream createFromParcel(Parcel source)
		{
			return new DraftPostStream().createFrom(source);
		}
	};
}
