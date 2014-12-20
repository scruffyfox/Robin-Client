package in.rob.client.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import net.callumtaylor.swipetorefresh.view.RefreshableListView;
import net.callumtaylor.swipetorefresh.view.RefreshableScrollView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;

import in.controller.adapter.DraftAdapter;
import in.data.stream.DraftPostStream;
import in.data.stream.base.Stream;
import in.lib.Constants;
import in.lib.loader.Loader;
import in.lib.loader.Loader.OnFileLoadedListener;
import in.lib.manager.CacheManager;
import in.lib.utils.Debug;
import in.lib.utils.Views.Injectable;
import in.model.AdnModel;
import in.model.DraftPost;
import in.rob.client.dialog.NewPostDialog;
import in.rob.client.fragment.base.StreamFragment;

@Injectable
public class DraftPostsFragment extends StreamFragment
{
	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		((RefreshableListView)getListView()).setCanRefresh(false);
		((RefreshableScrollView)getEmpty()).setCanRefresh(false);
	}

	@Override public void setupAdapter()
	{
		setAdapter(new DraftAdapter(getContext()));
	}

	@Override public void onDataReady()
	{
		getProgress().setVisibility(View.GONE);
	}

	@Override public void initData()
	{
		DraftCacheLoader loader = new DraftCacheLoader();
		loader.setOnFileLoadedListener(new OnFileLoadedListener<DraftPostStream>()
		{
			@Override public void onFileLoaded(DraftPostStream data, long age)
			{
				if (data != null)
				{
					getAdapter().setStream(data);
					getAdapter().notifyDataSetChanged();
				}

				onDataReady();
			}
		});
		loader.execute();
	}

	@Override public void onListItemClick(AdnModel item)
	{
		Intent postIntent = new Intent(getContext(), NewPostDialog.class);
		postIntent.putExtra(Constants.EXTRA_DRAFT_POST, (Parcelable)item);
		getActivity().startActivity(postIntent);
	}

	@Override public void fetchStream(String lastId, boolean append){}
	@Override public void handleResponse(Stream stream, boolean append){}
	@Override public void setupFooters(){}

	@Override public Class getCacheClass()
	{
		return DraftPostStream.class;
	}

	public static class DraftCacheLoader extends Loader<DraftPostStream>
	{
		@Override public DraftPostStream doInBackground(String... params)
		{
			try
			{
				DraftPostStream stream = new DraftPostStream();

				File files = new File(CacheManager.getInstance().getCachePath());
				String[] drafts = files.list(new FilenameFilter()
				{
					@Override public boolean accept(File dir, String filename)
					{
						return filename.startsWith("post_");
					}
				});

				Arrays.sort(drafts, Collections.reverseOrder());

				for (String draft : drafts)
				{
					DraftPost post = new DraftPost().load(draft.replace("post_", ""));
					if (post != null)
					{
						stream.getItems().add(post);
					}
				}

				return stream;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}

			return null;
		}
	}
}
