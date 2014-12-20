package in.obj;

import in.lib.manager.ImageAPIManager;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

@ToString() public class Auth
{
	@Tag(0x01) @Getter @Setter private String accessToken = "";
	@Tag(0x02) @Getter @Setter private HashMap<ImageAPIManager.Provider, String> imageDelegateToken = new HashMap<ImageAPIManager.Provider, String>();
}