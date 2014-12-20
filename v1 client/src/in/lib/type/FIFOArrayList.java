package in.lib.type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Limited array list. When limit is reached, the first object inputted is the first object removed.
 */
public class FIFOArrayList<E> extends ArrayList<E> implements Serializable
{
	private int size = 0;
	private int max = -1;

	public FIFOArrayList()
	{
		// TODO Auto-generated constructor stub
	}

	public FIFOArrayList(int max)
	{
		this.max = max;
	}

	/**
	 * Sets the max size of the list
	 * @param max
	 */
	public void setMax(int max)
	{
		this.max = max;
	}

	@Override public boolean add(E object)
	{
		size++;
		checkSize();
		return super.add(object);
	}

	@Override public boolean addAll(Collection<? extends E> collection)
	{
		size += collection.size();
		checkSize();
		return super.addAll(collection);
	}

	@Override public void add(int index, E object)
	{
		size++;
		checkSize();
		super.add(index, object);
	}

	@Override public boolean addAll(int index, Collection<? extends E> collection)
	{
		size += collection.size();
		checkSize();
		return super.addAll(index, collection);
	}

	@Override public E remove(int index)
	{
		size--;
		return super.remove(index);
	}

	@Override public boolean remove(Object object)
	{
		size--;
		return super.remove(object);
	}

	@Override public boolean removeAll(Collection<?> collection)
	{
		size -= collection.size();
		return super.removeAll(collection);
	}

	@Override protected void removeRange(int fromIndex, int toIndex)
	{
		size -= (toIndex - fromIndex);
		super.removeRange(fromIndex, toIndex);
	}

	private void checkSize()
	{
		if (size > max && max > 0 && size - max > 0)
		{
			this.removeRange(0, size - max);
		}
	}
}