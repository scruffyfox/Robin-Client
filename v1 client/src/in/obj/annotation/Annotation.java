package in.obj.annotation;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.google.gson.JsonElement;

@ToString public abstract class Annotation implements Serializable
{
	public static enum Type
	{
		LINK,
		IMAGE,
		VIDEO,
		RICH,
		LOCATION,
		CROSS_POST,
		IN_ORDER,
		NONE;
	}

	@Getter @Setter private String previewUrl;

	public abstract JsonElement toAnnotation();
	public abstract String getAnnotationKey();
}