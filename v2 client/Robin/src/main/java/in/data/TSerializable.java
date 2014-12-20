package in.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface TSerializable
{
	public void writeToBuffer(DataOutputStream buffer);
	public Object readFromBuffer(DataInputStream buffer);
}
