package in.model.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

import in.data.TSerializable;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;

public abstract class Model implements Parcelable, TSerializable
{
	public Model()
	{
	}

	public abstract Model read(SerialReaderUtil util);
	public abstract void write(SerialWriterUtil util);
	public abstract Model createFrom(JsonElement element);
	public abstract List<? extends Model> createListFrom(JsonElement element);

	public void save()
	{

	}

	@Override public int describeContents()
	{
		return 0;
	}

	public String getVersion()
	{
		return "";
	}

	public Model createFrom(Parcel parcel)
	{
		return read(new SerialReaderUtil(parcel));
	}

	public void writeToParcel(Parcel dest, int flags)
	{
		write(new SerialWriterUtil(dest));
	}

	@Override public Model readFromBuffer(DataInputStream buffer)
	{
		return read(new SerialReaderUtil(buffer));
	}

	@Override public void writeToBuffer(DataOutputStream buffer)
	{
		write(new SerialWriterUtil(buffer));
	}
}