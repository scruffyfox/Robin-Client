package in.rob.client.fragment.settings;

import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.Iterator;

import in.lib.utils.Debug;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.rob.client.R;
import in.rob.client.fragment.base.BaseFragment;

@Injectable
public class AdditionalSettingsFragment extends BaseFragment
{
	@InjectView private TextView imageCacheText;
	@InjectView private TextView imageCacheMemoryText;
	@InjectView private TextView fileCacheText;
	@InjectView private TextView buildNumber;
	@InjectView private TextView version;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.additional_settings_view, container, false);
		Views.inject(this, view);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		try
		{
			imageCacheText.setText(getString(R.string.currently) + ": " + calculateFolderCacheSize(getContext().getExternalCacheDir().getAbsolutePath() + "/uil-images/"));
			imageCacheMemoryText.setText(getString(R.string.currently) + ": " + calculateMemoryCacheSize());
			fileCacheText.setText(getString(R.string.currently) + ": " + calculateFolderCacheSize(getContext().getFilesDir().getAbsolutePath()));
			buildNumber.setText(String.valueOf(getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode));
			version.setText(getString(R.string.version) + " " + getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public String calculateMemoryCacheSize()
	{
		try
		{
			long size = 0;

			MemoryCacheAware<String, Bitmap> memoryCache = ImageLoader.getInstance().getMemoryCache();
			Iterator<String> iterator = memoryCache.keys().iterator();

			while (iterator.hasNext())
			{
				String key = iterator.next();

				if (VERSION.SDK_INT >= VERSION_CODES.KITKAT)
				{
					size += memoryCache.get(key).getAllocationByteCount();
				}
				else
				{
					size += memoryCache.get(key).getRowBytes() * memoryCache.get(key).getHeight();
				}
			}

			return Math.floor((size / 1024.0d / 1024.0d) * 100.0d) / 100.0d + "MB";
		}
		catch (Exception e)
		{
			return "unavailable";
		}
	}

	public String calculateFolderCacheSize(String path)
	{
		try
		{
			File f = new File(path);
			File[] files = f.listFiles();
			long size = 0;

			for (File file : files)
			{
				size += file.length();
			}

			return Math.floor((size / 1024.0d / 1024.0d) * 100.0d) / 100.0d + "MB";
		}
		catch (Exception e)
		{
			return "unavailable";
		}
	}
}
