package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.ThreadAdapter;
import in.controller.adapter.ThreadAdapter.Mode;
import in.controller.adapter.base.RobinAdapter;
import in.model.Post;
import in.rob.client.R;
import in.view.holder.ThreadPostCollapsedHolder;

public class ThreadPostCollapsedDelegate extends ThreadPostDelegate
{
	public ThreadPostCollapsedDelegate(RobinAdapter<Post> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.thread_post_collapsed_view;
	}

	@Override public View getView(Post item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		ThreadPostCollapsedHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);

			holder = new ThreadPostCollapsedHolder(convertView);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ThreadPostCollapsedHolder)convertView.getTag();
		}

		convertView.setTag(R.id.TAG_POSITION, position);
		holder.populate(item);

		if (((ThreadAdapter)getAdapter()).getMode() == Mode.NESTED)
		{
			setIndentation(convertView, item);
		}
		else
		{
			((ViewGroup)convertView).getChildAt(0).setVisibility(View.GONE);
		}

		return convertView;
	}

	@Override public boolean onItemLongClick(int position, View view)
	{
		String id = getAdapter().getItem(position).getOriginalId();
		int indent = ((ThreadAdapter)getAdapter()).getIndentSpec().get(id);

		for (int index = position, count = getAdapter().getCount(); index < count; index++)
		{
			int childIndent = ((ThreadAdapter)getAdapter()).getIndentSpec().get(getAdapter().getItem(index).getOriginalId());

			if (childIndent <= indent && index != position)
			{
				break;
			}

			if (index == position)
			{
				((ThreadAdapter)getAdapter()).getCollapsedReference().remove(id);
			}
		}

		getAdapter().notifyDataSetChanged();
		return true;
	}
}
