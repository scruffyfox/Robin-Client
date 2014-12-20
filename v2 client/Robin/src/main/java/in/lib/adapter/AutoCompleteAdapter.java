package in.lib.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.data.entity.HashEntity;
import in.model.AdnModel;
import in.model.SimpleUser;
import in.view.delegate.AutoCompleteHashDelegate;
import in.view.delegate.AutoCompleteUserDelegate;
import in.view.delegate.base.AdapterDelegate;
import lombok.Getter;

public class AutoCompleteAdapter extends BaseAdapter implements Filterable
{
	@Getter private final Context context;
	@Getter private final List<? extends AdnModel> items;
	@Getter private List<? extends AdnModel> originalItems;
	@Getter private SparseArray<AdapterDelegate<?>> itemTypes = new SparseArray<AdapterDelegate<?>>();
	private String typedConstraint = "";

	private static final int TYPE_USERNAMES = 0;
	private static final int TYPE_HASHTAGS = 1;

	public AutoCompleteAdapter(Context context, List<? extends AdnModel> items)
	{
		this.context = context;
		this.items = items;
		this.originalItems = new ArrayList<AdnModel>(items);

		itemTypes.put(TYPE_USERNAMES, new AutoCompleteUserDelegate(null));
		itemTypes.put(TYPE_HASHTAGS, new AutoCompleteHashDelegate(null));
	}

	public void setItems(Collection users)
	{
		this.items.clear();
		this.items.addAll(users);
		this.originalItems = new ArrayList<AdnModel>(items);
	}

	@Override public int getCount()
	{
		return items.size();
	}

	@Override public AdnModel getItem(int position)
	{
		return items.get(position);
	}

	@Override public long getItemId(int position)
	{
		return items.get(position).hashCode();
	}

	@Override public int getViewTypeCount()
	{
		return itemTypes.size();
	}

	@Override public int getItemViewType(int position)
	{
		AdnModel item = getItem(position);

		if (item instanceof SimpleUser)
		{
			return TYPE_USERNAMES;
		}
		else if (item instanceof HashEntity)
		{
			return TYPE_HASHTAGS;
		}

		return -1;
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		int viewType = getItemViewType(position);
		AdnModel item = getItem(position);

		convertView = ((AdapterDelegate)getItemTypes().get(viewType)).getView(item, position, convertView, parent, LayoutInflater.from(getContext()));

		return convertView;
	}

	@Override public Filter getFilter()
	{
		Filter myFilter = new Filter()
		{
			@Override protected FilterResults performFiltering(CharSequence constraint)
			{
				FilterResults filterResults = new FilterResults();
				if (constraint != null)
				{
					typedConstraint = constraint.toString().toLowerCase();

					List<AdnModel> items = new ArrayList<AdnModel>();
					for (AdnModel item : originalItems)
					{
						if (item instanceof SimpleUser)
						{
							String testUsername = String.format("@%s", ((SimpleUser)item).getUsername()).toLowerCase();
							String testName = String.format("@%s", ((SimpleUser)item).getFullname()).toLowerCase();
							if (testUsername.startsWith(typedConstraint)
							|| testName.startsWith(typedConstraint))
							{
								items.add(item);
							}
						}
						else if (item instanceof HashEntity)
						{
							String testHash = String.format("#%s", ((HashEntity)item).getName()).toLowerCase();
							if (testHash.startsWith(typedConstraint))
							{
								items.add(item);
							}
						}
					}

					Collections.sort(items, new Comparator<AdnModel>()
					{
						@Override public int compare(AdnModel lhs, AdnModel rhs)
						{
							if (lhs instanceof SimpleUser && rhs instanceof SimpleUser)
							{
								String lhsUsername = String.format("@%s", ((SimpleUser)lhs).getUsername());
								String rhsUsername = String.format("@%s", ((SimpleUser)rhs).getUsername());

								if (lhsUsername.indexOf(typedConstraint) < rhsUsername.indexOf(typedConstraint))
								{
									return 1;
								}
								else if (lhsUsername.indexOf(typedConstraint) > rhsUsername.indexOf(typedConstraint))
								{
									return -1;
								}
								else
								{
									return 0;
								}
							}
							else if (lhs instanceof HashEntity && rhs instanceof HashEntity)
							{
								String lhsTag = String.format("#%s", ((HashEntity)lhs).getName());
								String rhsTag = String.format("#%s", ((HashEntity)rhs).getName());

								if (lhsTag.indexOf(typedConstraint) < rhsTag.indexOf(typedConstraint))
								{
									return 1;
								}
								else if (lhsTag.indexOf(typedConstraint) > rhsTag.indexOf(typedConstraint))
								{
									return -1;
								}
								else
								{
									return 0;
								}
							}

							return 0;
						}
					});

					// Now assign the values and count to the FilterResults
					// object
					filterResults.values = items;
					filterResults.count = items.size();
				}

				return filterResults;
			}

			@Override protected void publishResults(CharSequence contraint, FilterResults results)
			{
				if (results != null && results.count > 0)
				{
					getItems().clear();
					getItems().addAll((Collection)results.values);

					notifyDataSetChanged();
				}
			}
		};

		return myFilter;
	}
}
