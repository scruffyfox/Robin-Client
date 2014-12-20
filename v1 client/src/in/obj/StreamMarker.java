package in.obj;

import in.lib.Debug;

import java.io.Serializable;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.google.gson.JsonObject;

@ToString public class StreamMarker implements Serializable
{
	@Tag(0x01) @NonNull @Getter @Setter private String id = "";
	@Tag(0x02) @NonNull @Getter @Setter private String lastReadId = "";
	@Tag(0x03) @NonNull @Getter @Setter private String name = "";
	@Tag(0x04) @NonNull @Getter @Setter private String lastUpdatedStr = "";
	@Tag(0x05) @NonNull @Getter @Setter private long lastUpdated = 0L;
	@Tag(0x06) @NonNull @Getter @Setter private String version = "";

	public static StreamMarker fromObject(JsonObject meta)
	{
		try
		{
			StreamMarker marker = new StreamMarker();

			if (meta.has("marker"))
			{
				JsonObject markerObj = meta.get("marker").getAsJsonObject();

				if (markerObj.has("id"))
				{
					marker.setId(markerObj.get("id").getAsString());
				}

				if (markerObj.has("last_read_id"))
				{
					marker.setLastReadId(markerObj.get("last_read_id").getAsString());
				}

				if (markerObj.has("name"))
				{
					marker.setName(markerObj.get("name").getAsString());
				}

				if (markerObj.has("updated_at"))
				{
					marker.setLastUpdatedStr(markerObj.get("updated_at").getAsString());
				}

				if (markerObj.has("version"))
				{
					marker.setVersion(markerObj.get("version").getAsString());
				}
			}

			return marker;
		}
		catch (Exception e)
		{
			Debug.out(e);
			return new StreamMarker();
		}
	}
}