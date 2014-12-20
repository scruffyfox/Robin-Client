package in.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import in.data.annotation.CrossPostAnnotation;
import in.data.annotation.ImageAnnotation;
import in.data.annotation.LocationAnnotation;
import in.data.annotation.VideoAnnotation;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import in.model.base.Model;
import lombok.Data;

@Data
public class AnnotationList extends Model
{
	protected List<ImageAnnotation> images = new ArrayList<ImageAnnotation>();
	protected List<VideoAnnotation> videos = new ArrayList<VideoAnnotation>();
	protected List<LocationAnnotation> locations = new ArrayList<LocationAnnotation>();
	protected List<CrossPostAnnotation> crossposts = new ArrayList<CrossPostAnnotation>();

	@Override public AnnotationList createFrom(JsonElement element)
	{
		return null;
	}

	@Override public AnnotationList createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public List<? extends Model> createListFrom(JsonElement element)
	{
		return null;
	}

	@Override public String getVersion()
	{
		return "87db65fe-2408-4b9a-bfca-3fe731cde3a1";
	}

	@Override public AnnotationList read(SerialReaderUtil util)
	{
		try
		{
			String version = util.readString();
			if (!version.equals(getVersion())) return null;

			this.images = util.readModelList(ImageAnnotation.class);
			this.videos = util.readModelList(VideoAnnotation.class);
			this.locations = util.readModelList(LocationAnnotation.class);
			this.crossposts = util.readModelList(CrossPostAnnotation.class);

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public void write(SerialWriterUtil util)
	{
		try
		{
			util.writeString(getVersion());
			util.writeModelList(images);
			util.writeModelList(videos);
			util.writeModelList(locations);
			util.writeModelList(crossposts);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public static final Parcelable.Creator<AnnotationList> CREATOR = new Creator<AnnotationList>()
	{
		@Override public AnnotationList[] newArray(int size)
		{
			return new AnnotationList[size];
		}

		@Override public AnnotationList createFromParcel(Parcel source)
		{
			return new AnnotationList().createFrom(source);
		}
	};
}
