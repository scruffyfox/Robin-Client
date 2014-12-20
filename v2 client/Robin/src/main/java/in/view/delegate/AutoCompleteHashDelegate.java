package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import in.data.entity.HashEntity;
import in.model.AdnModel;
import in.rob.client.R;
import in.view.delegate.base.AdapterDelegate;
import in.view.holder.HashHolder;

public class AutoCompleteHashDelegate extends AdapterDelegate
{
	public AutoCompleteHashDelegate(BaseAdapter adapter)
	{
		super(null);
	}

	@Override public View getView(AdnModel item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		if (item instanceof HashEntity)
		{
			HashHolder holder;

			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.popup_hash_view, parent, false);
				holder = new HashHolder(convertView);
				convertView.setTag(holder);
			}
			else
			{
				holder = (HashHolder)convertView.getTag();
			}

			holder.populate((HashEntity)item);
			convertView.setTag(R.id.TAG_POSITION, position);
		}

		return convertView;
	}
}
