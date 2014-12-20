package in.obj.annotation;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.google.gson.JsonElement;

@ToString public class LinkAnnotation extends Annotation
{
	@Getter @Setter private String url;

	@Override public String getAnnotationKey()
	{
		return null;
	}

	@Override public JsonElement toAnnotation()
	{
		return null;
	}
}