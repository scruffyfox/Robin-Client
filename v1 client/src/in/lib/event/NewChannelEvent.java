package in.lib.event;

import in.model.Channel;
import lombok.Getter;

public class NewChannelEvent
{
	@Getter private Channel channel;

	public NewChannelEvent(Channel c)
	{
		this.channel = c;
	}
}