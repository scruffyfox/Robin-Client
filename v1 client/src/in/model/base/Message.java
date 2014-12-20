package in.model.base;

import in.lib.Debug;
import in.lib.Regex;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.URLUtils;
import in.model.User;
import in.obj.annotation.Annotation;
import in.obj.annotation.Annotation.Type;
import in.obj.annotation.CrosspostAnnotation;
import in.obj.annotation.ImageAnnotation;
import in.obj.annotation.LinkAnnotation;
import in.obj.annotation.LocationAnnotation;
import in.obj.annotation.RichAnnotation;
import in.obj.annotation.VideoAnnotation;
import in.obj.entity.LinkEntity;
import in.obj.entity.MentionEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@ToString(includeFieldNames = true, callSuper = true)
public class Message extends NetObject
{
	@Tag(0x01) @Getter @Setter private String formattedText;
	@Tag(0x10) @Getter @Setter private String originalText;
	@Tag(0x02) @Getter @Setter private User poster;
	@Tag(0x03) @Getter @Setter private boolean mention;
	@Tag(0x04) @Getter @Setter private String canonicalUrl;
	@Tag(0x05) @Getter @Setter private String timeZone;
	@Tag(0x06) @Getter @Setter private String clientName;
	@Tag(0x07) @Getter @Setter private String clientLink;
	@Tag(0x08) @Getter @Setter private String replyTo;
	@Tag(0x09) @Getter @Setter private long date;
	@Tag(0x0A) @Getter @Setter private String dateStr;
	@Tag(0x0B) @Getter @Setter private HashMap<Type, ArrayList<Annotation>> annotations;
	@Tag(0x0C) @Getter @Setter private String[] hashTags = {};
	@Tag(0x0D) @Getter @Setter private LinkEntity[] links = {};
	@Tag(0x0E) @Getter @Setter private MentionEntity[] mentions = {};
	@Tag(0x0F) @Getter @Setter private boolean machinePost;

	/**
	 * Set this to true if it is a faux post added to the timeline. Check
	 * for this when finding the first post in the list's ID to prevent
	 * timeline shifting
	 */
	@Tag(0x10) @Getter @Setter private boolean newPost = false;

	@Override public Message createFrom(JsonObject post)
	{
		return createFrom(post, UserManager.getUser(), false);
	}

	public Message createFrom(JsonObject post, boolean allowDeleted)
	{
		return createFrom(post, UserManager.getUser(), allowDeleted);
	}

