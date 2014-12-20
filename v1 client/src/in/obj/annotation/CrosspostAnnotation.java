package in.obj.annotation;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CrosspostAnnotation extends ImageAnnotation
{
	private static final long serialVersionUID = 84172983129929L;

	@Getter @Setter private String url;

	private void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeUTF(url);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		url = ois.readUTF();
	}

	@Override public JsonElement toAnnotation()
	{
		JsonObject object = new JsonObject();
		JsonObject values = new JsonObject();

		values.addProperty("canonical_url", getUrl());
		object.addProperty("type", getAnnotationKey());
		object.add("value", values);

		return object;
	}

	@Override public String getAnnotationKey()
	{
		return "net.app.core.crosspost";
	}
}