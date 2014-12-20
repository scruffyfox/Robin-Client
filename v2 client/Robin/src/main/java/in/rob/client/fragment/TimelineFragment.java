package in.rob.client.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import in.controller.adapter.PostAdapter;
import in.controller.handler.PostStreamResponseHandler;
import in.data.stream.PostStream;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.BitUtils;
import in.lib.utils.Views.Injectable;
import in.model.AdnModel;
import in.rob.client.R;
import in.rob.client.ThreadActivity;
import in.rob.client.fragment.base.StreamFragment;
import in.view.holder.TimelineHeaderHolder;

@Injectable
public class TimelineFragment extends StreamFragment
{
	private TimelineHeaderHolder header;

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (header != null)
		{
			header.populate(UserManager.getInstance().getUser());
		}
	}

	@Override public void setupAdapter()
	{
		setAdapter(new PostAdapter(getContext()));
	}

	@Override public void setupHeaders()
	{
		if (BitUtils.contains(SettingsManager.getInstance().getShowHideBit(), Constants.BIT_SHOWHIDE_TIMELINE_COVER))
		{
			View headerView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_header_view, getListView(), false);
			header = new TimelineHeaderHolder(headerView);
			getListView().addHeaderView(headerView, null, false);
		}
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		PostStreamResponseHandler response = new PostStreamResponseHandler(append);
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
		APIManager.getInstance().getUnifiedTimeLine(lastId, 60, response);
	}

	@Override public void onListItemClick(AdnModel model)
	{
		Intent threadIntent = new Intent(getContext(), ThreadActivity.class);
		threadIntent.putExtra(Constants.EXTRA_POST, model);
		getActivity().startActivity(threadIntent);
	}

	@Override public Class getCacheClass()
	{
		return PostStream.class;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			String.format(Constants.RESPONSE_TIMELINE, UserManager.getInstance().getUser().getId())
		};
	}

	@Override public String getCacheKey()
	{
		return String.format(Constants.CACHE_TIMELINE, UserManager.getInstance().getUser().getId());
	}
}