	/**
	 * Parses the return API object into a post class using the specified user
	 * @param p The message object to populate
	 * @param user The jsonobject post from the API
	 * @param acc The user to use when parsing the object
	 * @return The new Post object
	 */
	public Message createFrom(JsonObject post, User acc, boolean allowDeleted)
	{
		if (post.get("is_deleted") != null && post.get("is_deleted").getAsBoolean() && !allowDeleted)
		{
			return null;
		}

		try
		{
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date postDate = format.parse(post.get("created_at").getAsString());
			long date = postDate.getTime();

			setId(post.get("id").getAsString());
			setDate(date);
			setDateStr(calculateDateString());

			if (post.has("html"))
			{
				String html = post.get("html").getAsString()
						.replaceAll("\\s{2}", "&nbsp;&nbsp;")
						.replaceAll("\\t+", "&nbsp;&nbsp;&nbsp;&nbsp;")
						.replaceAll("(<br>(\\s)?){3,}", "<br />")
						.replaceAll("(?:\\<br\\s*/?\\>)+\\</span\\>*$", "</span>");

				if (SettingsManager.isBoldEnabled())
				{
					Matcher asterix = Regex.MATCH_ASTERIX.matcher(html);
					while (asterix.find())
					{
						String group = asterix.group();
						int start = asterix.start();
						int end = asterix.end();

						String begin = html.substring(0, start);
						String finish = html.substring(end, html.length());
						html = begin + "<b>" + group.substring(2, group.length() - 2) + "</b>" + finish;
						asterix = Regex.MATCH_ASTERIX.matcher(html);
					}
				}

				if (SettingsManager.isItalicEnabled())
				{
					Matcher slash = Regex.MATCH_SLASH.matcher(html);
					while (slash.find())
					{
						String group = slash.group();
						int start = slash.start();
						int end = slash.end();

						String begin = html.substring(0, start);
						String finish = html.substring(end, html.length());
						html = begin + "<i>" + group.substring(1, group.length() - 1) + "</i>" + finish;
						slash = Regex.MATCH_SLASH.matcher(html);
					}
				}

				if (SettingsManager.isUnderlineEnabled())
				{
					Matcher underscore = Regex.MATCH_UNDERSCORE.matcher(html);
					while (underscore.find())
					{
						String group = underscore.group();

						int start = underscore.start();
						int end = underscore.end();

						String begin = html.substring(0, start);
						String finish = html.substring(end, html.length());
						html = begin + "<u>" + group.substring(1, group.length() - 1) + "</u>" + finish;
						underscore = Regex.MATCH_UNDERSCORE.matcher(html);
					}
				}

				setFormattedText(html);
			}

			if (post.has("text"))
			{
				setOriginalText(post.get("text").getAsString());
			}

			setTimeZone(post.get("user").getAsJsonObject().get("timezone").getAsString());
			setClientName(post.get("source").getAsJsonObject().get("name").getAsString());
			setClientLink(post.get("source").getAsJsonObject().get("link").getAsString());

			User poster = new User().createFrom(post.get("user").getAsJsonObject());
			setPoster(poster);

			if (post.has("reply_to"))
			{
				setReplyTo(post.get("reply_to").getAsString());
			}

			JsonArray mentions = post.get("entities").getAsJsonObject().get("mentions").getAsJsonArray();
			if (mentions.size() > 0)
			{
				setMentions(parseMentions(mentions, acc));

				if (mentions != null)
				{
					for (MentionEntity men : getMentions())
					{
						if (men.getId().equals(acc.getId()))
						{
							setMention(true);
							break;
						}
					}
				}
			}

			JsonArray links = post.get("entities").getAsJsonObject().get("links").getAsJsonArray();
			if (links.size() > 0)
			{
				setLinks(parseLinks(links));
			}

			JsonArray hashTags = post.get("entities").getAsJsonObject().get("hashtags").getAsJsonArray();
			if (hashTags.size() > 0)
			{
				setHashTags(parseHashTags(hashTags));
			}

			if (post.has("annotations"))
			{
				JsonArray annotations = post.get("annotations").getAsJsonArray();

				if (annotations.size() > 0 || this.links.length > 0)
				{
					setAnnotations(new LinkedHashMap<Annotation.Type, ArrayList<Annotation>>());
					setAnnotations(parseAnnotations(annotations, links));
				}
			}

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
			return null;
		}
	}

	@Override public Message createFrom(Parcel object)
	{
		setFormattedText(object.readString());
		setOriginalText(object.readString());
		setPoster((User)object.readParcelable(User.class.getClassLoader()));
		setMention((Boolean)object.readValue(null));
		setCanonicalUrl(object.readString());
		setTimeZone(object.readString());
		setClientName(object.readString());
		setClientLink(object.readString());
		setReplyTo(object.readString());
		setDate(object.readLong());
		setDateStr(object.readString());
		setAnnotations((HashMap<Annotation.Type, ArrayList<Annotation>>)object.readSerializable());

		String[] tags = new String[object.readInt()];
		object.readStringArray(tags);
		setHashTags(tags);

		setLinks((LinkEntity[])object.readSerializable());
		setMentions((MentionEntity[])object.readSerializable());
		setMachinePost((Boolean)object.readValue(null));
		super.createFrom(object);
		return this;
	}

	/**
	 * Goes through a json array of mentions and adds them to the list
	 * @param mentions The mentions from the API
	 * @return An ArrayList of mentions
	 */
	public MentionEntity[] parseMentions(JsonArray mentions, User acc)
	{
		MentionEntity[] mens = new MentionEntity[mentions.size()];
		int index = 0;
		for (JsonElement mention : mentions)
		{
			mens[index++] = new MentionEntity(mention.getAsJsonObject().get("id").getAsString(), mention.getAsJsonObject().get("name").getAsString());
		}

		return mens;
	}

	/**
	 * Processes the links entity object into class struct list
	 * @param links
	 * @return
	 */
	public LinkEntity[] parseLinks(JsonArray links)
	{
		LinkEntity[] linksArr = new LinkEntity[links.size()];
		int index = 0;
		for (JsonElement e : links)
		{
			LinkEntity link = new LinkEntity();
			link.setLen(e.getAsJsonObject().get("len").getAsInt());
			link.setPos(e.getAsJsonObject().get("pos").getAsInt());
			link.setUrl(e.getAsJsonObject().get("url").getAsString());

			if (e.getAsJsonObject().has("amended_len"))
			{
				link.setAmendedLen(e.getAsJsonObject().get("amended_len").getAsInt());
			}

			linksArr[index++] = link;
		}

		return linksArr;
	}

