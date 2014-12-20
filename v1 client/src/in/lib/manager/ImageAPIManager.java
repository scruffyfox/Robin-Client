package in.lib.manager;

import in.lib.Debug;
import in.model.DraftPost;
import in.model.User;
import in.obj.Auth;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.obj.entity.MultiPartEntity;
import net.callumtaylor.asynchttp.response.AsyncHttpResponseHandler;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import com.google.gson.JsonObject;

/**
 * Handles all api calls
 * @author CallumTaylor
 */
public class ImageAPIManager
{
	public enum Provider
	{
		APPNET(4, "App.net", APIManager.API_URL + APIManager.API_STREAM + APIManager.API_VERSION + "files", "", 20),
		IMGLY(1, "Imgly", "http://img.ly/api/2/upload.json", "", 22),
		BLIMS(3, "Blims", "http://bli.ms/api/upload/", "", 22);
		//FTP("", "");

		@Getter private final int id;
		@Getter private final String url;
		@Getter private final String clientToken;
		@Getter private final String name;
		private final int urlLength;

		private Provider(int id, String name, String url, String clientToken, int urlLength)
		{
			this.id = id;
			this.name = name;
			this.url = url;
			this.clientToken = clientToken;
			this.urlLength = urlLength;
		}

		public int getUrlLength()
		{
			if (this == APPNET)
			{
				return urlLength + SettingsManager.getPostIdLength();
			}

			return urlLength;
		}

		public static Provider getProviderById(int id)
		{
			for (Provider v : values())
			{
				if (v.getId() == id) return v;
			}

			return null;
		}
	}

	private static volatile ImageAPIManager mAPIManager;

	public static ImageAPIManager getInstance()
	{
		if (mAPIManager == null)
		{
			mAPIManager = new ImageAPIManager();
		}

		return mAPIManager;
	}

	public void registerForToken(final Context c, final User user)
	{
		MultiPartEntity params = new MultiPartEntity();
		params.addPart("grant_type", "delegate");
		params.addPart("delegate_client_id", SettingsManager.getImageProvider().getClientToken());
		final HashMap<String, Auth> auths = UserManager.getAuths(c);

		AsyncHttpClient client = new AsyncHttpClient(APIManager.API_AUTH + "?access_token=" + auths.get(user.getId()).getAccessToken());
		client.post(params, new JsonResponseHandler()
		{
			@Override public void onSuccess()
			{
				JsonObject response = getContent().getAsJsonObject();
				String token = response.get("delegate_token").getAsString();
				auths.get(user.getId()).getImageDelegateToken().put(SettingsManager.getImageProvider(), token);

				if (c != null)
				{
					UserManager.setAuths(c, auths);
				}
			}
		});
	}

	public void blimsSetPostThread(String code, String threadId, AsyncHttpResponseHandler response)
	{
		AsyncHttpClient client = new AsyncHttpClient(SettingsManager.getImageProvider().getUrl() + "update/");
		MultiPartEntity params = new MultiPartEntity();
		params.addPart("post_id", threadId);
		params.addPart("short_code", code);
		client.post(params, response);
	}

	public void uploadImage(Context c, DraftPost holder, User u, int orientation, AsyncHttpResponseHandler response)
	{
		try
		{
			final HashMap<String, Auth> auths = UserManager.getAuths(c);
			if (SettingsManager.getImageProvider() == Provider.IMGLY || SettingsManager.getImageProvider() == Provider.BLIMS)
			{
				if (TextUtils.isEmpty(auths.get(u.getId()).getImageDelegateToken().get(SettingsManager.getImageProvider())))
				{
					registerForToken(c, u);
					response.onFailure();
					return;
				}
			}

			// send the image
			MultiPartEntity params = new MultiPartEntity();

			if (SettingsManager.getImageProvider() == Provider.BLIMS)
			{
				params.addPart("app_key", "7fa0bee0a0a8cafa8a100580954436f1");
				params.addPart("text", holder.getPostText());
			}

			List<Header> headers = new ArrayList<Header>();
			if (SettingsManager.getImageProvider() == Provider.APPNET)
			{
				params.addPart("type", "robin.image.photo");
				params.addPart("kind", "image");
				headers.add(new BasicHeader("Authorization", "BEARER " + auths.get(u.getId()).getAccessToken()));

				params.addPart("content", getImageBody(c, holder.getImagePath()));
			}
			else
			{
				headers.add(new BasicHeader("Identity-Delegate-Token", auths.get(u.getId()).getImageDelegateToken().get(SettingsManager.getImageProvider())));
				headers.add(new BasicHeader("Identity-Delegate-Endpoint", "https://alpha-api.app.net/stream/0/token"));

				params.addPart("media", getImageBody(c, holder.getImagePath()));
			}

			AsyncHttpClient client = new AsyncHttpClient(SettingsManager.getImageProvider().getUrl());
			client.post(params, headers, response);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public ContentBody getImageBody(Context c, String path)
	{
		Uri imageUri = Uri.parse(path);

		if (imageUri.getScheme().startsWith("content"))
		{
			try
			{
				ParcelFileDescriptor parcelFileDescriptor = c.getContentResolver().openFileDescriptor(imageUri, "r");
				final long size = parcelFileDescriptor.getStatSize();
				parcelFileDescriptor.close();
				InputStream fs = c.getContentResolver().openInputStream(imageUri);

				return new InputStreamBody(fs, "image/jpeg", "image.jpg")
				{
					@Override public long getContentLength()
					{
						return size;
					}
				};
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (imageUri.getScheme().startsWith("file"))
		{
			return new FileBody(new File(imageUri.getPath()), "image/jpeg");
		}

		return null;
	}
}