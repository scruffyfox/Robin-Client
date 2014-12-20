package in.obj.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
public class LinkEntity extends Entity
{
	@Getter @Setter private int pos;
	@Getter @Setter private int len;
	@Getter @Setter private int amendedLen = -1;
	@Getter @Setter private String url;
}