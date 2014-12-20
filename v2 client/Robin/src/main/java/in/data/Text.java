package in.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import in.data.entity.BoldStyledEntity;
import in.data.entity.HashEntity;
import in.data.entity.ItalicStyledEntity;
import in.data.entity.LinkEntity;
import in.data.entity.MentionEntity;
import in.data.entity.StyledEntity;
import in.data.entity.UnderlineStyledEntity;
import in.lib.Constants;
import in.lib.manager.SettingsManager;
import in.lib.utils.BitUtils;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.model.base.Model;
import lombok.Data;

@Data
public class Text extends Model
{
	private String text;
	private List<MentionEntity> mentions;
	private List<LinkEntity> links;
	private List<HashEntity> hashTags;
	private List<? extends StyledEntity> emphasisStyles;

	@Override public Text createFrom(JsonElement element)
	{
		try
		{
			JsonObject textObject = element.getAsJsonObject();

			if (textObject.has("description"))
			{
				textObject = textObject.get("description").getAsJsonObject();
			}

			this.text = textObject.get("text").getAsString().trim();

			if (BitUtils.contains(SettingsManager.getInstance().getEmphasisBit(), Constants.BIT_EMPHASIS_ITALIC, Constants.BIT_EMPHASIS_BOLD, Constants.BIT_EMPHASIS_UNDERLINE))
			{
				this.emphasisStyles = new ArrayList<StyledEntity>();

				if (BitUtils.contains(SettingsManager.getInstance().getEmphasisBit(), Constants.BIT_EMPHASIS_ITALIC))
				{
					List italics = new ItalicStyledEntity().createListFrom(this.text);

					if (italics != null)
					{
						emphasisStyles.addAll(italics);
					}
				}

				if (BitUtils.contains(SettingsManager.getInstance().getEmphasisBit(), Constants.BIT_EMPHASIS_BOLD))
				{
					List bolds = new BoldStyledEntity().createListFrom(this.text);

					if (bolds != null)
					{
						emphasisStyles.addAll(bolds);
					}
				}

				if (BitUtils.contains(SettingsManager.getInstance().getEmphasisBit(), Constants.BIT_EMPHASIS_UNDERLINE))
				{
					List underlines = new UnderlineStyledEntity().createListFrom(this.text);

					if (underlines != null)
					{
						emphasisStyles.addAll(underlines);
					}
				}
			}

			JsonObject entityObject = textObject.get("entities").getAsJsonObject();
			this.mentions = new MentionEntity().createListFrom(entityObject.get("mentions"));
			this.links = new LinkEntity().createListFrom(entityObject.get("links"));
			this.hashTags = new HashEntity().createListFrom(entityObject.get("hashtags"));

			return this;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		return null;
	}

	@Override public String getVersion()
	{
		return "74949507-19b5-4e46-a832-50766fc63125";
	}

	@Override public Model read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			text = util.readString();
			mentions = util.readModelList(MentionEntity.class);
			links = util.readModelList(LinkEntity.class);
			hashTags = util.readModelList(HashEntity.class);
			emphasisStyles = util.readModelList(StyledEntity.class);

			return this;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override public void write(SerialWriterUtil util)
	{
		try
		{
			util.writeString(getVersion());
			util.writeString(text);
			util.writeModelList(mentions);
			util.writeModelList(links);
			util.writeModelList(hashTags);
			util.writeModelList(emphasisStyles);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override public Text createFrom(Parcel parcel)
	{
		return ((Text)super.createFrom(parcel));
	}

	public static final Parcelable.Creator<Text> CREATOR = new Creator<Text>()
	{
		@Override public Text[] newArray(int size)
		{
			return new Text[size];
		}

		@Override public Text createFromParcel(Parcel source)
		{
			return new Text().createFrom(source);
		}
	};
}
