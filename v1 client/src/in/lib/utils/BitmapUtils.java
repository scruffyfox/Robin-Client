/**
 * @brief x util is the utility library which includes the method extentions for common data types
 *
 * @author Callum Taylor
**/
package in.lib.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;

/**
 * @brief Utilities for bitmap processing and manipulation
 */
public class BitmapUtils
{
	public static int FLIP_HORIZONTAL = 0x01;
	public static int FLIP_VERTICAL = 0x10;

	//	Orientation vars
	public static final int ORIENTATION_HORIZONTAL = 2;
	public static final int ORIENTATION_180_ROTATE_LEFT = 3;
	public static final int ORIENTATION_VERTICAL_FLIP = 4;
	public static final int ORIENTATION_VERTICAL_FLIP_90_ROTATE_RIGHT = 5;
	public static final int ORIENTATION_90_ROTATE_RIGHT = 6;
	public static final int ORIENTATION_HORIZONTAL_FLIP_90_ROTATE_RIGHT = 7;
	public static final int ORIENTATION_90_ROTATE_LEFT = 8;

	private static boolean mRecycleBitmaps = true;
	private static Config mBitmapConfig = Config.ARGB_8888;

	/**
	 * Set if the utils library recycles the bitmaps after processing
	 * @param recycleBitmaps
	 */
	public static void setRecycleBitmaps(boolean recycleBitmaps)
	{
		mRecycleBitmaps = recycleBitmaps;
	}

	public static void setConfig(Config config)
	{
		mBitmapConfig = config;
	}

	/**
	 * Recursivly samples an image to below or equal the max width/height
	 * @param path The path to the image
	 * @param maxWidth The maximum width the image can be
	 * @param maxHeight The maximum height the image can be
	 * @return The scale size of the image to use with {@link #BitmapFactory.Options()}
	 */
	public static int recursiveSample(String path, int maxWidth, int maxHeight)
	{
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		int scale = 1;
		int imageWidth = options.outWidth;
		int imageHeight = options.outHeight;

		while (imageWidth / 2 >= maxWidth || imageHeight / 2 >= maxHeight)
		{
			imageWidth /= 2;
			imageHeight /= 2;
			scale *= 2;
		}

		if (scale < 1)
		{
			scale = 1;
		}

		return scale;
	}

	/**
	 * Recursivly samples an image to below or equal the max width/height
	 * @param path The path to the image
	 * @param maxWidth The maximum width the image can be
	 * @param maxHeight The maximum height the image can be
	 * @return The scale size of the image to use with {@link #BitmapFactory.Options()}
	 */
	public static int recursiveSample(FileDescriptor fd, int maxWidth, int maxHeight)
	{
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fd, null, options);

		int scale = 1;
		int imageWidth = options.outWidth;
		int imageHeight = options.outHeight;

		while (imageWidth / 2 >= maxWidth || imageHeight / 2 >= maxHeight)
		{
			imageWidth /= 2;
			imageHeight /= 2;
			scale *= 2;
		}

		if (scale < 1)
		{
			scale = 1;
		}

