package in.rob.client.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.FileAdapter;
import in.controller.handler.FileStreamResponseHandler;
import in.data.annotation.FileAnnotation;
import in.data.stream.FileStream;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.utils.Views;
import in.lib.utils.Views.Injectable;
import in.model.AdnModel;
import in.rob.client.R;
import in.rob.client.fragment.base.StreamFragment;

@Injectable
public class StorageBrowserFragment extends StreamFragment
{
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.stream_grid_layout, container, false);
		Views.inject(this, view);

		getListView().setEmptyView(getEmpty());

		return view;
	}

	@Override public void setupAdapter()
	{
		setAdapter(new FileAdapter(getContext()));
		getListView().setDividerHeight(0);
		getListView().setDivider(null);
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		FileStreamResponseHandler response = new FileStreamResponseHandler(append);
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
		APIManager.getInstance().getUserFiles(lastId, response);
	}

	@Override public void onListItemClick(AdnModel model)
	{
		FileAnnotation file = (FileAnnotation)model;

		Intent data = new Intent();
		data.putExtra(Constants.EXTRA_FILE, (Parcelable)file);

		getActivity().setResult(Activity.RESULT_OK, data);
		getActivity().finish();
	}

	@Override public Class getCacheClass()
	{
		return FileStream.class;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{"files"};
	}

	@Override public String getCacheKey()
	{
		return "storage_files";
	}

	@Override public int getCacheTimeout()
	{
		return 0;
	}
}
