package in.view.holder.base;

import android.view.View;

import in.lib.utils.Views;
import in.lib.utils.Views.Injectable;

@Injectable
public abstract class Holder<T>
{
	public Holder(View view)
	{
		Views.inject(this, view);
	}

	public abstract void populate(T model);
}
