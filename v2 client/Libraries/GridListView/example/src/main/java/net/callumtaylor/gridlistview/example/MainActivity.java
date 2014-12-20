package net.callumtaylor.gridlistview.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.callumtaylor.gridlistview.GridAdapter;
import net.callumtaylor.gridlistview.GridListView;

public class MainActivity extends Activity
{
	private String[] items = new String[]
	{
		"1",
		"2",
		"3",
		"4",
		"5",
		"6",
		"7",
		"8",
		"9",
		"10",
		"11",
		"12",
		"13",
		"14",
		"15",
		"16",
		"17",
		"18",
		"19",
		"20"
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		GridListView list = (GridListView)findViewById(R.id.list_view);

		GridAdapter adapter = new GridAdapter()
		{
			@Override public int getCount()
			{
				return items.length;
			}

			@Override public String getItem(int position)
			{
				return items[position];
			}

			@Override public long getItemId(int position)
			{
				return 0;
			}

			@Override public int getColumnCount(int rowIndex)
			{
				if (rowIndex % 2 == 0)
				{
					return 1;
				}
				else if (rowIndex % 3 == 0)
				{
					return 2;
				}

				return super.getColumnCount(rowIndex);
			}

			@Override public View getView(int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
				{
					convertView = getLayoutInflater().inflate(R.layout.column, parent, false);
				}

				((TextView)convertView.findViewById(android.R.id.text1)).setText(getItem(position));

				return convertView;
			}
		};

		list.setAdapter(adapter);
	}
}
