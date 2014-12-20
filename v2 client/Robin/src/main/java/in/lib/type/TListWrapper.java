package in.lib.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

import in.data.TSerializable;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.model.base.Model;
import lombok.Getter;
import lombok.ToString;

@ToString
public class TListWrapper implements TSerializable
{
	@Getter private List list;
	private Class instanceType;

	public TListWrapper(List list, Class instanceType)
	{
		this.list = list;
		this.instanceType = instanceType;
	}

	public TListWrapper()
	{
	}

	@Override public void writeToBuffer(DataOutputStream buffer)
	{
		try
		{
			SerialWriterUtil writer = new SerialWriterUtil(buffer);
			writer.writeString(instanceType.getName());
			writer.writeModelList(list);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public TListWrapper readFromBuffer(DataInputStream buffer)
	{
		try
		{
			SerialReaderUtil reader = new SerialReaderUtil(buffer);
			instanceType = (Class<? extends Model>)Class.forName(reader.readString());
			this.list = reader.readModelList(instanceType);
			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}
}
