package in.lib.handler.base;

import in.lib.Debug;
import in.model.base.NetObject;
import in.obj.StreamMarker;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import android.content.Context;

public abstract class WidgetResponseHandler extends RobinResponseHandler
{
	@Getter private List<NetObject> objects = new ArrayList<NetObject>();
	@Getter @Setter private StreamMarker marker;
	@Getter @Setter private Boolean hasMore;
	@Getter @Setter private String lastId = "";
	@Getter @Setter private String firstId = "";
	@Getter private String cacheFileName;

	public WidgetResponseHandler(Context c, String cacheFileName)
	{
		super(c);
		this.cacheFileName = cacheFileName;
	}

	@Override public void onFinish(boolean failed)
	{
		if (failed)
		{
			Debug.out(getConnectionInfo());
		}
	}

	public void setFinishedLoading(boolean append)
	{

	}
}