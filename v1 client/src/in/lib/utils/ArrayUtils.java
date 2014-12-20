/**
 * @brief x util is the utility library which includes the method extentions for common data types
 * 
 * @author Callum Taylor
**/
package in.lib.utils;

import java.lang.reflect.Array;

/**
* @brief This class is for applying a function to each cell in an array 
*/
public class ArrayUtils
{
	/**
	* Iterates through an array and applies the filter to each cell.
	* @param arr The input array to be iterated
	* @param iterator The iterator command to apply to the array
	*/
	public static Object[] map(Object[] arr, MapInterface iterator)
	{		
		int count = arr.length;
		for (int index = 0; index < count; index++)
		{
			arr[index] = iterator.apply(arr[index], index);
		}
		
		return arr;			
	}
	
	/**
	 * @brief Interface class to apply to each item in an array.	 
	 */
	public interface MapInterface
	{		
		/**
		 * Function to manipulate the cells in an array
		 * @param item The array item to process
		 * @param position The position of the item in the array
		 * @return The item to be reassigned to the array
		 */
		public Object apply(Object item, int position);		
	}
	
	/**
	 * Gets the index of an object in an array
	 * @param item The item to find
	 * @param array The array to look in
	 * @return The index of the item if found, if not -1 is returned
	 */
	public static <E> int indexOf(E item, E array)
	{
		for (int index = 0; index < ((E[])array).length; index++)
		{
			if (((E[])array)[index].equals(item))
			{
				return index;
			}
		}
		
		return -1;
	}
	
	/** 
	 * Create an array with default values
	 * @param <E> The instance of the new array
	 * @param size The size of the new array
	 * @param value The default value
	 * @return The newly created array
	 */
	public static <E> E[] createArray(int size, E value)
	{
		E[] array = (E[])Array.newInstance(value.getClass(), size);
		for (int index = 0; index < size; index++)
		{
			array[index] = value;
		}
		
		return array;
	}
}