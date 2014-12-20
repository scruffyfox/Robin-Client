package in.view.holder;

import android.view.View;
import android.widget.TextView;

import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.model.Post;
import in.rob.client.R;
import in.view.holder.base.Holder;
import lombok.Getter;

@Injectable
public class PostCollapsedHolder extends Holder<Post>
{
	@Getter @InjectView(R.id.title) protected TextView title;

	public PostCollapsedHolder(View view)
	{
		super(view);
	}

	@Override public void populate(Post model)
	{
		title.setText(String.format("Collapsed thread id#%s", model.getThreadId()));
	}
}
