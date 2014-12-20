package in.lib.handler.base;

import in.lib.Debug;
import net.callumtaylor.asynchttp.response.AsyncHttpResponseHandler;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * A modification to {@link JsonResponseHandler} to catch malformed json responses
 */
public abstract class ResponseHandler extends AsyncHttpResponseHandler
{
	private StringBuffer stringBuffer;

	@Override public void onPublishedDownloadProgress(byte[] chunk, int chunkLength, long totalProcessed, long totalLength)
	{
		if (stringBuffer == null)
		{
			int total = (int)(totalLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalLength);
			stringBuffer = new StringBuffer(Math.max(total, 1024 * 8));
		}

		if (chunk != null)
		{
			try
			{
				stringBuffer.append(new String(chunk, 0, chunkLength, "UTF-8"));
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}
	}

	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @return The data represented as a gson JsonElement primitive type
	 */
	@Override public JsonElement getContent()
	{
		try
		{
			return new JsonParser().parse(stringBuffer.toString());
		}
		catch (Exception e)
		{
			Debug.out(e);
			return null;
		}
		finally
		{
			stringBuffer = null;
		}
	}
}