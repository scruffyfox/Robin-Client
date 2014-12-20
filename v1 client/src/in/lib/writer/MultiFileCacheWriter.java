package in.lib.writer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

public class MultiFileCacheWriter
{
	private Map<String, Object> writeQueue = Collections.synchronizedMap(new HashMap<String, Object>());

	public void scheduleAsyncWrite(String filename, Object contents)
	{
		if (!TextUtils.isEmpty(filename) && contents != null)
		{
			writeQueue.put(filename, contents);
		}
	}

	public void executeAsyncWriteList()
	{
		CacheWriter writer = new CacheWriter(writeQueue.keySet());
		writer.write(writeQueue.values().toArray(new Object[writeQueue.size()]));
		writeQueue.clear();
	}
}