package in.lib.utils;

public class BitUtils
{
	public static boolean contains(int bit, int... options)
	{
		return (bit & and(options)) == and(options);
	}

	public static int and(int... options)
	{
		int out = options[0];

		for (int index = 1; index < options.length; index++)
		{
			out &= options[index];
		}

		return out;
	}

	public static int or(int... options)
	{
		int out = options[0];

		for (int index = 1; index < options.length; index++)
		{
			out |= options[index];
		}

		return out;
	}

	public static int xor(int... options)
	{
		int out = options[0];

		for (int index = 1; index < options.length; index++)
		{
			out ^= options[index];
		}

		return out;
	}
}
