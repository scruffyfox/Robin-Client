package in.rob.client.fragment;

import android.content.Intent;

import in.controller.adapter.InteractionAdapter;
import in.controller.handler.InteractionStreamResponseHandler;
import in.data.stream.InteractionStream;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.utils.Views.Injectable;
import in.model.AdnModel;
import in.model.Interaction;
import in.model.Interaction.Type;
import in.rob.client.ProfileActivity;
import in.rob.client.ThreadActivity;
import in.rob.client.fragment.base.StreamFragment;

@Injectable
public class InteractionsFragment extends StreamFragment
{
	@Override public void setupAdapter()
	{
		setAdapter(new InteractionAdapter(getContext()));
	}

	@Override public void onDataReady()
	{

	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		InteractionStreamResponseHandler response = new InteractionStreamResponseHandler(append);
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
		APIManager.getInstance().getInteractions(lastId, response);
	}

	@Override public void onListItemClick(AdnModel item)
	{
		Interaction model = (Interaction)item;

		if (model.getType() == Type.REPOST || model.getType() == Type.STAR)
		{
			Intent threadIntent = new Intent(getContext(), ThreadActivity.class);
			threadIntent.putExtra(Constants.EXTRA_POST, model.getObject());
			getActivity().startActivity(threadIntent);
		}
		else
		{
			if (model.getUsers().size() == 1)
			{
				Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
				profileIntent.putExtra(Constants.EXTRA_USER, model.getUsers().get(0));
				getActivity().startActivity(profileIntent);
			}
		}
	}

	@Override public Class getCacheClass()
	{
		return InteractionStream.class;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			Constants.RESPONSE_INTERACTIONS
		};
	}

	@Override public String getCacheKey()
	{
		return Constants.CACHE_INTERACTIONS;
	}
}