	/**
	 * Goes through a json array of tags and adds them to the list
	 * @param hashTags The tags from the API
	 * @return An ArrayList of tags
	 */
	public String[] parseHashTags(JsonArray hashTags)
	{
		String[] tags = new String[hashTags.size()];
		int index = 0;
		for (JsonElement tag : hashTags)
		{
			tags[index++] = tag.getAsJsonObject().get("name").getAsString().toLowerCase();
		}

		return tags;
	}

	/**
	 * Goes through annotations and embedded links in a post. Adds them to the relevant type in the HashMap
	 * Any duplicate images found encoded in the post and annotation will be skipped.
	 *
	 * @param annotations The annotation jsonArray
	 * @param jLinks The links jsonArray
	 * @return The finished hashmap
	 */
	public HashMap<Type, ArrayList<Annotation>> parseAnnotations(JsonArray annotations, JsonArray jLinks)
	{
		HashMap<Type, ArrayList<Annotation>> ann = new  HashMap<Type, ArrayList<Annotation>>();
		ArrayList<Annotation> locs = new ArrayList<Annotation>();
		ArrayList<Annotation> images = new ArrayList<Annotation>();
		ArrayList<Annotation> videos = new ArrayList<Annotation>();
		ArrayList<Annotation> rich = new ArrayList<Annotation>();
		ArrayList<Annotation> links = new ArrayList<Annotation>();
		ArrayList<Annotation> crossPost = new ArrayList<Annotation>();
		ArrayList<Annotation> inOrder = new ArrayList<Annotation>();

		ArrayList<String> tmpImageList = new ArrayList<String>();

		for (JsonElement annotation : annotations)
		{
			JsonObject a = annotation.getAsJsonObject();
			JsonObject value = a.get("value").getAsJsonObject();
			String type = a.get("type").getAsString();

			if (type.equals("net.app.core.geolocation"))
			{
				LocationAnnotation location = new LocationAnnotation();
				location.setLat(value.get("latitude").getAsDouble());
				location.setLng(value.get("longitude").getAsDouble());
				locs.add(location);
			}
			else if (type.equals("net.app.core.crosspost"))
			{
				if (value.has("canonical_url"))
				{
					CrosspostAnnotation cross = new CrosspostAnnotation();
					cross.setUrl(value.get("canonical_url").getAsString());

					crossPost.add(cross);
				}
			}
			else if (type.equals("net.app.core.oembed"))
			{
				if (value.has("type"))
				{
					if (value.get("type").getAsString().equalsIgnoreCase("video"))
					{
						VideoAnnotation image = new VideoAnnotation();

						if (!value.has("thumbnail_url")) continue;

						image.setPreviewUrl(value.get("thumbnail_url").getAsString());
						image.setUrl(value.get("thumbnail_url").getAsString());
						image.setTextUrl(value.get("thumbnail_url").getAsString());
						image.setWidth(value.get("width").getAsInt());
						image.setHeight(value.get("height").getAsInt());

						if (value.has("embeddable_url"))
						{
							image.setEmbeddableUrl(value.get("embeddable_url").getAsString());
							image.setUrl(value.get("embeddable_url").getAsString());
							image.setTextUrl(value.get("embeddable_url").getAsString());
							tmpImageList.add(image.getEmbeddableUrl());
						}
						else
						{
							tmpImageList.add(image.getUrl());
						}

						videos.add(image);
						inOrder.add(image);
					}
					else if (value.get("type").getAsString().equalsIgnoreCase("photo"))
					{
						ImageAnnotation image = new ImageAnnotation();
						image.setUrl(value.get("url").getAsString());
						image.setTextUrl(value.get("url").getAsString());
						image.setWidth(value.get("width").getAsInt());
						image.setHeight(value.get("height").getAsInt());

						if (value.has("thumbnail_large_url_immediate"))
						{
							image.setThumbUrl(value.get("thumbnail_large_url_immediate").getAsString());
							image.setPreviewUrl(image.getThumbUrl());
						}
						else if (value.has("thumbnail_url"))
						{
							image.setThumbUrl(value.get("thumbnail_url").getAsString());
							image.setPreviewUrl(image.getThumbUrl());
						}

						if (value.has("embeddable_url"))
						{
							image.setEmbeddableUrl(value.get("embeddable_url").getAsString());
						}

						tmpImageList.add(image.getUrl());

						images.add(image);
						inOrder.add(image);
					}
					else if (value.get("type").getAsString().equalsIgnoreCase("rich"))
					{
						RichAnnotation richAnnotation = new RichAnnotation();
						richAnnotation.setUrl(value.get("url").getAsString());

						if (value.has("thumbnail_large_url_immediate"))
						{
							richAnnotation.setThumbUrl(value.get("thumbnail_large_url_immediate").getAsString());
							richAnnotation.setPreviewUrl(richAnnotation.getThumbUrl());
						}
						else if (value.has("thumbnail_url"))
						{
							richAnnotation.setThumbUrl(value.get("thumbnail_url").getAsString());
							richAnnotation.setPreviewUrl(richAnnotation.getThumbUrl());
						}

						if (value.has("embeddable_url"))
						{
							richAnnotation.setUrl(value.get("embeddable_url").getAsString());
							richAnnotation.setEmbeddableUrl(value.get("embeddable_url").getAsString());
						}

						tmpImageList.add(richAnnotation.getUrl());
						tmpImageList.add(richAnnotation.getEmbeddableUrl());

						rich.add(richAnnotation);
						inOrder.add(richAnnotation);
					}
				}
			}
		}

		for (JsonElement link : jLinks)
		{
			JsonObject l = link.getAsJsonObject();
			String stringUrl = l.get("url").getAsString();
			Uri uri = Uri.parse(stringUrl);

			if (URLUtils.isImage(uri) && !tmpImageList.contains(stringUrl))
			{
				ImageAnnotation image = new ImageAnnotation();
				image.setUrl(stringUrl);
				image.setTextUrl(stringUrl);
				image.setEmbeddableUrl(stringUrl);

				tmpImageList.add(stringUrl);
				images.add(image);
				inOrder.add(image);
			}
			else if (URLUtils.isYoutubeVideo(uri))
			{
				VideoAnnotation image = new VideoAnnotation();
				image.setUrl(stringUrl);
				image.setTextUrl(stringUrl);

				videos.add(image);
				inOrder.add(image);
			}
//			else if (URLUtils.isMap(uri))
//			{
//				LocationAnnotation location = new LocationAnnotation();
//
//				locs.add(new OldAnnotation(stringUrl, Type.LOCATION));
//			}
			else
			{
				LinkAnnotation linkAnnotation = new LinkAnnotation();
				linkAnnotation.setUrl(stringUrl);
				links.add(linkAnnotation);
			}
		}

		ann.put(Type.IMAGE, images);
		ann.put(Type.VIDEO, videos);
		ann.put(Type.RICH, rich);
		ann.put(Type.LOCATION, locs);
		ann.put(Type.LINK, links);
		ann.put(Type.CROSS_POST, crossPost);
		ann.put(Type.IN_ORDER, inOrder);

		return ann;
	}

