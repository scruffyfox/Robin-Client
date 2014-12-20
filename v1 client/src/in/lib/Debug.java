package in.lib;

import java.text.DecimalFormat;
import java.util.Collection;

import android.util.Log;

/**
 * @brief This static class is for debugging
 * @todo Add debug outputs for more data types
 */
public class Debug
{
	private static long timeseed = 0L;
	private final static String LOG_TAG = "DEBUG";
	private static boolean DEBUG = true;

	/**
	 * Sets if the app is in debug mode.
	 *
	 * @param inDebug
	 *            If set to true then outputs will be made, else they wont
	 */
	public static void setDebugMode(boolean inDebug)
	{
		DEBUG = inDebug;
	}

	public static void seedTimer()
	{
		timeseed = System.currentTimeMillis();
	}

	public static void tickTimer()
	{
		out("Time: " + (System.currentTimeMillis() - timeseed));
		seedTimer();
	}

	public static String getCallingStack()
	{
		Throwable fakeException = new Throwable();
		StackTraceElement[] stackTrace = fakeException.getStackTrace();

		if (stackTrace != null && stackTrace.length >= 2)
		{
			String retstr = "";
			int index = 0;
			for (StackTraceElement s : stackTrace)
			{
				if (s != null)
				{
					retstr += index + " : " + s.getFileName() + " from " + s.getMethodName() + " on line " + s.getLineNumber() + "\n";
				}

				index++;
			}

			return retstr;
		}

		return null;
	}

	public static String getCallingMethodInfo()
	{
		Throwable fakeException = new Throwable();
		StackTraceElement[] stackTrace = fakeException.getStackTrace();

		if (stackTrace != null && stackTrace.length >= 2)
		{
			StackTraceElement s = stackTrace[2];
			if (s != null)
			{
				return s.getFileName() + "(" + s.getMethodName() + ":" + s.getLineNumber() + "):";
			}
		}

		return null;
	}

	private static void longInfo(String str)
	{
		if (str.length() > 4000)
		{
			Log.e(LOG_TAG, str.substring(0, 4000));
			longInfo(str.substring(4000));
		}
		else
		{
			Log.e(LOG_TAG, str);
		}
	}

	public static void out(Collection args)
	{
		if (!DEBUG)
			return;

		String output = "";

		for (Object o : args)
		{
			output += o.toString() + ", ";
		}

		longInfo(getCallingMethodInfo() + " " + output);
	}

	public static void out(Object... args)
	{
		if (!DEBUG)
			return;

		String output = "";

		for (Object o : args)
		{
			output += o.toString() + ", ";
		}

		longInfo(getCallingMethodInfo() + " " + output);
	}

	public static void out(String str, Object... args)
	{
		if (!DEBUG)
			return;

		longInfo(getCallingMethodInfo() + " " + String.format(str, args));
	}

	public static void out(Object obj)
	{
		if (!DEBUG)
			return;

		longInfo(getCallingMethodInfo() + " " + (obj == null ? "[null]" : obj.toString()));
	}

	public static void out(Exception obj)
	{
		if (!DEBUG)
			return;

		obj.printStackTrace();
	}

	/**
	 * Logs the heap size of the application and any allocation sizes
	 *
	 * @param mClass
	 *            The class of the application to check
	 */
	public static void logHeap(Class mClass)
	{
		logHeap("", mClass);
	}

	public static String getHeap(Class c)
	{
		Double allocated = new Double(android.os.Debug.getNativeHeapAllocatedSize()) / new Double((1048576));
		Double available = new Double(android.os.Debug.getNativeHeapSize() / 1048576.0);
		Double free = new Double(android.os.Debug.getNativeHeapFreeSize() / 1048576.0);
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		String s = "";
		s += System.currentTimeMillis() + " - DUMP";
		s += "\nMemory Heap Debug. ==============================================================================================================";
		s += "\nMemory Heap Native: Allocated " + df.format(allocated) + "MB of " + df.format(available) + "MB (" + df.format(free) + "MB free) in [" + c.getName() + "]";
		s += "\nMemory Heap App: Allocated: " + df.format(new Double(Runtime.getRuntime().totalMemory() / 1048576)) + "MB of " + df.format(new Double(Runtime.getRuntime().maxMemory() / 1048576)) + "MB (" + df.format(new Double(Runtime.getRuntime().freeMemory() / 1048576)) + "MB free)";
		s += "\nMemory Heap Debug. ==============================================================================================================";

		return s;
	}

	/**
	 * Logs the heap size of the application and any allocation sizes
	 *
	 * @param msg
	 *            A message to display
	 * @param mClass
	 *            The class of the application to check
	 */
	public static void logHeap(String msg, Class mClass)
	{
		if (!DEBUG)
			return;

		Double allocated = new Double(android.os.Debug.getNativeHeapAllocatedSize()) / new Double((1048576));
		Double available = new Double(android.os.Debug.getNativeHeapSize() / 1048576.0);
		Double free = new Double(android.os.Debug.getNativeHeapFreeSize() / 1048576.0);
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		Log.e("MEM", "" + System.currentTimeMillis() + " - DUMP: " + msg);
		Log.e("MEM", "Memory Heap Debug. ==============================================================================================================");
		Log.e("MEM", "Memory Heap Native: Allocated " + df.format(allocated) + "MB of " + df.format(available) + "MB (" + df.format(free) + "MB free) in [" + mClass.getName() + "]");
		Log.e("MEM", "Memory Heap App: Allocated: " + df.format(new Double(Runtime.getRuntime().totalMemory() / 1048576)) + "MB of " + df.format(new Double(Runtime.getRuntime().maxMemory() / 1048576)) + "MB (" + df.format(new Double(Runtime.getRuntime().freeMemory() / 1048576)) + "MB free)");
		Log.e("MEM", "Memory Heap Debug. ==============================================================================================================");
	}
}