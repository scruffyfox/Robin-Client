package in.lib.manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import in.data.TSerializable;
import in.lib.utils.Debug;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class CacheManager
{
	@Getter @Setter private static String cachePath = "";
	private static CacheManager instance;

	public static CacheManager getInstance()
	{
		if (instance == null)
		{
			synchronized (CacheManager.class)
			{
				if (instance == null)
				{
					instance = new CacheManager();
				}
			}
		}

		return instance;
	}

	public long getFileAge(String fileName)
	{
		return System.currentTimeMillis() - new File(cachePath, fileName).lastModified();
	}

	public boolean fileExists(String fileName)
	{
		return new File(cachePath, fileName).exists();
	}

	public boolean fileExists(String path, String fileName)
	{
		return new File(path, fileName).exists();
	}

	public boolean removeFile(String fileName)
	{
		return removeFile(cachePath, fileName);
	}

	public boolean removeFile(String folderPath, String fileName)
	{
		File f = new File(folderPath + "/" + fileName);
		return f.delete();
	}

	public void writeFile(String fileName, TSerializable object)
	{
		writeFile(cachePath, fileName, object);
	}

	public void writeFile(String folderPath, String fileName, TSerializable object)
	{
		try
		{
			RandomAccessFile file = new RandomAccessFile(folderPath + "/" + fileName, "rw");
			FileOutputStream fos = new FileOutputStream(file.getFD());
			BufferedOutputStream bos = new BufferedOutputStream(fos, 1024 * 8);
			DataOutputStream dos = new DataOutputStream(bos);

			object.writeToBuffer(dos);

			bos.flush();
			fos.close();
			dos.close();
			bos.close();
			file.close();
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public <T extends TSerializable> T readFile(String fileName, @NonNull Class<T> cls)
	{
		return readFile(cachePath, fileName, cls);
	}

	public <T extends TSerializable> T readFile(String fileName, @NonNull T object)
	{
		return readFile(cachePath, fileName, object);
	}

	public <T extends TSerializable> T readFile(String folderPath, String fileName, @NonNull Class<T> cls)
	{
		try
		{
			T data = cls.newInstance();
			return readFile(folderPath, fileName, data);
		}
		catch (InstantiationException e)
		{
			Debug.out(e);
		}
		catch (IllegalAccessException e)
		{
			Debug.out(e);
		}

		return null;
	}

	public <T extends TSerializable> T readFile(String folderPath, String fileName, @NonNull T object)
	{
		if (fileExists(folderPath, fileName))
		{
			try
			{
				RandomAccessFile file = new RandomAccessFile(cachePath + "/" + fileName, "rw");
				FileInputStream fis = new FileInputStream(file.getFD());
				BufferedInputStream bis = new BufferedInputStream(fis, 1024 * 8);
				DataInputStream dis = new DataInputStream(bis);

				object.readFromBuffer(dis);

				fis.close();
				dis.close();
				bis.close();
				file.close();

				return object;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}
}