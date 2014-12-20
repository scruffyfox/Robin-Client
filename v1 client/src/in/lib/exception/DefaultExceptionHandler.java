package in.lib.exception;

import in.lib.manager.UserManager;
import in.model.CrashReport;
import in.model.Settings;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Random;

public class DefaultExceptionHandler implements UncaughtExceptionHandler
{
	private UncaughtExceptionHandler defaultExceptionHandler;

	public DefaultExceptionHandler(UncaughtExceptionHandler pDefaultExceptionHandler)
	{
		defaultExceptionHandler = pDefaultExceptionHandler;
	}

	@Override public void uncaughtException(Thread t, Throwable e)
	{
		sendException(e);
		defaultExceptionHandler.uncaughtException(t, e);
	}

	public static void sendException(Throwable e)
	{
		sendException(e, "");
	}

	public static void sendException(Throwable e, String optionalMessage)
	{
		try
		{
			CrashReport report = new CrashReport();
			report.setException(e);
			report.setAdditionalMessage(optionalMessage);
			report.setModel(android.os.Build.MODEL);
			report.setManufacturer(android.os.Build.MANUFACTURER);
			report.setOsVersion(android.os.Build.VERSION.RELEASE);
			report.setTimestamp(System.currentTimeMillis());
			report.setDeviceId(Settings.DEVICE_ID);
			report.setPackageName(Settings.PACKAGE_NAME);
			report.setVersion(Settings.VERSION);
			report.setVersionCode(Settings.VERSION_CODE);
			report.setUserId(UserManager.getUserId());

			Random generator = new Random();
			int random = generator.nextInt(99999);
			String filename = Integer.toString(random) + ".stacktrace";

			ExceptionHandler.writeFile(ExceptionHandler.getInstance().getFilesPath(), filename, report);
		}
		catch (Exception ebos)
		{
			ebos.printStackTrace();
		}
	}
}