		return scale;
	}

	/**
	 * Makes sure the colour does not exceed the 0-255 bounds
	 * @param colour The colour integer
	 * @return The numerical value of the colour
	 */
	public static int safe(int colour)
	{
		return Math.min(255, Math.max(colour, 0));
	}

	/**
	 * Makes sure the colour does not exceed the 0-255 bounds
	 * @param colour The colour integer
	 * @return The numerical value of the colour
	 */
	public static int safe(double colour)
	{
		return (int)Math.min(255.0, Math.max(colour, 0.0));
	}

	/**
	 * Duplicates a bitmap. This does <b>not</b> recycle the original bitmap after the method is called
	 * @param bm The bitmap you wish to duplicate
	 * @return The new bitmap
	 */
	public static Bitmap duplicate(Bitmap bm)
	{
		Bitmap newBitmap = Bitmap.createBitmap(bm);
		return newBitmap;
	}

	/**
	 * Resizes a bitmap. Original bitmap is recycled after this method is called.
	 * @param bm The bitmap to resize
	 * @param width The new width
	 * @param height Thew new height
	 * @return The resized bitmap
	 */
	public static Bitmap resize(Bitmap bm, int width, int height)
	{
		Bitmap newBitmap = Bitmap.createBitmap(width, height, mBitmapConfig);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawBitmap(bm, new Rect(0, 0, bm.getWidth(), bm.getHeight()), new Rect(0, 0, width, height), new Paint(Paint.ANTI_ALIAS_FLAG));

		if (mRecycleBitmaps)
		{
			bm.recycle();
		}

		return newBitmap;
	}

	/**
	 * Resizes a bitmap to a specific width, maintaining the ratio. Original bitmap is recycled after this method is called.
	 * @param bm The bitmap to resize
	 * @param width The new width
	 * @return The resized bitmap
	 */
	public static Bitmap resizeToWidth(Bitmap bm, int width)
	{
		//	Calculate the new height
		float ratio = (float)width / (float)bm.getWidth();
		int height = (int)(bm.getHeight() * ratio);

		return BitmapUtils.resize(bm, width, height);
	}

	/**
	 * Resizes a bitmap to a specific height, maintaining the ratio. Original bitmap is recycled after this method is called.
	 * @param bm The bitmap to resize
	 * @param height The new width
	 * @return The resized bitmap
	 */
	public static Bitmap resizeToHeight(Bitmap bm, int height)
	{
		//	Calculate the new width
		float ratio = (float)height / (float)bm.getHeight();
		int width = (int)(bm.getWidth() * ratio);

		return BitmapUtils.resize(bm, width, height);
	}

	/**
	 * Resizes a bitmap to a which ever axis is the largest, maintaining the ratio. Original bitmap is recycled after this method is called.
	 * @param bm The bitmap to resize
	 * @param maxWidth The max width size
	 * @param maxHeight The max height size
	 * @return The resized bitmap
	 */
	public static Bitmap maxResize(Bitmap bm, int maxWidth, int maxHeight)
	{
		//	Calculate what is larger width or height
		if (bm.getWidth() > bm.getHeight())
		{
			return BitmapUtils.resizeToWidth(bm, maxWidth);
		}
		else
		{
			return BitmapUtils.resizeToHeight(bm, maxHeight);
		}
	}

	/**
	 * Compresses a bitmap. original bitmap is recycled after this method is called.
	 * @param bm The bitmap to be compressed.
	 * @param compression The compression ratio 0-100.
	 * @return The compressed bitmap.
	 */
	public static Bitmap compress(Bitmap bm, int compression)
	{
		ByteArrayOutputStream bitmapOutputStream = new ByteArrayOutputStream();
		bm.compress(CompressFormat.JPEG, compression, bitmapOutputStream);

		if (mRecycleBitmaps)
		{
			bm.recycle();
		}

		return BitmapFactory.decodeByteArray(bitmapOutputStream.toByteArray(), 0, bitmapOutputStream.size());
	}

	/**
	 * Rotates a bitmap. Original bitmap is recycled after this method is called.
	 * @param bm The bitmap to rotate
	 * @param degrees The degrees to rotate at. 0-360 clockwise.
	 * @return The rotated bitmap
	 */
	public static Bitmap rotate(Bitmap bm, int degrees)
	{
		Matrix rotateMatrix = new Matrix();
		rotateMatrix.setRotate(degrees);

		Bitmap newBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), rotateMatrix, true);

		if (mRecycleBitmaps)
		{
			bm.recycle();
		}

		return newBitmap;
	}

	/**
	 * Flips an image
	 * @param bm The image to flip. Original bitmap is recycled after this method is called.
	 * @param mode The mode to flip
	 * @return The flipped bitmap
	 */
	public static Bitmap flip(Bitmap bm, int mode)
	{
		Bitmap newBitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), mBitmapConfig);
		Canvas canvas = new Canvas(newBitmap);
		Matrix flipMatrix = new Matrix();
		float xFlip = 1.0f, yFlip = 1.0f;

		if ((mode & FLIP_HORIZONTAL) == mode)
		{
			xFlip = -1.0f;
		}

		if ((mode & FLIP_VERTICAL) == mode)
		{
			yFlip = -1.0f;
		}

		flipMatrix.preScale(xFlip, yFlip);
		canvas.drawBitmap(bm, flipMatrix, new Paint());

		if (mRecycleBitmaps)
		{
			bm.recycle();
		}

		return newBitmap;
	}

	/**
	 * Crops a bitmap at the given indexes. Original bitmap is recycled after this method is called.
	 * @param bm The bitmap to crop
	 * @param startX The start x coord starting in TOP LEFT
	 * @param startY The start y coord starting in TOP LEFT
	 * @param width The width of the crop
	 * @param height The height of the crop
	 * @return The newly cropped bitmap.
	 */
	public static Bitmap crop(Bitmap bm, int startX, int startY, int width, int height)
	{
		int w = width;
	    int h = height;

	    Bitmap ret = Bitmap.createBitmap(w, h, Config.ARGB_8888);//bm.getConfig());
	    Canvas canvas = new Canvas(ret);
	    canvas.drawBitmap(bm, -startX, -startY, null);

	    if (mRecycleBitmaps)
		{
			bm.recycle();
		}

	    return ret;
	}

	/**
	 * Blend modes to use with the merge method
	 */
	public static enum BlendMode
	{
		//	Standard PorterDuff modes
		CLEAR,
		DARKEN,
		DST,
		DST_ATOP,
		DST_IN,
		DST_OUT,
		DST_OVER,
		LIGHTEN,
		MULTIPLY,
		SCREEN,
		SRC,
		SRC_ATOP,
		SRC_IN,
		SRC_OUT,
		SRC_OVER,
		XOR,

		//	Custom Blend modes
		NORMAL,
		OVERLAY,
		//ADD,
		DIFFERENCE,
		//EXCLUSION,
		SOFTLIGHT
	}

	/**
	 * Merges an image ontop of another image using a specific blend mode. Both images are recycled after the merge.
	 * @param original The bottom image
	 * @param overlay The image to merge to
	 * @param blendMode The blending mode of the merge
	 * @return The merged images
	 */
	public static Bitmap merge(Bitmap original, Bitmap overlay, BlendMode blendMode)
	{
		try
		{
			PorterDuff.Mode m = PorterDuff.Mode.valueOf(blendMode.name());

			int w = original.getWidth();
			int h = original.getHeight();

			Bitmap newBitmap = Bitmap.createBitmap(w, h, mBitmapConfig);
			Canvas c = new Canvas(newBitmap);
			c.drawBitmap(original, 0, 0, null);


			Paint paint = new Paint();
			paint.setXfermode(new PorterDuffXfermode(m));

			c.drawBitmap(overlay, new Rect(0, 0, overlay.getWidth(), overlay.getHeight()), new Rect(0, 0, w, h), paint);

			if (mRecycleBitmaps)
			{
				original.recycle();
				overlay.recycle();
			}

			return newBitmap;
		}
		catch (IllegalArgumentException e)
		{
			int w = original.getWidth();
			int h = original.getHeight();

			Bitmap newBitmap = Bitmap.createBitmap(w, h, mBitmapConfig);
			Canvas c = new Canvas(newBitmap);
			//c.drawBitmap(overlay, new Rect(0, 0, overlay.getWidth(), overlay.getHeight()), new Rect(0, 0, w, h), new Paint());

			if (blendMode == BlendMode.NORMAL)
			{
				c.drawBitmap(original, 0, 0, new Paint());
				c.drawBitmap(overlay, new Rect(0, 0, overlay.getWidth(), overlay.getHeight()), new Rect(0, 0, w, h), new Paint());

				if (mRecycleBitmaps)
				{
					original.recycle();
					overlay.recycle();
				}

				return newBitmap;
			}

			for (int x = 0; x < w; x++)
			{
				for (int y = 0; y < h; y++)
				{
					int colour1 = original.getPixel(x, y);
					int colour2 = newBitmap.getPixel(x, y);

					double[] rgb1 = new double[]{Color.red(colour1), Color.green(colour1), Color.blue(colour1)};
					double[] rgb2 = new double[]{Color.red(colour2), Color.green(colour2), Color.blue(colour2)};
					int[] rgb3 = new int[]{(int)rgb2[0], (int)rgb2[1], (int)rgb2[2]};

					if (blendMode == BlendMode.OVERLAY)
					{
						rgb3[0] = safe(rgb1[0] > 128.0 ? (255.0 - 2.0 * (255.0 - rgb2[0]) * (255.0 - rgb1[0]) / 255.0) : ((rgb1[0] * rgb2[0] * 2.0) / 255.0));
						rgb3[1] = safe(rgb1[1] > 128.0 ? (255.0 - 2.0 * (255.0 - rgb2[1]) * (255.0 - rgb1[1]) / 255.0) : ((rgb1[1] * rgb2[1] * 2.0) / 255.0));
						rgb3[2] = safe(rgb1[2] > 128.0 ? (255.0 - 2.0 * (255.0 - rgb2[2]) * (255.0 - rgb1[2]) / 255.0) : ((rgb1[2] * rgb2[2] * 2.0) / 255.0));
					}
					else if (blendMode == BlendMode.DIFFERENCE)
					{
						rgb3[0] = safe(Math.abs(rgb2[0] - rgb1[0]));
						rgb3[1] = safe(Math.abs(rgb2[1] - rgb1[1]));
						rgb3[2] = safe(Math.abs(rgb2[2] - rgb1[2]));
					}
					else if (blendMode == BlendMode.SOFTLIGHT)
					{
						rgb3[0] = safe(rgb1[0] > 128 ? 255 - ((255 - rgb1[0]) * (255 - (rgb2[0] - 128))) / 255 : (rgb1[0] * (rgb2[0] + 128)) / 255);
						rgb3[1] = safe(rgb1[1] > 128 ? 255 - ((255 - rgb1[1]) * (255 - (rgb2[1] - 128))) / 255 : (rgb1[1] * (rgb2[1] + 128)) / 255);
						rgb3[2] = safe(rgb1[2] > 128 ? 255 - ((255 - rgb1[2]) * (255 - (rgb2[2] - 128))) / 255 : (rgb1[2] * (rgb2[2] + 128)) / 255);
					}

					Paint p = new Paint();
					p.setARGB(255, rgb3[0], rgb3[1], rgb3[2]);
					c.drawRect(x, y, x + 1, y + 1, p);
				}
			}

			if (mRecycleBitmaps)
			{
				original.recycle();
				overlay.recycle();
			}

			return newBitmap;
		}
	}

	/**
	 * Fixes the orientation of a bitmap. Original bitmap is recycled after this method is called
	 * @param bm The bitmap to fix
	 * @param currentOrientation The current orientation as discripted in {@link ExifInterface}
	 * @return The fixed bitmap
	 */
	public static Bitmap fixOrientation(Bitmap bm, int currentOrientation)
	{
		switch (currentOrientation)
		{
			case ORIENTATION_HORIZONTAL:
			{
				return flip(bm, FLIP_HORIZONTAL);
			}

			case ORIENTATION_180_ROTATE_LEFT:
			{
				return rotate(bm, -180);
			}

			case ORIENTATION_VERTICAL_FLIP:
			{
				return flip(bm, FLIP_VERTICAL);
			}

			case ORIENTATION_VERTICAL_FLIP_90_ROTATE_RIGHT:
			{
				return rotate(flip(bm, FLIP_VERTICAL), 90);
			}

			case ORIENTATION_90_ROTATE_RIGHT:
			{
				return rotate(bm, 90);
			}

			case ORIENTATION_HORIZONTAL_FLIP_90_ROTATE_RIGHT:
			{
				return rotate(flip(bm, FLIP_HORIZONTAL), 90);
			}

			case ORIENTATION_90_ROTATE_LEFT:
			{
				return rotate(bm, -90);
			}
		}

		return bm;
	}
}