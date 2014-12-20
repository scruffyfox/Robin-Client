package in.lib.exception;

import in.lib.Debug;
import in.lib.manager.CacheManager;
import in.model.CrashReport;
import in.model.Settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lombok.Getter;
import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.obj.entity.JsonEntity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Secure;

public class ExceptionHandler
{
	@Getter private String reportUrl = "localhost";
	@Getter private String filesPath = "/";
	private String[] stackTraceFileList = null;

	private static ExceptionHandler instance;
	public static ExceptionHandler getInstance()
	{
		if (instance == null)
		{
			synchronized (ExceptionHandler.class)
			{
				if (instance == null)
				{
					instance = new ExceptionHandler();
				}
			}
		}

		return instance;
	}

	/**
	 * @return Gets the base 64 rehashable id of the device
	 */
	public String getDeviceId(Context c)
	{
		// Create re-hashable password for the device
		String deviceId = Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
		deviceId = deviceId == null ? "NOID" + System.currentTimeMillis() : deviceId;

		// Now we hash it and b64 it
		String deviceHash = CacheManager.getHash(deviceId);
		return deviceHash;
	}

	public void register(Context context, String url)
	{
		reportUrl = url;

		try
		{
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			Settings.PACKAGE_NAME = pi.packageName;
			Settings.VERSION = "" + pi.versionName;
			Settings.VERSION_CODE = "" + pi.versionCode;
			Settings.DEVICE_ID = getDeviceId(context);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		try
		{
			filesPath = context.getExternalCacheDir().getAbsolutePath() + "/crash_reports";
		}
		catch (Exception e)
		{
			filesPath = context.getFilesDir().getAbsolutePath() + "/crashes/";
		}

		new Thread()
		{
			@Override public void run()
			{
				submitStackTraces();
				UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();

				// don't register again if already registered
				if (!(currentHandler instanceof DefaultExceptionHandler))
				{
					Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(currentHandler));
				}
			}
		}.start();
	}

	/**
	 * Forces a manual exception post
	 * @param e
	 */
	public static void sendException(Exception e)
	{
		DefaultExceptionHandler.sendException(e, "CAUGHT EXCEPTION");
	}

	/**
	 * Search for stack trace files.
	 *
	 * @return
	 */
	private String[] searchForStackTraces()
	{
		try
		{
			if (stackTraceFileList != null)
			{
				return stackTraceFileList;
			}

			File dir = new File(filesPath);

			// Try to create the files folder if it doesn't exist
			dir.mkdir();

			FilenameFilter filter = new FilenameFilter()
			{
				@Override public boolean accept(File dir, String name)
				{
					return name.endsWith(".stacktrace");
				}
			};

			return (stackTraceFileList = dir.list(filter));
		}
		catch (Exception e)
		{
			return new String[]{};
		}
	}

	/**
	 * Look into the files folder to see if there are any "*.stacktrace" files.
	 * If any are present, submit them to the trace server.
	 */
	public void submitStackTraces()
	{
		String[] list = searchForStackTraces();

		try
		{
			if (list != null && list.length > 0)
			{
				for (int i = 0; i < list.length; i++)
				{
					String filename = list[i];
					final CrashReport report = readFileAsObject(filesPath, filename, CrashReport.class);

					boolean result = new Handler(Looper.getMainLooper()).post(new Runnable()
					{
						@Override public void run()
						{
							try
							{
								AsyncHttpClient c = new AsyncHttpClient(reportUrl);
								JsonEntity entity = report.toEntity();

								c.post(entity, null);
							}
							catch (Exception e)
							{
								Debug.out(e);
							}
						}
					});

					File file = new File(filesPath, filename);
					file.delete();
				}
			}
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public static <T> T readFileAsObject(String filesPath, String fileName, Class<T> outClass)
	{
		try
		{
			return outClass.cast(desterializeObject(readFile(new FileInputStream(new File(filesPath, fileName)))));
		}
		catch (Exception e)
		{
			Debug.out(e);
			return null;
		}
	}

	public static void writeFile(String filesPath, String fileName, Serializable contents)
	{
		FileOutputStream fos = null;
		try
		{
			if (fos == null)
			{
				File f = new File(filesPath, fileName);
				fos = new FileOutputStream(f);
			}

			fos.write(serializeObject(contents));
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public static byte[] readFile(InputStream input)
	{
		ByteArrayOutputStream bos = null;

		try
		{
			bos = new ByteArrayOutputStream(8192);

			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];

			int len = 0;
			while ((len = input.read(buffer)) > 0)
			{
				bos.write(buffer, 0, len);
			}

			return bos.toByteArray();
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
		finally
		{
			try
			{
				input.close();
				bos.close();
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	public static Object desterializeObject(byte[] data)
	{
		try
		{
			ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(data));
			Object objectData = input.readObject();
			input.close();

			return objectData;
		}
		catch (Exception e)
		{
			Debug.out(e);
			return null;
		}
	}

	public static byte[] serializeObject(Object data)
	{
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(data);
			byte[] yourBytes = bos.toByteArray();

			return yourBytes;
		}
		catch (Exception e)
		{
			Debug.out(e);
			return null;
		}
	}
}