package in.view.holder;

import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import in.lib.manager.ImageOptionsManager;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.model.User;
import in.rob.client.R;
import in.view.holder.base.Holder;
import lombok.Getter;

@Injectable
public class TimelineHeaderHolder extends Holder<User>
{
	@Getter @InjectView(R.id.cover) protected ImageView cover;

	public TimelineHeaderHolder(View view)
	{
		super(view);
	}

	@Override public void populate(User model)
	{
		ImageLoader.getInstance().cancelDisplayTask(cover);

		if (!model.isCoverDefault())
		{
			ImageLoader.getInstance().displayImage(model.getCoverUrl(), cover, ImageOptionsManager.getInstance().getCoverImageOptions());
		}
	}
}
