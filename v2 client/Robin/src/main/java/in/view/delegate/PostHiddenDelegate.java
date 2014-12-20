package in.view.delegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.model.Post;
import in.rob.client.R;

public class PostHiddenDelegate extends PostDelegate
{
	public PostHiddenDelegate(RobinAdapter<Post> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.post_hidden_view;
	}

	@Override public View getView(Post item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);
		}

		convertView.setTag(R.id.TAG_POSITION, position);

		return convertView;
	}

	@Override public boolean onItemLongClick(int position, View view)
	{
		return false;
	}
}
