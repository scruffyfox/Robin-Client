package in.lib.utils;

import android.net.Uri;

import java.util.List;

/**
 * URLUtils is used for getting images for things such as youtube videos, maps
 * It is also used to convert special image links into the absolute image path
 */
public class URLUtils
{
	public static Boolean isImage(Uri uri)
	{
		List<String> segments = uri.getPathSegments();

		return 	segments.size() > 0 && (hasImageExt(uri)) ||
				(containsIgnoreCase(uri.getHost(), "cl.ly") && uri.getPathSegments().size() > 0) ||
				(containsIgnoreCase(uri.getHost(), "imgur.com") && uri.getPathSegments().size() > 0 && !uri.getPathSegments().get(0).equals("a") && !uri.getPathSegments().get(0).equals("gallery")) ||
				(containsIgnoreCase(uri.getHost(), "i.rbn.im") && segments.size() > 0) ||
				(containsIgnoreCase(uri.getHost(), "d.pr") && segments.size() > 1) ||
				(containsIgnoreCase(uri.getHost(), "img.ly") && segments.size() > 0) ||
				(containsIgnoreCase(uri.getHost(), "bli.ms") && segments.size() > 0 && IntegerUtils.parseInt(uri.getPathSegments().get(0)) > 0) ||
				(containsIgnoreCase(uri.getHost(), "instagram.com") && segments.size() > 1) ||
				(containsIgnoreCase(uri.getHost(), "instagr.am") && segments.size() > 1);
	}

	public static boolean containsIgnoreCase(String str, String cmp)
	{
		return str.toLowerCase().contains(cmp.toLowerCase());
	}

	public static Boolean hasImageExt(Uri url)
	{
		return url.getLastPathSegment().endsWith(".jpg") ||
				url.getLastPathSegment().endsWith(".jpeg") ||
				url.getLastPathSegment().endsWith(".png") ||
				url.getLastPathSegment().endsWith(".gif");
	}

	public static Boolean isYoutubeVideo(Uri uri)
	{
		return 	(uri.getHost().endsWith("youtube.com") && uri.getQueryParameter("v") != null) ||
				(uri.getHost().endsWith("youtu.be") && uri.getPathSegments().size() > 0);
	}

	public static String getYoutubeThumbnail(Uri uri)
	{
		String videoId;

		if (uri.getHost().endsWith("youtube.com"))
		{
			videoId = uri.getQueryParameter("v");
		}
		else if (uri.getHost().endsWith("youtu.be"))
		{
			videoId = uri.getPathSegments().get(0);
		}
		else
		{
			return uri.toString();
		}

		return String.format("http://img.youtube.com/vi/%s/mqdefault.jpg", videoId);
	}

	public static Boolean isMap(Uri uri)
	{
		return 	uri.getHost().contains("maps.google") &&
				uri.getQueryParameter("q") != null;
	}

	public static String getMapThumbnail(double lat, double lng)
	{
		return getMapThumbnail(lat, lng, 600, 300);
	}

	public static String getMapThumbnail(double lat, double lng, int width, int height)
	{
		return "http://maps.googleapis.com/maps/api/staticmap?center=" + (lat + "," + lng) + "&zoom=15&size=" + width + "x" + height + "&sensor=false&maptype=roadmap&markers=color:red%7Ccolor:red%7Clabel:A%7C" + (lat + "," + lng);
	}

	public static String getMapThumbnail(Uri uri)
	{
		String loc = uri.getQueryParameter("q");
		return String.format("http://maps.googleapis.com/maps/api/staticmap?center=%s&zoom=13&size=600x300&sensor=false", loc);
	}

	public static String fixInlineImage(Uri uri)
	{
		if (hasImageExt(uri)) return uri.toString();

		if (containsIgnoreCase(uri.getHost(), "d.pr") && uri.getPathSegments().size() > 1)
		{
			return "http://" + uri.getHost().replace("www.", "") + uri.getPath().replace("+", "") + ".png?" + uri.getQuery();
		}
		else if (containsIgnoreCase(uri.getHost(), "img.ly") && uri.getPathSegments().size() > 0)
		{
			String stub = uri.getLastPathSegment();
			return "http://img.ly/show/full/" + stub;
		}
		else if (containsIgnoreCase(uri.getHost(), "cl.ly") && uri.getPathSegments().size() > 0)
		{
			return uri.toString() + "/content";
		}
		else if (containsIgnoreCase(uri.getHost(), "imgur.com") && uri.getPathSegments().size() > 0 && !uri.getPathSegments().get(0).equals("a"))
		{
			return "http://i.imgur.com/" + uri.getLastPathSegment() + ".png";
		}
		else if (containsIgnoreCase(uri.getHost(), "bli.ms") && uri.getPathSegments().size() > 0)
		{
			return "http://bli.ms/" + uri.getLastPathSegment() + "_thumb.jpg";
		}
		else if (containsIgnoreCase(uri.getHost(), "instagram.com") || containsIgnoreCase(uri.getHost(), "instagr.am"))
		{
			if (uri.getPathSegments().get(0).equals("p"))
			{
				String stub = uri.getPathSegments().get(uri.getPathSegments().size() - 1);
				return "http://instagr.am/p/" + stub + "/media/?size=m";
			}
		}
		else
		{
			return uri.toString();
		}

		return "";
	}
}