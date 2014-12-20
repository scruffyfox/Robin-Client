/**
 * @brief x util is the utility library which includes the method extentions for common data types
 *
 * @author Callum Taylor
**/
package in.lib.utils;

import in.lib.utils.ArrayUtils.MapInterface;

import java.util.Collection;

/**
 * @brief Class to extend manipulation to strings
 */
public class StringUtils
{
	/**
	 * Capitalizes each word in a string
	 * @param input The input string
	 * @return The formatted string
	 */
	public static String capitalize(String input)
	{
		if (input == null || input.length() < 2) return "";

		String[] words = input.split(" ");
		int length = words.length;

		for (int i = 0; i < length; ++i)
		{
			words[i] = (Character.toUpperCase(words[i].charAt(0))) + words[i].substring(1, words[i].length());
		}

		return StringUtils.join(words, " ");
	}

	public static String trimString(String string, int length, boolean soft)
	{
		if (string == null || string.trim().isEmpty())
		{
			return string;
		}

		StringBuffer sb = new StringBuffer(string);
		int actualLength = length - 3;
		if (sb.length() > actualLength)
		{
			// -3 because we add 3 dots at the end. Returned string length has
			// to be length including the dots.
			if (!soft)
				return (sb.insert(actualLength, "...").substring(0, actualLength + 3));
			else
			{
				int endIndex = sb.indexOf(" ", actualLength);
				String str = sb.insert(endIndex, "...").toString();
				if (endIndex + 3 > 0)
				{
					str = str.substring(0, endIndex + 3);
				}

				return str;
			}
		}
		return string;
	}

	/**
	 * Joins an array together with a string, will remove any cells with a null value
	 * @param arr The input array
	 * @param glue The glue
	 * @return The joined string with the glue seperators
	 */
	public static String join(Object[] arr, String glue)
	{
		String retString = "";
		int arrCount = arr.length;

		for (int arrIndex = 0; arrIndex < arrCount; arrIndex++)
		{
			if (arr[arrIndex] != null && !(arr[arrIndex].toString()).trim().equals(""))
			{
				retString += (arr[arrIndex].toString()).trim();
				retString += glue;
			}
		}

		if (retString.length() >= glue.length())
		{
			retString = retString.substring(0, retString.length() - glue.length());
		}

		return retString;
	}

	/**
	 * Joins an array together with a string, will remove any cells with a null value
	 * @param arr The input array
	 * @param glue The glue
	 * @return The joined string with the glue seperators
	 */
	public static String join(Collection arr, String glue)
	{
		return join(arr.toArray(), glue);
	}

	/**
	 * Splits a string with the specified seperator, will remove any cells that are null
	 * @param input The input string
	 * @param splitStr The split seperator
	 * @return The string array
	 */
	public static String[] split(String input, String splitStr)
	{
		String[] arr = input.split(splitStr);
		arr = (String[])ArrayUtils.map(arr, new MapInterface()
		{
			@Override public Object apply(Object item, int position)
			{
				item = ((String) item).trim();
				return item;
			}
		});

		return arr;
	}

	/**
	 * Pads a string to a certain length with a certain string
	 * @param str The input string
	 * @param maxSize The max length of the string
	 * @param chr The string to pad the input string with
	 * @return The new padded string
	 */
	public static String padTo(String str, int maxSize, String chr)
	{
		return padTo(str, maxSize, chr, false);
	}

	/**
	 * Pads a string to a certain length with a certain string
	 * @param str The input string
	 * @param maxSize The max length of the string
	 * @param chr The string to pad the input string with
	 * @param padLeft Whether to pad the string to the left or not. If false the string will pad to the right
	 * @return The new padded string
	 */
	public static String padTo(String str, int maxSize, String chr, boolean padLeft)
	{
		int strLen = str.length();
		String newStr = str;

		if (strLen < maxSize)
		{
			String pad = "";
			for (int padCount = 0; padCount < maxSize - strLen; padCount++)
			{
				pad += chr;
			}

			if (padLeft)
			{
				newStr = pad + newStr;
			}
			else
			{
				newStr += pad;
			}
		}

		return newStr;
	}

	/**
	 * Sub strings a string to the set length and appends with elipses. This method does not cut off mid-word
	 * @param str The string to preview
	 * @param limitSize The max size of the string
	 * @return The newly formatted string
	 */
	public static String preview(String str, int limitSize)
	{
		int count = limitSize;

		if (str.length() > limitSize)
		{
			while (str.charAt(limitSize) != ' ' && limitSize < str.length() - 1)
			{
				count++;
				if (count - limitSize == 12)
				{
					return str.substring(0, limitSize) + "...";
				}
			}

			return str.substring(0, limitSize) + "...";
		}
		else
		{
			return str;
		}
	}

	/**
	 * Checks if a string is empty
	 * @param str The string to check
	 * @return False if the string is not empty, true if is.
	 */
	public static boolean isEmpty(String str)
	{
		return str != null && str.length() < 1;
	}
}