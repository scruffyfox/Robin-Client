package in.lib.utils;

import android.os.Parcel;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import in.model.base.Model;

public class SerialReaderUtil
{
	private Parcel parcelObject;
	private DataInputStream streamInputObject;

	public SerialReaderUtil(Parcel parcel)
	{
		this.parcelObject = parcel;
	}

	public SerialReaderUtil(DataInputStream stream)
	{
		this.streamInputObject = stream;
	}

	public synchronized boolean readBoolean() throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			return parcelObject.readByte() == (byte)1 ? true : false;
		}
		else if (streamInputObject != null)
		{
			return streamInputObject.readBoolean();
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized byte readByte() throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			return parcelObject.readByte();
		}
		else if (streamInputObject != null)
		{
			return streamInputObject.readByte();
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized int readInt() throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			return parcelObject.readInt();
		}
		else if (streamInputObject != null)
		{
			return streamInputObject.readInt();
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized long readLong() throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			return parcelObject.readLong();
		}
		else if (streamInputObject != null)
		{
			return streamInputObject.readLong();
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized double readDouble() throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			return parcelObject.readDouble();
		}
		else if (streamInputObject != null)
		{
			return streamInputObject.readDouble();
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized float readFloat() throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			return parcelObject.readFloat();
		}
		else if (streamInputObject != null)
		{
			return streamInputObject.readFloat();
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized String readString() throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			return parcelObject.readString();
		}
		else if (streamInputObject != null)
		{
			boolean isNull = streamInputObject.readBoolean();
			return isNull ? null : streamInputObject.readUTF();
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized List<String> readStringList() throws IOException, IllegalAccessException
	{
		int size = readInt();

		if (size < 0) return null;

		ArrayList<String> list = new ArrayList<String>(size);
		for (int index = 0; index < size; index++)
		{
			list.add(index, readString());
		}

		return list;
	}

	public synchronized Serializable readSerializable() throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			return parcelObject.readSerializable();
		}
		else if (streamInputObject != null)
		{
			int size = streamInputObject.readInt();
			byte[] buffer = new byte[size];
			streamInputObject.read(buffer, 0, size);

			BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(buffer), 8192);
			ObjectInputStream input = new ObjectInputStream(bis);

			try
			{
				Object objectData = input.readObject();
				return (Serializable)objectData;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
			finally
			{
				input.close();
				bis.close();
			}

			return null;
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized <T extends Model> T readModel(Class<T> model) throws IOException, IllegalAccessException
	{
		try
		{
			boolean isNull = readBoolean();
			return isNull ? null : model.cast(model.newInstance().read(this));
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public synchronized <T extends Model> List<T> readModelList(Class<T> model) throws IOException, IllegalAccessException
	{
		int size = readInt();

		if (size < 0) return null;

		ArrayList list = new ArrayList(size);
		for (int index = 0; index < size; index++)
		{
			Model m = readModel(model);

			// something failed to read
			if (m == null)
			{
				throw new RuntimeException("Failed to read model list at index " + index);
			}

			list.add(index, m);
		}

		return list;
	}
}
