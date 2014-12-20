package in.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.data.AnnotationList;
import in.data.Text;
import in.data.annotation.CheckinAnnotation;
import in.data.annotation.CrossPostAnnotation;
import in.data.annotation.ImageAnnotation;
import in.data.annotation.LocationAnnotation;
import in.data.annotation.VideoAnnotation;
import in.data.entity.LinkEntity;
import in.data.entity.MentionEntity;
import in.lib.manager.UserManager;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.lib.utils.URLUtils;
import lombok.Data;

@Data
public class Message extends AdnModel
{
	protected Text postText;
	protected User poster;
	protected long date;
	protected String dateStr;
	protected String canonicalUrl;
	protected String timeZone;
	protected String clientName;
	protected String clientLink;
	protected String replyTo;
	protected boolean machinePost;
	protected boolean newPost = false;
	protected boolean deleted = false;
	protected AnnotationList annotations;

	public boolean isMention()
	{
		return isMention(UserManager.getInstance().getUser().getId());
	}

	public boolean isMention(String userId)
	{
		if (postText.getMentions() != null)
		{
			for (MentionEntity mention : postText.getMentions())
			{
				if (mention.getId().equals(userId))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override public Message createFrom(JsonElement element)
	{
		try
		{
			JsonObject postObject = element.getAsJsonObject();

			this.id = postObject.get("id").getAsString();
			this.poster = new User().createFrom(postObject.get("user"));

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			Date postDate = format.parse(postObject.get("created_at").getAsString());
			this.date = postDate.getTime();
			this.dateStr = new SimpleDateFormat().format(postDate);
			this.timeZone = postObject.get("user").getAsJsonObject().get("timezone").getAsString();
			this.clientName = postObject.get("source").getAsJsonObject().get("name").getAsString();
			this.clientLink = postObject.get("source").getAsJsonObject().get("link").getAsString();

			if (postObject.has("canonical_url"))
			{
				this.canonicalUrl = postObject.get("canonical_url").getAsString();
			}

			if (postObject.has("reply_to"))
			{
				this.replyTo = postObject.get("reply_to").getAsString();
			}

			if (postObject.has("is_deleted"))
			{
				this.deleted = postObject.get("is_deleted").getAsBoolean();
			}

			if (!deleted)
			{
				this.postText = new Text().createFrom(postObject);
			}

			if (postObject.has("annotations"))
			{
				JsonArray annotationArray = postObject.get("annotations").getAsJsonArray();
				this.annotations = parseAnnotations(annotationArray);
			}

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public List<? extends Message> createListFrom(JsonElement element)
	{
		try
		{
			JsonArray postArray = element.getAsJsonArray();
			ArrayList<Message> posts = new ArrayList<Message>(postArray.size());

			for (JsonElement postElement : postArray)
			{
				Message post = new Message().createFrom(postElement);

				if (post != null)
				{
					posts.add(post);
				}
			}

			return posts;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public Message createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	public AnnotationList parseAnnotations(JsonArray annotationArray)
	{
		ArrayList<String> tmpImageList = new ArrayList<String>();

		AnnotationList map = new AnnotationList();

		if (annotationArray.size() > 0)
		{
			for (JsonElement annotationElement : annotationArray)
			{
				JsonObject annotationObject = annotationElement.getAsJsonObject();
				JsonObject value = annotationObject.get("value").getAsJsonObject();
				String type = annotationObject.get("type").getAsString();

				if (type.equals("net.app.core.oembed") && value.has("type"))
				{
					if (value.get("type").getAsString().equalsIgnoreCase("photo"))
					{
						ImageAnnotation image = new ImageAnnotation().createFrom(value);
						if (image != null && !map.getImages().contains(image))
						{
							map.getImages().add(image);
							tmpImageList.add(image.getUrl());
						}
					}
					else if (value.get("type").getAsString().equalsIgnoreCase("video"))
					{
						VideoAnnotation video = new VideoAnnotation().createFrom(value);
						if (video != null && !map.getVideos().contains(video))
						{
							map.getVideos().add(video);
						}
					}
				}
				else if (type.equals("net.app.core.geolocation"))
				{
					LocationAnnotation location = new LocationAnnotation().createFrom(value);

					if (location != null && !map.getLocations().contains(location))
					{
						map.getLocations().add(location);
					}
				}
				else if (type.equals("net.app.core.checkin"))
				{
					CheckinAnnotation location = new CheckinAnnotation().createFrom(value);

					if (location != null && !map.getLocations().contains(location))
					{
						map.getLocations().add(location);
					}
				}
				else if (type.equals("net.app.core.crosspost"))
				{
					CrossPostAnnotation crosspost = new CrossPostAnnotation().createFrom(value);

					if (crosspost != null && !map.getCrossposts().contains(crosspost))
					{
						map.getCrossposts().add(crosspost);
					}
				}
			}
		}

		if (postText != null && postText.getLinks().size() > 0)
		{
			for (LinkEntity link : postText.getLinks())
			{
				Uri uri = Uri.parse(link.getUrl());

				if (URLUtils.isImage(uri) && !tmpImageList.contains(link.getUrl()))
				{
					ImageAnnotation image = new ImageAnnotation();
					image.setUrl(link.getUrl());
					image.setTextUrl(link.getUrl());
					image.setEmbeddableUrl(link.getUrl());

					tmpImageList.add(link.getUrl());

					if (!map.getImages().contains(image))
					{
						map.getImages().add(image);
					}
				}
			}
		}

		return map;
	}

	@Override public String getVersion()
	{
		return "44f3c4c6-00a0-4a1e-b144-7d4584ffe031";
	}

	@Override public void write(SerialWriterUtil util)
	{
		super.write(util);

		try
		{
			util.writeString(getVersion());
			util.writeModel(postText);
			util.writeModel(poster);
			util.writeLong(date);
			util.writeString(dateStr);
			util.writeString(canonicalUrl);
			util.writeString(timeZone);
			util.writeString(clientName);
			util.writeString(clientLink);
			util.writeString(replyTo);
			util.writeBoolean(machinePost);
			util.writeBoolean(newPost);
			util.writeBoolean(deleted);
			util.writeModel(annotations);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public Message read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				postText = util.readModel(Text.class);
				poster = util.readModel(User.class);
				date = util.readLong();
				dateStr = util.readString();
				canonicalUrl = util.readString();
				timeZone = util.readString();
				clientName = util.readString();
				clientLink = util.readString();
				replyTo = util.readString();
				machinePost = util.readBoolean();
				newPost = util.readBoolean();
				deleted = util.readBoolean();
				annotations = util.readModel(AnnotationList.class);

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
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
}
