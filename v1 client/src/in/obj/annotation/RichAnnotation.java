package in.obj.annotation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.google.gson.JsonElement;

@ToString(callSuper=true) public class RichAnnotation extends Annotation
{
	private static final long serialVersionUID = 887667543211247203L;

	@Getter @Setter protected String thumbUrl = "";
	@Getter @Setter protected int thumbWidth = 0, thumbHeight = 0;
	@Getter @Setter protected String url = "";
	@Getter @Setter protected String embeddableUrl = "";

	private void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.writeUTF(url);
		oos.writeUTF(thumbUrl);
		oos.writeInt(thumbWidth);
		oos.writeInt(thumbHeight);
		oos.writeUTF(embeddableUrl);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		url = ois.readUTF();
		thumbUrl = ois.readUTF();
		thumbWidth = ois.readInt();
		thumbHeight = ois.readInt();
		embeddableUrl = ois.readUTF();
	}

	@Override public String getAnnotationKey()
	{
		return "net.app.core.oembed";
	}

	@Override public JsonElement toAnnotation()
	{
		return null;
	}
}