package in.obj.annotation;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ChannelInviteAnnotation extends ImageAnnotation
{
	private static final long serialVersionUID = 124567122929L;

	@Getter @Setter private String channelId;

	private void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeUTF(channelId);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		channelId = ois.readUTF();
	}

	@Override public JsonElement toAnnotation()
	{
		JsonObject object = new JsonObject();
		JsonObject values = new JsonObject();

		values.addProperty("channel_id", getChannelId());
		object.addProperty("type", getAnnotationKey());
		object.add("value", values);

		return object;
	}

	@Override public String getAnnotationKey()
	{
		return "net.app.core.channel.invite";
	}
}