	public String calculateDateString()
	{
		if (SettingsManager.getShowLongDates())
		{
			GregorianCalendar cal = new GregorianCalendar();
			GregorianCalendar todayDate = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
			long startDate = todayDate.getTimeInMillis();

			String time = "";

			if (getDate() > startDate)
			{
				time = SettingsManager.getTimeFormat().format(new Date(getDate()));
			}
			else
			{
				time = SettingsManager.getDateFormat().format(new Date(getDate()));
			}

			return time;
		}
		else
		{
			return SettingsManager.getDateUtils().timeAgo(getDate());
		}
	}

	/**
	 * Checks if the post has a hash tag which has been muted
	 * @return True if it is muted, false otherwise
	 */
	public boolean isMuted()
	{
		if (this.hashTags == null) return false;

		for (String tag : this.hashTags)
		{
			if (SettingsManager.isTagMuted(tag))
			{
				return true;
			}
		}

		return false;
	}

	@Override public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getFormattedText());
		dest.writeString(getOriginalText());
		dest.writeParcelable(getPoster(), 0);
		dest.writeValue(isMention());
		dest.writeString(getCanonicalUrl());
		dest.writeString(getTimeZone());
		dest.writeString(getClientName());
		dest.writeString(getClientLink());
		dest.writeString(getReplyTo());
		dest.writeLong(getDate());
		dest.writeString(getDateStr());
		dest.writeSerializable(getAnnotations());
		dest.writeInt(getHashTags().length);
		dest.writeStringArray(getHashTags());
		dest.writeSerializable(getLinks());
		dest.writeSerializable(getMentions());
		dest.writeValue(isMachinePost());
		super.writeToParcel(dest, flags);
	}

	public static final Parcelable.Creator<Message> CREATOR = new Creator<Message>()
	{
		@Override public Message[] newArray(int size)
		{
			return new Message[size];
		}

		@Override public Message createFromParcel(Parcel source)
		{
			return new Message().createFrom(source);
		}
	};

	public static Message deserialize(byte[] data)
	{
		return CacheManager.getInstance().deserialize(data, Message.class);
	}
}