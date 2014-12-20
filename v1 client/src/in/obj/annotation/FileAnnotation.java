package in.obj.annotation;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FileAnnotation extends ImageAnnotation
{
	private static final long serialVersionUID = 175492480681929929L;

	@Getter @Setter private String fileId;
	@Getter @Setter private String fileToken;

	private void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeUTF(fileId);
		oos.writeUTF(fileToken);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		fileId = ois.readUTF();
		fileToken = ois.readUTF();
	}

	@Override public JsonElement toAnnotation()
	{
		JsonObject object = new JsonObject();
		JsonObject file = new JsonObject();
		JsonObject value = new JsonObject();
		file.addProperty("file_id", getFileId());
		file.addProperty("file_token", getFileToken());
		file.addProperty("format", "oembed");
		object.addProperty("type", "net.app.core.oembed");
		value.add("+net.app.core.file", file);
		object.add("value", value);

		return object;
	}

	@Override public String getAnnotationKey()
	{
		return "";
	}
}