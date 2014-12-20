package in.model;


import in.model.base.NetObject;
import in.obj.StreamMarker;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

@ToString public class Stream
{
	/**
	 * Min ID is used with before_id IE last id
	 */
	@Tag(0x01) @Getter @Setter private String minId = "";

	/**
	 * Max ID is used with since_id
	 */
	@Tag(0x02) @Getter @Setter private String maxId = "";
	@Tag(0x03) @Getter @Setter private List<NetObject> objects = new ArrayList<NetObject>();
	@Tag(0x04) @Getter @Setter private int breakPosition = -2;
	@Tag(0x05) @Getter @Setter private StreamMarker marker = new StreamMarker();
	@Tag(0x06) @Getter @Setter private Boolean hasMore = true;

//	public byte[] serialize()
//	{
//		/*String filename = "" + hashCode();
//		byte[] val = filename.getBytes();
//		CacheManager.getInstance().writeFile(filename, this);
//		return val;*/
//		return CacheManager.getInstance().serialize(this);
//	}
//
//	public static Stream deserialize(byte[] data)
//	{
//		/*String file = new String(data);
//		Stream s = CacheManager.getInstance().readFileAsObject(file, Stream.class);
//		CacheManager.getInstance().removeFile(file);
//		return s;*/
//		return CacheManager.getInstance().deserialize(data, Stream.class);
//	}
}