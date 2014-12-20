package in.lib.utils;

import android.os.Parcel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import in.model.base.Model;

public class SerialWriterUtil
{
	private Parcel parcelObject;
	private DataOutputStream streamOutputObject;

	public SerialWriterUtil(Parcel parcel)
	{
		this.parcelObject = parcel;
	}

	public SerialWriterUtil(DataOutputStream stream)
	{
		this.streamOutputObject = stream;
	}
	public synchronized void writeBoolean(boolean value) throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			parcelObject.writeByte(value ? (byte)1 : (byte)0);
		}
		else if (streamOutputObject != null)
		{
			streamOutputObject.writeBoolean(value);
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized void writeByte(byte value) throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			parcelObject.writeByte(value);
		}
		else if (streamOutputObject != null)
		{
			streamOutputObject.writeByte(value);
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized void writeInt(int value) throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			parcelObject.writeInt(value);
		}
		else if (streamOutputObject != null)
		{
			streamOutputObject.writeInt(value);
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized void writeLong(long value) throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			parcelObject.writeLong(value);
		}
		else if (streamOutputObject != null)
		{
			streamOutputObject.writeLong(value);
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized void writeDouble(double value) throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			parcelObject.writeDouble(value);
		}
		else if (streamOutputObject != null)
		{
			streamOutputObject.writeDouble(value);
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized void writeFloat(float value) throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			parcelObject.writeFloat(value);
		}
		else if (streamOutputObject != null)
		{
			streamOutputObject.writeFloat(value);
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized void writeString(String value) throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			parcelObject.writeString(value);
		}
		else if (streamOutputObject != null)
		{
			streamOutputObject.writeBoolean(value == null);

			if (value != null)
			{
				streamOutputObject.writeUTF(value);
			}
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized void writeStringList(List<String> value) throws IOException, IllegalAccessException
	{
		if (value == null)
		{
			writeInt(-1);
		}
		else
		{
			writeInt(value.size());

			for (String str : value)
			{
				writeString(str);
			}
		}
	}

	public synchronized void writeSerializable(Serializable value) throws IOException, IllegalAccessException
	{
		if (parcelObject != null)
		{
			parcelObject.writeSerializable(value);
		}
		else if (streamOutputObject != null)
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(value);
			streamOutputObject.writeInt(bos.size());
			streamOutputObject.write(bos.toByteArray());
		}
		else
		{
			throw new IllegalAccessException("No object to read from");
		}
	}

	public synchronized void writeModel(Model value) throws IOException, IllegalAccessException
	{
		writeBoolean(value == null);

		if (value != null)
		{
			value.write(this);
		}
	}

	public synchronized void writeArrayList(List value) throws IOException, IllegalAccessException
	{
		ParameterizedType superclass = (ParameterizedType)value.getClass().getGenericSuperclass();
		Type[] types = superclass.getActualTypeArguments();
		Class<?> actualType = null;
		if (types != null && types.length > 0 && (types[0] instanceof Class<?>))
		{
			actualType = (Class<?>)types[0];
		}

		writeString(actualType.getName());
		writeInt(value.size());

		for (Object o : value)
		{
			if (o instanceof String)
			{
				writeString((String)o);
			}
		}
	}

	public synchronized void writeModelList(List<? extends Model> value) throws IOException, IllegalAccessException
	{
		if (value == null)
		{
			writeInt(-1);
		}
		else
		{
			writeInt(value.size());

			for (Model model : value)
			{
				writeModel(model);
			}
		}
	}
}
