package in.model;

import in.lib.manager.CacheManager;
import in.model.base.NetObject;
import in.obj.annotation.Annotation;
import in.obj.entity.Entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * This will be the holder class we use when creating a new post
 */
@ToString() public class DraftPost extends NetObject
{
	/**
	 * The main text of the post
	 */
	@Tag(0x01) @Getter @Setter private String postText = "";

	/**
	 * The reply id of the text
	 */
	@Tag(0x02) @Getter @Setter private String replyId = "";

	/**
	 * The channel id
	 */
	@Tag(0x03) @Getter @Setter private String channelId = "";

	/**
	 * The repost id of the text
	 */
	@Tag(0x04) @Getter @Setter private String repostId = "";

	/**
	 * The path to the attached image
	 */
	@Tag(0x05) @Getter @Setter private String imagePath;

	/**
	 * The selected account to post from
	 */
	@Tag(0x06) @Getter @Setter private String selectedAccountId = "";

	/**
	 * Date of the draft
	 */
	@Tag(0x07) @Getter @Setter private long date = 0l;

	/**
	 * List of annotations to post
	 */
	@Tag(0x08) @Getter @Setter private List<Annotation> annotations = new ArrayList<Annotation>();

	/**
	 * List of entities to post
	 */
	@Tag(0x09) @Getter @Setter private LinkedHashMap<Entity.Type, ArrayList<Entity>> entities = new LinkedHashMap<Entity.Type, ArrayList<Entity>>();

	public DraftPost()
	{
		this.date = System.currentTimeMillis();
	}

	public static DraftPost deserialize(byte[] data)
	{
		return CacheManager.getInstance().deserialize(data, DraftPost.class);
	}
}