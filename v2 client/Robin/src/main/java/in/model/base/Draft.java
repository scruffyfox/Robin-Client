package in.model.base;

import android.os.Parcel;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import in.data.annotation.Annotation;
import in.data.entity.Entity;
import in.data.entity.LinkEntity;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.model.AdnModel;
import lombok.Data;

@Data
public abstract class Draft extends AdnModel
{
	protected String selectedAccountId;
	protected String postText;
	protected List<String> images;
	protected int imageCount = 0;
	protected List<Annotation> annotations;
	protected List<LinkEntity> linkEntities;
	protected long date = 0l;

	public Draft()
	{
		this.date = System.currentTimeMillis();
		this.images = new ArrayList<String>();
		this.annotations = new ArrayList<Annotation>();
		this.linkEntities = new ArrayList<LinkEntity>();
	}

	public abstract void save();
	public abstract void delete();
	public abstract Draft load(String id);

	@Override public Model createFrom(JsonElement element)
	{
		// Not used
		return null;
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		// Not used
		return null;
	}

	@Override public String getVersion()
	{
		return "7dc55add-16ff-4a90-886e-7426e52db462";
	}

	@Override public Draft read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				selectedAccountId = util.readString();
				postText = util.readString();
				images = util.readStringList();
				imageCount = util.readInt();
				annotations = util.readModelList(Annotation.class);
				linkEntities = util.readModelList(LinkEntity.class);
				date = util.readLong();

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public void write(SerialWriterUtil util)
	{
		super.write(util);

		try
		{
			util.writeString(getVersion());
			util.writeString(selectedAccountId);
			util.writeString(postText);
			util.writeStringList(images);
			util.writeInt(imageCount);
			util.writeModelList(annotations);
			util.writeModelList(linkEntities);
			util.writeLong(date);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public Draft createFrom(Parcel object)
	{
		return (Draft)super.createFrom(object);
	}
}