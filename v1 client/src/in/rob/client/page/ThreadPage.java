package in.rob.client.page;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.adapter.PostAdapter;
import in.lib.event.DeletePostEvent;
import in.lib.event.NewPostEvent;
import in.lib.handler.streams.ThreadResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.manager.APIManager;
import in.lib.manager.UserManager;
import in.lib.utils.StringUtils;
import in.model.Post;
import in.rob.client.R;
import in.rob.client.ThreadActivity;
import in.rob.client.dialog.ReplyPostDialog;
import in.rob.client.page.base.PostStreamFragment;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.squareup.otto.Subscribe;

public class ThreadPage extends PostStreamFragment
{
	/**
	 * The current center post in the thread view
	 */
	@Getter @Setter private Post centerPost;
	@Getter @Setter private String threadId = "";

	@Override public void setupAdapters()
	{
		super.setupAdapters();
		((PostAdapter)getAdapter()).setReady(false);
	}

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);
		if (arguments != null)
		{
			if (arguments.containsKey(Constants.EXTRA_POST))
			{
				centerPost = (Post)arguments.getParcelable(Constants.EXTRA_POST);
				threadId = centerPost.getThreadId();

				if (TextUtils.isEmpty(threadId))
				{
					threadId = centerPost.getId();
				}
			}
			else if (arguments.containsKey(Constants.EXTRA_POST_ID))
			{
				threadId = arguments.getString(Constants.EXTRA_POST_ID);
				centerPost = new Post();
				centerPost.setMachinePost(true);
				centerPost.setId(arguments.getString(Constants.EXTRA_POST_ID));
			}

			if (arguments.containsKey(Constants.EXTRA_CENTER_POST_ID))
			{
				centerPost = new Post();
				centerPost.setMachinePost(true);
				centerPost.setId(arguments.getString(Constants.EXTRA_CENTER_POST_ID));
				arguments.remove(Constants.EXTRA_CENTER_POST_ID);
			}
		}
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		onDataReady();
	}

	@Override public void onDataReady()
	{
		// add the center post into the list
		if (centerPost != null && !centerPost.isMachinePost())
		{
			threadId = centerPost.getThreadId();

			if (getAdapter().indexOf(centerPost) < 0)
			{
				getAdapter().addItem(centerPost);
			}

			((PostAdapter)getAdapter()).setCenter(centerPost);
			getAdapter().notifyDataSetChanged();
		}
	}

	@Override public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
	{
		Post p = (Post)getAdapter().getItem(position - getListView().getHeaderViewsCount());

		if (p != centerPost)
		{
			centerPost = p;
			((ThreadActivity)getActivity()).setPost(centerPost);
			((PostAdapter)getAdapter()).setCenter(centerPost);
			getAdapter().notifyDataSetChanged();
		}
	}

	/**
	 * Checks the adapter sizes and removes the appropriate headers
	 */
	@Override public void checkAdapterSizes()
	{
		try
		{
			if (getAdapter().getCount() - ((PostAdapter)getAdapter()).getCenterPosition() > 3)
			{
				getHeadedListView().removeFooterView(getPaddingView());
			}
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public void addDefaultFooters()
	{
		super.addDefaultFooters();
		addPaddingView();
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		if (!append && !isLoading())
		{
			ThreadResponseHandler handler = new ThreadResponseHandler(getApplicationContext());
			handler.setResponseKey(getResponseKeys()[0]);
			ResponseHelper.getInstance().addResponse(getResponseKeys()[0], handler, this);
			APIManager.getInstance().getPostThread(threadId, handler);
		}
	}

	/**
	 * Get's the thread posts/replies
	 *
	 * Use this once at the start, and when the user forces a refresh.
	 * Use {@link #fetchStream(String, boolean)} for pagination
	 */
	@Override public void beginLoadFromApi()
	{
		//fetchStream("", false);
		onForceRefresh();
	}

	@Override public String getCacheFileName()
	{
		return null;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{String.format(Constants.RESPONSE_THREAD, threadId)};
	}

	@Subscribe @Override public void onPostRecieved(NewPostEvent event)
	{
		Post p = event.getPost();

		if (p != null)
		{
			if (p.getThreadId().equals(threadId))
			{
				((PostAdapter)getAdapter()).appendItem(p);
				getAdapter().notifyDataSetChanged();
			}
		}
	}

	@Subscribe @Override public void onPostDeleted(DeletePostEvent event)
	{
		super.onPostDeleted(event);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_new_post)
		{
			replyAll();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void replyAll()
	{
		if (centerPost == null) return;

		ArrayList<String> participents = new ArrayList<String>();

		for (int index = 0; index < getAdapter().getCount(); index++)
		{
			Post p = (Post)getAdapter().getItem(index);
			if (!participents.contains("@" + p.getPoster().getMentionName()))
			{
				participents.add("@" + p.getPoster().getMentionName());
			}
		}

		if (participents.size() < 1)
		{
			participents.add("@" + UserManager.getUser().getMentionName());
		}
		else
		{
			participents.remove("@" + UserManager.getUser().getMentionName());
		}

		String participentsStr = StringUtils.join(participents, " ");

		Intent in = new Intent(getContext(), ReplyPostDialog.class);
		in.putExtra(Constants.EXTRA_POST_ID, centerPost.getId());
		in.putExtra(Constants.EXTRA_TEXT, participentsStr);
		startActivityForResult(in, Constants.REQUEST_REPLY_POST);
	}

	@Override public void addLoadMoreView(){}
	@Override public void removeLoadMoreView(){}
}