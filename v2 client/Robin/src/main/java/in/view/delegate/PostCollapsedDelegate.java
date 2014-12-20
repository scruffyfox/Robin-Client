package in.view.delegate;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.lib.manager.SettingsManager;
import in.model.Post;
import in.rob.client.R;
import in.view.holder.PostCollapsedHolder;

public class PostCollapsedDelegate extends PostDelegate
{
	public PostCollapsedDelegate(RobinAdapter<Post> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.post_collapsed_view;
	}

	@Override public View getView(Post item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		PostCollapsedHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);

			holder = new PostCollapsedHolder(convertView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (PostCollapsedHolder)convertView.getTag();
		}

		convertView.setTag(R.id.TAG_POSITION, position);
		holder.populate(item);

		return convertView;
	}

	@Override public boolean onItemLongClick(int position, View view)
	{
		final Post item = getAdapter().getItem(position - getAdapter().getListView().getHeaderViewsCount());
		final Builder dialogBuilder = new Builder(view.getContext());
		String[] options = {view.getContext().getString(R.string.expand_thread)};

		dialogBuilder.setItems(options, new OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				SettingsManager.getInstance().expandThread(item.getThreadId());
				getAdapter().notifyDataSetChanged();
			}
		});

		dialogBuilder.show();
		return true;
	}
}
