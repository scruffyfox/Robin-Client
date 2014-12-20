package in.rob.client.page;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.adapter.DraftAdapter;
import in.lib.event.DeletePostDraftEvent;
import in.lib.event.NewPostDraftEvent;
import in.lib.event.UpdatedPostDraftEvent;
import in.lib.loader.base.Loader;
import in.lib.manager.CacheManager;
import in.model.DraftPost;
import in.model.Stream;
import in.model.base.NetObject;
import in.rob.client.dialog.NewPostDialog;
import in.rob.client.page.base.StreamFragment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.squareup.otto.Subscribe;

public class DraftsPage extends StreamFragment implements OnItemClickListener
{
	@Override public void retrieveArguments(Bundle savedInstanceState)
	{
	}

	@Override public void setupAdapters()
	{
		if (getAdapter() == null)
		{
			setAdapter(new DraftAdapter(getContext(), new ArrayList<DraftPost>()));
		}
		else
		{
			setAdapter(getAdapter());
		}
	}

	@Override public void initData()
	{
		new CacheLoader().execute();
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		getHeadedListView().setCanRefresh(false);
		getEmptyListView().setCanRefresh(false);
	}

	@Override public void onDataReady()
	{
		hideProgressLoader();
	}

	@Override public void addLoadMoreView()
	{
	}

	@Override public void removeLoadMoreView()
	{
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
	}

	@Override public String getCacheFileName()
	{
		return "";
	}

	@Override public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
	{
		DraftPost post = (DraftPost)getAdapter().getItem(position - getHeadedListView().getHeaderViewsCount());

		Intent editIntent = new Intent(getContext(), NewPostDialog.class);
		editIntent.putExtra(Constants.EXTRA_NEW_POST_DRAFT, post.serialize());
		getContext().startActivity(editIntent);
	}

	/**
	 * Checks the adapter sizes and removes the appropriate headers
	 */
	@Override public void checkAdapterSizes()
	{
		removeLoadMoreView();
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{};
	}

	public class CacheLoader extends Loader<Stream>
	{
		public CacheLoader()
		{
			super("");
		}

		@Override public Stream doInBackground()
		{
			try
			{
				Stream stream = new Stream();

				File files = new File(CacheManager.getInstance().getCachePath());
				String[] drafts = files.list(new FilenameFilter()
				{
					@Override public boolean accept(File dir, String filename)
					{
						return filename.startsWith("cache_" + Constants.CACHE_DRAFT_PREFIX);
					}
				});

				Arrays.sort(drafts, Collections.reverseOrder());

				for (String draft : drafts)
				{
					DraftPost post = CacheManager.getInstance().readFileAsObject(draft.replace("cache_", ""), DraftPost.class);
					if (post != null)
					{
						stream.getObjects().add(post);
					}
				}

				return stream;
			}
			catch (Exception e)
			{
				Debug.out(e);
				return null;
			}
		}

		@Override public void onPostExecute(Stream stream)
		{
			super.onPostExecute(stream);

			if (getActivity() == null)
			{
				return;
			}

			if (stream != null && stream.getObjects().size() > 0)
			{
				getAdapter().setStream(stream);
				getAdapter().notifyDataSetChanged();
			}

			stream = null;
			checkAdapterSizes();
			onDataReady();
		}
	}

	@Override public View createPaddingView()
	{
		return new View(getContext());
	}

	/**************************************************
	 *
	 *  OTTO EVENTS
	 *
	 **************************************************/

	@Subscribe public void onDraftAdded(NewPostDraftEvent event)
	{
		prependItem(event.getPost());
	}

	@Subscribe public void onDraftDeleted(DeletePostDraftEvent event)
	{
		deleteItem(event.getPost());
	}

	@Subscribe public void onDraftUpdated(UpdatedPostDraftEvent event)
	{
		int pos = -1;
		for (NetObject item : getAdapter().getItems())
		{
			pos++;

			if (event.getPost().getDate() == ((DraftPost)item).getDate())
			{
				break;
			}
		}

		if (pos > -1 && pos < getAdapter().getCount())
		{
			getAdapter().removeItemAt(pos);
			getAdapter().addItem(pos, event.getPost());
			getAdapter().notifyDataSetChanged();
		}
	}
}