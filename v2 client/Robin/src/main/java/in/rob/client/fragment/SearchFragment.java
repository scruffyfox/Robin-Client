package in.rob.client.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import in.lib.Constants;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.rob.client.R;
import in.rob.client.SearchResultsActivity;
import in.rob.client.fragment.base.BaseFragment;

@Injectable
public class SearchFragment extends BaseFragment
{
	@InjectView private EditText input;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.search_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_search, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_search)
		{
			Intent results = new Intent(getContext(), SearchResultsActivity.class);
			results.putExtra(Constants.EXTRA_SEARCH_TERM, input.getText().toString());
			getActivity().startActivity(results);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
