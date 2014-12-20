package in.obj.entity;

import lombok.Getter;
import lombok.Setter;

public class MentionEntity extends Entity
{
	@Getter @Setter private String id = "";
	@Getter @Setter private String name = "";

	public MentionEntity()
	{

	}

	public MentionEntity(String id, String name)
	{
		this.id = id;
		this.name = name;
	}
}
