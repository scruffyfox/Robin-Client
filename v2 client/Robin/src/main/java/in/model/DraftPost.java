package in.model;

import android.os.Parcel;
import android.os.Parcelable;

import in.lib.manager.CacheManager;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.model.base.Draft;
import lombok.Data;

@Data
public class DraftPost extends Draft
{
	protected String replyId;
	protected String repostId;

	@Override public void save()
	{
		CacheManager.getInstance().writeFile("post_" + getDate(), this);
	}

	@Override public DraftPost load(String id)
	{
		return CacheManager.getInstance().readFile("post_" + id, DraftPost.class);
	}

	@Override public DraftPost createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public void delete()
	{
		CacheManager.getInstance().removeFile("post_" + getDate());
	}

	@Override public String getVersion()
	{
		return "9f854785-6f36-429b-915e-c158e527eab4";
	}

	@Override public void write(SerialWriterUtil util)
	{
		super.write(util);

		try
		{
			util.writeString(getVersion());
			util.writeString(replyId);
			util.writeString(repostId);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public DraftPost read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				replyId = util.readString();
				repostId = util.readString();

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	public static final Parcelable.Creator<DraftPost> CREATOR = new Creator<DraftPost>()
	{
		@Override public DraftPost[] newArray(int size)
		{
			return new DraftPost[size];
		}

		@Override public DraftPost createFromParcel(Parcel source)
		{
			return new DraftPost().createFrom(source);
		}
	};
}