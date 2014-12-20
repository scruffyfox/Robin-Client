package in.rob.client.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.List;

import in.controller.adapter.ThreadAdapter;
import in.controller.adapter.ThreadAdapter.Mode;
import in.controller.handler.PostStreamResponseHandler;
import in.data.Meta;
import in.data.stream.PostStream;
import in.data.stream.base.Stream;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views.Injectable;
import in.lib.writer.CacheWriter;
import in.model.AdnModel;
import in.model.Post;
import in.rob.client.R;
import in.rob.client.dialog.ReplyPostDialog;
import in.rob.client.fragment.base.StreamFragment;
import in.view.delegate.PostDelegate;

@Injectable
public class ThreadFragment extends StreamFragment
{
	private Post thread;

	@Override public void onDataReady()
	{
		getActivity().invalidateOptionsMenu();
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null && arguments.containsKey(Constants.EXTRA_POST))
		{
			thread = (Post)arguments.getParcelable(Constants.EXTRA_POST);
		}
		else
		{
			Toast.makeText(getContext(), R.string.thread_stream_fail, Toast.LENGTH_SHORT).show();
			getActivity().finish();
			return;
		}
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		if (thread != null)
		{
			outState.putParcelable(Constants.EXTRA_POST, thread);
		}
	}

	@Override public void setupAdapter()
	{
		setAdapter(new ThreadAdapter(getContext()));

		if (thread != null)
		{
			PostStream fauxStream = new PostStream();
			Meta fauxMeta = new Meta();
			fauxMeta.setMoreAvailable(false);
			fauxMeta.setMinId(thread.getOriginalId());
			fauxMeta.setMaxId(thread.getOriginalId());
			fauxStream.setMeta(fauxMeta);
			fauxStream.getItems().add(thread);

			getAdapter().setStream(fauxStream);
			((ThreadAdapter)getAdapter()).setSelectedPost(thread);
			getAdapter().notifyDataSetChanged();
		}
	}

	@Override public void setupFooters()
	{
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		PostStreamResponseHandler response = new PostStreamResponseHandler(append);
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);

		if (!append)
		{
			APIManager.getInstance().getThreadStart(thread.getThreadId(), thread.getOriginalId(), response);
		}
	}

	@Override public void handleResponse(Stream stream, boolean append)
	{
		getRefreshHelper().finish();

		if (stream != null && !append)
		{
//			((ThreadAdapter)getAdapter()).setMode(Mode.STANDARD);

			if (append)
			{
				getAdapter().appendStream(stream);
			}
			else
			{
				getAdapter().setStream(stream);
			}

			if (thread != null)
			{
				for (Post item : (List<Post>)stream.getItems())
				{
					if (thread.equals(item))
					{
						thread = item;
						break;
					}
				}

				((ThreadAdapter)getAdapter()).setSelectedPost(thread);
			}

			getAdapter().notifyDataSetChanged();

			// write to cache
			CacheWriter writer = new CacheWriter(getCacheKey());
			writer.write(getAdapter().getStream());
		}

		getActivity().invalidateOptionsMenu();
	}

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (((ThreadAdapter)getAdapter()).getMode() == Mode.NESTED)
		{
			int pos = position - getListView().getHeaderViewsCount();
			int viewType = getAdapter().getItemViewType(pos);

			if (viewType == ThreadAdapter.TYPE_POST)
			{
				((PostDelegate)((ThreadAdapter)getAdapter()).getItemTypes().get(viewType)).toggleOptions(pos, view);
			}
			else if (viewType == ThreadAdapter.TYPE_POST_COLLAPSED_HEADER)
			{
				((PostDelegate)((ThreadAdapter)getAdapter()).getItemTypes().get(viewType)).onItemLongClick(pos, view);
			}
		}
		else
		{
			super.onItemClick(parent, view, position, id);
		}
	}

	@Override public void onListItemClick(AdnModel model)
	{
		thread = (Post)model;
		((ThreadAdapter)getAdapter()).setSelectedPost((Post)model);
		getAdapter().notifyDataSetChanged();
	}

	@Override public Class getCacheClass()
	{
		return PostStream.class;
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			String.format(Constants.RESPONSE_THREAD, thread.getThreadId())
		};
	}

	@Override public String getCacheKey()
	{
		return String.format(Constants.RESPONSE_THREAD, thread.getThreadId());
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_thread, menu);

		if (getAdapter().getCount() < 2)
		{
			menu.findItem(R.id.menu_nest).setVisible(false);
		}
		else
		{
			if (((ThreadAdapter)getAdapter()).getMode() == Mode.NESTED)
			{
				menu.findItem(R.id.menu_nest).setVisible(false);
				menu.findItem(R.id.menu_denest).setVisible(true);
			}
			else
			{
				menu.findItem(R.id.menu_nest).setVisible(true);
				menu.findItem(R.id.menu_denest).setVisible(false);
			}
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_nest)
		{
			((ThreadAdapter)getAdapter()).setMode(Mode.NESTED);
			getAdapter().setStream(getAdapter().getStream());
			getAdapter().notifyDataSetChanged();
			getActivity().invalidateOptionsMenu();

			if (thread != null)
			{
				final int itemPos = getAdapter().indexOf(thread);
				int viewType = getAdapter().getItemViewType(itemPos);
				int firstPos = getListView().getFirstVisiblePosition() - getListView().getHeaderViewsCount();
				int lastPos = getListView().getLastVisiblePosition();

				if (itemPos >= firstPos && itemPos <= lastPos)
				{
					final int pos = itemPos - firstPos;

					if (viewType == ThreadAdapter.TYPE_POST)
					{
						getListView().post(new Runnable()
						{
							@Override public void run()
							{
								((PostDelegate)((ThreadAdapter)getAdapter()).getItemTypes().get(ThreadAdapter.TYPE_POST)).toggleOptions(itemPos, getListView().getChildAt(pos));
							}
						});
					}
				}
			}

			return true;
		}
		else if (item.getItemId() == R.id.menu_denest)
		{
			((ThreadAdapter)getAdapter()).setMode(Mode.STANDARD);
			getAdapter().setStream(getAdapter().getStream());
			getAdapter().notifyDataSetChanged();
			getActivity().invalidateOptionsMenu();

			return true;
		}
		else if (item.getItemId() == R.id.menu_refresh)
		{
			getRefreshHelper().refresh();
		}
		else if (item.getItemId() == R.id.menu_collapse)
		{
			SettingsManager.getInstance().collapseThread(thread.getThreadId());
		}
		else if (item.getItemId() == R.id.menu_reply_all)
		{
			int count = getAdapter().getCount();
			StringBuilder usernames = new StringBuilder(count * 10);

			for (int index = 0; index < count; index++)
			{
				Post post = (Post)getAdapter().getItem(index);

				if (usernames.indexOf(post.getPoster().getUsername()) < 0 && !post.getPoster().getUsername().equals(thread.getPoster().getUsername()))
				{
					if (usernames.length() > 0)
					{
						usernames.append(" ");
					}

					usernames.append("@").append(post.getPoster().getUsername());
				}
			}

			Intent replyIntent = new Intent(getContext(), ReplyPostDialog.class);
			replyIntent.putExtra(Constants.EXTRA_POST, (Parcelable)thread);
			replyIntent.putExtra(Constants.EXTRA_REPLY_EXTRA, usernames.toString());
			startActivity(replyIntent);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override public int getCacheTimeout()
	{
		return 60 * 1 * 1000;
	}
}
