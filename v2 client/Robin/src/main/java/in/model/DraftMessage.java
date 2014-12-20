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
public class DraftMessage extends Draft
{
	protected String replyId;
	protected String channelId;

	@Override public void save()
	{
		CacheManager.getInstance().writeFile("message_" + getDate(), this);
	}

	@Override public DraftMessage load(String id)
	{
		return null;//CacheManager.getInstance().readFileAsObject("message_" + id, DraftMessage.class);
	}

	@Override public void delete()
	{
		CacheManager.getInstance().removeFile("message_" + getDate());
	}

	@Override public DraftMessage createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public String getVersion()
	{
		return "af4b4eda-942e-4c2e-a1b0-72b0735bdf61";
	}

	@Override public void write(SerialWriterUtil util)
	{
		super.write(util);

		try
		{
			util.writeString(getVersion());
			util.writeString(replyId);
			util.writeString(channelId);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public DraftMessage read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				replyId = util.readString();
				channelId = util.readString();

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	public static final Parcelable.Creator<DraftMessage> CREATOR = new Creator<DraftMessage>()
	{
		@Override public DraftMessage[] newArray(int size)
		{
			return new DraftMessage[size];
		}

		@Override public DraftMessage createFromParcel(Parcel source)
		{
			return new DraftMessage().createFrom(source);
		}
	};
}