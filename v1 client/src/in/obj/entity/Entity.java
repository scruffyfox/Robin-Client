package in.obj.entity;

import java.io.Serializable;

import lombok.Getter;

public class Entity implements Serializable
{
	public static enum Type
	{
		LINK("link");

		@Getter private final String key;
		private Type(String key)
		{
			this.key = key;
		}
	}
}