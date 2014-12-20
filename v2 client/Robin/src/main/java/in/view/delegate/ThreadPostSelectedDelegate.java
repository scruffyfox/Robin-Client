package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.model.Post;
import in.rob.client.R;
import in.view.holder.ThreadPostSelectedHolder;

public class ThreadPostSelectedDelegate extends ThreadPostDelegate
{
	public ThreadPostSelectedDelegate(RobinAdapter<Post> adapter)
	{
		super(adapter);
	}

	@Override public int getLayout()
	{
		return R.layout.thread_post_selected_view;
	}

	@Override public View getView(Post item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		ThreadPostSelectedHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);

			holder = new ThreadPostSelectedHolder(convertView);
			holder.getReply().setOnClickListener(this);
			holder.getReplyAll().setOnClickListener(this);
			holder.getRepost().setOnClickListener(this);
			holder.getShare().setOnClickListener(this);
			holder.getMore().setOnClickListener(this);

			convertView.setTag(holder);
		}
		else
		{
			holder = (ThreadPostSelectedHolder)convertView.getTag();
		}

		convertView.setTag(R.id.TAG_POSITION, position);
		holder.populate(item);

		return convertView;
	}

	@Override public boolean onItemLongClick(int position, View view)
	{
		return false;
	}
}
