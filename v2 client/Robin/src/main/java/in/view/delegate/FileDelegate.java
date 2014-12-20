package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.data.annotation.FileAnnotation;
import in.rob.client.R;
import in.view.delegate.base.AdapterDelegate;
import in.view.holder.FileHolder;

public class FileDelegate extends AdapterDelegate<FileAnnotation>
{
	public FileDelegate(RobinAdapter<FileAnnotation> adapter)
	{
		super(adapter);
	}

	@Override public View getView(FileAnnotation item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		FileHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.browser_image, parent, false);
			holder = new FileHolder(convertView);

			convertView.setTag(holder);
		}
		else
		{
			holder = (FileHolder)convertView.getTag();
		}

		convertView.setTag(R.id.TAG_POSITION, position);
		holder.populate(item);

		return convertView;
	}
}
