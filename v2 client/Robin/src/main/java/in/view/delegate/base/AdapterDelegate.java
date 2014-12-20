package in.view.delegate.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.model.AdnModel;
import lombok.Getter;
import lombok.Setter;

public abstract class AdapterDelegate<T extends AdnModel>
{
	@Getter @Setter private RobinAdapter<T> adapter;

	public AdapterDelegate(RobinAdapter<T> adapter)
	{
		this.adapter = adapter;
	}

	public abstract View getView(T item, int position, View convertView, ViewGroup parent, LayoutInflater inflater);

	public boolean onItemLongClick(int position, View view)
	{
		return false;
	}
}
