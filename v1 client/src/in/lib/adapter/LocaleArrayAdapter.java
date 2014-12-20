package in.lib.adapter;

import in.rob.client.R;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LocaleArrayAdapter extends ArrayAdapter<Locale>
{
	static class LocaleHolder
	{
		TextView name;
	}

	private final LayoutInflater mLayoutInflater;
	private final Context mContext;
	private final List<Locale> mItems;
	private final int mLayoutResourceId;

	public LocaleArrayAdapter(Context context, List<Locale> locales)
	{
		super(context, R.layout.spinner_item, locales);
		mContext = context;
		mItems = locales;
		mLayoutResourceId = R.layout.spinner_item;
		mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		LocaleHolder holder = null;

		if (row == null)
		{
			row = mLayoutInflater.inflate(mLayoutResourceId, parent, false);

			holder = new LocaleHolder();
			holder.name = (TextView)row.findViewById(R.id.text1);

			row.setTag(holder);
		}
		else
		{
			holder = (LocaleHolder)row.getTag();
		}

		Locale locale = getItem(position);
		holder.name.setText(locale.getDisplayName());

		return row;
	}

	@Override public View getDropDownView(int position, View convertView, ViewGroup parent)
	{
		return getView(position, convertView, parent);
	}

	public String getItemCode(int position)
	{
		return getItem(position).toString();
	}

	@Override public int getPosition(Locale item)
	{
		String itemCode = item.toString().toLowerCase();
		for (int i = 0; i < mItems.size(); i++)
		{
			if (getItemCode(i).toLowerCase().equals(itemCode))
			{
				return i;
			}
		}

		return -1;
	}
}
