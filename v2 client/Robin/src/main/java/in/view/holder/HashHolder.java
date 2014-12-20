package in.view.holder;

import android.view.View;
import android.widget.TextView;

import in.data.entity.HashEntity;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.rob.client.R;
import in.view.holder.base.Holder;
import lombok.Getter;

@Injectable
public class HashHolder extends Holder<HashEntity>
{
	@Getter @InjectView(R.id.title) protected TextView title;

	public HashHolder(View view)
	{
		super(view);
	}

	@Override public void populate(HashEntity model)
	{
		title.setText(String.format("#%s", model.getName()));
	}
}