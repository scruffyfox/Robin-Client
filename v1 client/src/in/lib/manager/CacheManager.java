package in.lib.manager;

import in.lib.Debug;
import in.lib.writer.CacheWriter;
import in.lib.writer.CacheWriter.WriterListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import android.util.Base64;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CacheManager
{
	@Getter @Setter private String cachePath;
	private static CacheManager instance;
	private static final String CACHE_PREFIX = "cache_";
	private long lastCheckTime = System.currentTimeMillis();

	/**
	 * Gets the cache manager instance or creates it if it's null
	 * @return
	 */
	public static CacheManager getInstance()
	{
		if (instance == null)
		{
			instance = new CacheManager();
		}

		return instance;
	}

	public CacheManager()
	{

	}

	private Object lock = new Object();
	public void checkLimit()
	{
		synchronized (lock)
		{
			if (System.currentTimeMillis() - lastCheckTime > 5000)
			{
				lastCheckTime = System.currentTimeMillis();

				long currentCount = 0L;
				long limitBytes = SettingsManager.getMaxCacheSize() * 1024 * 1024;
				List<File> files = Collections.synchronizedList(Arrays.asList(new File(cachePath).listFiles()));

				synchronized (files)
				{
					final Map<File, Long> fileList = new HashMap<File, Long>();
					for (final File f : files)
					{
						fileList.put(f, f.lastModified());
					}

					Collections.sort(files, new Comparator<File>()
					{
						@Override public int compare(final File f1, final File f2)
						{
							if (fileList.get(f1) < fileList.get(f2))
							{
								return +1;
							}
							else if (fileList.get(f1) > fileList.get(f2))
							{
								return -1;
							}
							else
							{
								return 0;
							}
						}
					});

					for (File f : files)
					{
						currentCount += f.length();

						if (currentCount > limitBytes)
						{
							f.delete();
						}
					}
				}
			}
		}
	}

	public boolean fileExists(String file)
	{
		if (file.startsWith("/"))
		{
			return new File(file).exists();
		}

		return new File(cachePath + "/" + CACHE_PREFIX + file).exists();
	}

	/**
	 * Gets the modified date of the file
	 * @param fileName The file
	 * @return The modified date in ms since 1970 (EPOCH)
	 */
	public long fileModifiedDate(String fileName)
	{
		File f;

		if (fileName.startsWith("/"))
		{
			f = new File(fileName);
		}
		else
		{
			f = new File(cachePath + "/" + CACHE_PREFIX + fileName);
		}

		return f.lastModified();
	}

	/**
	 * Checks if a file was created before a certain date
	 * @param fileName The file to check
	 * @param date The date to check against
	 * @return True if the file is older, false if not
	 */
	public boolean fileOlderThan(String fileName, long date)
	{
		long lastDate = fileModifiedDate(fileName);

		if (lastDate > date)
		{
			return false;
		}

		return true;
	}

	public boolean removeFile(String file)
	{
		if (file.startsWith("/"))
		{
			return new File(file).delete();
		}

		return new File(cachePath + "/" + CACHE_PREFIX + file).delete();
	}

	public <T> T readFileAsObject(String file, Class<T> out)
	{
		try
		{
			File f;

			if (file.startsWith("/"))
			{
				f = new File(file);
			}
			else
			{
				f = new File(cachePath + "/" + CACHE_PREFIX + file);
			}

			Kryo kryo = new Kryo();
			kryo.setReferences(false);

			InputStream fis = new BufferedInputStream(new FileInputStream(f), 8196);
			Input input = new Input(fis, Math.max(1024 * 8, fis.available()));
			T obj = kryo.readObject(input, out);
			input.close();
			fis.close();
			kryo = null;

			return obj;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	public <T> T readFileAsObject(String file, T def)
	{
		Object obj = readFileAsObject(file, def.getClass());
		return (T)(obj == null ? def : obj);
	}

	public byte[] serialize(Object contents)
	{
		if (contents == null) return null;

		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 8);

		try
		{
			Kryo kryo = new Kryo();
			kryo.setReferences(false);

			Output output = new Output(bos);
			kryo.writeObject(output, contents);
			output.close();
			kryo = null;

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
				bos.close();
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	public <T> T deserialize(byte[] contents, Class<T> outClass)
	{
		if (contents == null) return null;

		Kryo kryo = new Kryo();
		kryo.setReferences(false);

		Input input = new Input(contents);
		T obj = kryo.readObject(input, outClass);
		input.close();
		kryo = null;

		return obj;
	}

	public void writeFile(String fileName, Object contents)
	{
		try
		{
			File f;

			if (fileName.startsWith("/"))
			{
				f = new File(fileName);
			}
			else
			{
				f = new File(cachePath + "/" + CACHE_PREFIX + fileName);
			}

			Kryo kryo = new Kryo();
			kryo.setReferences(false);

			OutputStream fos = new BufferedOutputStream(new FileOutputStream(f), 8196);
			Output output = new Output(fos);
			kryo.writeObject(output, contents);
			output.close();
			fos.close();
			kryo = null;

			// not sure how badly this may affect performance
			checkLimit();
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public void asyncWriteFile(String filename, Object contents)
	{
		CacheWriter writer = new CacheWriter(filename);
		writer.write(contents);
	}

	public void asyncWriteFile(String filename, Object contents, WriterListener listener)
	{
		CacheWriter writer = new CacheWriter(filename);
		writer.setWriterListener(listener);
		writer.write(contents);
	}

	/**
	 * Gets a base64'd MD5 hash of an input string
	 *
	 * @param input
	 *            The input string
	 * @return The base64 MD5 hash of the input string
	 */
	public static String getHash(String input)
	{
		String hashFileName = "";

		try
		{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			hashFileName = Base64.encodeToString(md5.digest(input.getBytes()), Base64.DEFAULT).replace('/', '.');
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return hashFileName;
	}

	/**
	 * @brief The class that serailizes data
	 */
	public static class Serializer
	{
		/**
		 * Serializes data into bytes
		 * @param data The data to be serailized
		 * @return The serialized data in a byte array
		 */
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

		/**
		 * Deserailizes data into an object
		 * @param data The byte array to be deserialized
		 * @return The data as an object
		 */
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
	}
}