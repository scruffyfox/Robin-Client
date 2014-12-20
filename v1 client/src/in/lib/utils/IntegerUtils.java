package in.lib.utils;

/**
 * @brief The class that gives more manipulation options for integer
 */
public class IntegerUtils
{
	/**
	 * Parses an string to integer by removing any non-integer character
	 * @param str The input integer value as a string
	 * @return The converted integer value (I.E. 1,000 becomes 1000) returns 0 if fails
	 */
	public static int parseInt(String str)
	{
		str = str.replaceAll("[^0-9]+", "");

		try
		{
			return Integer.parseInt(str);
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	/**
	 * Adds commas to an integer
	 * @param value The integer value
	 * @return Comma formatted string value of the integer (I.E. 1000 becomes 1,000)
	 */
	public static String addCommas(int value)
	{
		String finalString = "";
		String intStr = "" + value;

		int strCount = intStr.length();
		for (int index = strCount - 1, pointerCount = 0; index >= 0; index--, pointerCount++)
		{
			if (pointerCount > 0)
			{
				if (pointerCount % 3 == 0)
				{
					finalString = "," + finalString;
				}
			}

			finalString = intStr.charAt(index) + finalString;
		}

		return finalString;
	}
}