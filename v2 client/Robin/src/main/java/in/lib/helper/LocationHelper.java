/**
 * @brief x lib is the library which includes the commonly used functions in 3 Sided Cube Android applications
 *
 * @author Callum Taylor
 **/
package in.lib.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import java.util.List;

/**
 * @brief This class is used to fetch the user's current location using either
 *        cached location (if available) or requests it using the
 *        LocationListener if not
 */
public class LocationHelper implements LocationListener
{
  /**
	 * Message ID: Used when a provider has been disabled
	 */
	public static final int MESSAGE_PROVIDER_DISABLED = 0;
	/**
	 * Message ID: Used when the search has timed out
	 */
	public static final int MESSAGE_TIMEOUT = 1;
	/**
	 * Message ID: Used when the user cancels the request
	 */
	public static final int MESSAGE_FORCED_CANCEL = 2;

	private final Context mContext;
	private final LocationManager mLocationManager;
	private final long mTimeout = 0;
	private LocationResponse mCallback = null;
	private Accuracy mAccuracy = Accuracy.FINE;
	private final Handler mTimeoutHandler = new Handler();
	private float mAccuracyFloat = 30.0f;

	private final Runnable mTimeoutRunnable = new Runnable()
	{
		@Override public void run()
		{
			mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
			mLocationManager.removeUpdates(LocationHelper.this);
			stopFetch();

			if (mCallback != null)
			{
				mCallback.onLocationFailed("Timeout", MESSAGE_TIMEOUT);
				mCallback.onTimeout();
			}
		};
	};

	/**
	 * Determines the accuracy of the fetch
	 */
	public enum Accuracy
	{
		/**
		 * Get the location as close to the real point as possible
		 */
		FINE,

		/**
		 * Get the location by any means
		 */
		COARSE;
	}

	/**
	 * Default Constructor
	 *
	 * @param context
	 *            The application/activity context to use
	 */
	public LocationHelper(Context context)
	{
		mContext = context;
		mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * Cancels the request
	 */
	public void cancelRequest()
	{
		mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
		mLocationManager.removeUpdates(this);

		if (mCallback != null)
		{
			mCallback.onLocationFailed("Canceled", MESSAGE_FORCED_CANCEL);
		}
	}

	/**
	 * Sets the desired accuracy for the fetch
	 *
	 * @param accuracy
	 *            The new accuracy
	 */
	public void setAccuracy(float accuracy)
	{
		mAccuracyFloat = accuracy;
	}

	/**
	 * Gets the current set desired accuracy for a fetch
	 *
	 * @return The accuracy in meters
	 */
	public float getAccuracy()
	{
		return mAccuracyFloat;
	}

	/**
	 * Fetches the location using Fine accuracy. Note: if the response returns
	 * location fetch failed, use the helper to get the cached location, then
	 * finally fail if that is null
	 *
	 * @param timeout
	 *            The time out for the request in MS
	 * @param callback
	 *            The callback for the request
	 */
	public void fetchLocation(long timeout, LocationResponse callback)
	{
		fetchLocation(timeout, Accuracy.FINE, callback);
	}

	/**
	 * Fetches the location
	 *
	 * @param timeout
	 *            The time out for the request in MS
	 * @param accuracy
	 *            The accuracy of the fetch
	 * @param callback
	 *            The callback for the request
	 */
	public void fetchLocation(long timeout, Accuracy accuracy, LocationResponse callback)
	{
		mCallback = callback;
		mCallback.onRequest();
		mAccuracy = accuracy;
		Location userLocation = null;

		// Try to get the cache location first
		userLocation = getCachedLocation();
		// if (userLocation == null)
		{
			if (timeout > 0)
			{
				mTimeoutHandler.postDelayed(mTimeoutRunnable, timeout);
			}

			try
			{
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets the cached location
	 *
	 * @return The location, or null if one was not retrieved
	 */
	public Location getCachedLocation()
	{
		List<String> providers = mLocationManager.getProviders(true);
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--)
		{
			Location loc = mLocationManager.getLastKnownLocation(providers.get(i));

			if (l == null || (loc != null && loc.getAccuracy() < l.getAccuracy()))
			{
				l = loc;
			}
		}

		return l;
	}

	private boolean hasAcquired = false;

	@Override public void onLocationChanged(Location location)
	{
		if (location != null)
		{
			if (mCallback != null)
			{
				mCallback.onLocationChanged(location);

				if (mAccuracy == Accuracy.FINE && (!location.hasAccuracy() || location.getAccuracy() > mAccuracyFloat))
				{
					return;
				}

				if (!hasAcquired)
				{
					mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
					mLocationManager.removeUpdates(this);
					mCallback.onLocationAcquired(location);
					hasAcquired = true;
				}
			}
		}
	}

	public void stopFetch()
	{
		mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
		mLocationManager.removeUpdates(this);
	}

	@Override public void onProviderDisabled(String provider)
	{
		List<String> providers = mLocationManager.getProviders(true);
		boolean allOn = false;

		for (int i = providers.size() - 1; i >= 0; i--)
		{
			allOn |= Settings.Secure.isLocationProviderEnabled(mContext.getContentResolver(), providers.get(i));
		}

		if (!allOn)
		{
			mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
			mLocationManager.removeUpdates(this);

			if (mCallback != null)
			{
				mCallback.onLocationFailed("All providers disabled", MESSAGE_PROVIDER_DISABLED);
			}
		}
	}

	@Override public void onProviderEnabled(String provider)
	{
	}

	@Override public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}

	/**
	 * @brief The location response for the callback of the LocationHelper
	 */
	public static abstract class LocationResponse
	{
		/**
		 * Called when the request was initiated
		 */
		public void onRequest()
		{
		}

		/**
		 * Called when the location changes
		 *
		 * @param l
		 *            The new location
		 */
		public void onLocationChanged(Location l)
		{
		}

		/**
		 * Called when the location was acquired
		 *
		 * @param l
		 *            The location received
		 */
		public abstract void onLocationAcquired(Location l);

		/**
		 * Called when the request timed out
		 */
		public void onTimeout()
		{
		}

		/**
		 * Called when the request failed
		 *
		 * @param message
		 *            The message
		 * @param messageId
		 *            The ID of the message
		 */
		public void onLocationFailed(String message, int messageId)
		{
		}
	}
}