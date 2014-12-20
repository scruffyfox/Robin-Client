package in.rob.client.page;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.adapter.LocaleArrayAdapter;
import in.lib.annotation.InjectView;
import in.lib.event.ProfileUpdatedEvent;
import in.lib.helper.BusHelper;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.BitmapUtils;
import in.lib.utils.LocaleUtils;
import in.lib.utils.Views;
import in.model.User;
import in.rob.client.MainApplication;
import in.rob.client.R;
import in.rob.client.base.RobinFragment;
import in.rob.client.dialog.base.DialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.TimeZone;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ProfileSettingsPage extends RobinFragment implements OnClickListener
{
	@InjectView(R.id.username) public EditText mUserName;
	@InjectView(R.id.bio) public EditText mBio;
	@InjectView(R.id.languages) public Spinner mLanguages;
	@InjectView(R.id.timezones) public Spinner mTimezones;
	@InjectView(R.id.edit_profile_button) public View mSaveProfile;
	@InjectView(R.id.cover_image) public ImageView mCoverImage;
	@InjectView(R.id.avatar) public ImageView mAvatar;

	private User user;
	private ProgressDialog mProgressDialog;
	private File avatarFile, coverFile;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.edit_profile_view, null);
		Views.inject(this, view);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(SettingsManager.getBioLength());
		mBio.setFilters(FilterArray);

		user = UserManager.getUser();
		mUserName.setText(user.getUserName());

		if (!TextUtils.isEmpty(user.getFormattedDescription()))
		{
			mBio.setText(Html.fromHtml(user.getFormattedDescription()).toString());
		}

		mProgressDialog = new ProgressDialog(getContext());
		mProgressDialog.setMessage(getString(R.string.updating_profile));
		mProgressDialog.setCanceledOnTouchOutside(false);

		mSaveProfile.setOnClickListener(this);

		new Handler().post(new Runnable()
		{
			@Override public void run()
			{
				ImageLoader.getInstance().displayImage(user.getCoverUrl(), mCoverImage, MainApplication.getMediaImageOptions());
				ImageLoader.getInstance().displayImage(user.getAvatarUrl(), mAvatar, MainApplication.getMediaImageOptions());

				LocaleArrayAdapter mLocalesAdapter = new LocaleArrayAdapter(getContext(), LocaleUtils.getSortedAvailableLocales());
				mLanguages.setAdapter(mLocalesAdapter);
				mLanguages.setSelection(mLocalesAdapter.getPosition(new Locale(UserManager.getUser().getLocale())));

				ArrayAdapter<String> mTimezoneAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, TimeZone.getAvailableIDs());
				mTimezones.setAdapter(mTimezoneAdapter);
				mTimezones.setSelection(mTimezoneAdapter.getPosition(UserManager.getUser().getTimeZone()));
			}
		});

		mAvatar.setOnClickListener(this);
		mCoverImage.setOnClickListener(this);
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != Activity.RESULT_OK) return;

		if (requestCode == Constants.REQUEST_CAMERA_AVATAR || requestCode == Constants.REQUEST_GALLERY_AVATAR)
		{
			if (requestCode == Constants.REQUEST_CAMERA_AVATAR)
			{
				try
				{
					Intent intent = new Intent("com.android.camera.action.CROP");
					intent.setDataAndType(Uri.fromFile(avatarFile), "image/*");
					intent.putExtra("crop", "true");
					intent.putExtra("scale", true);
					intent.putExtra("outputX", 256);
					intent.putExtra("outputY", 256);
					intent.putExtra("aspectX", 1);
					intent.putExtra("aspectY", 1);
					intent.putExtra("noFaceDetection", false);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(avatarFile));
					startActivityForResult(intent, Constants.REQUEST_GALLERY_AVATAR);
					return;
				}
				catch (Exception e) {}
			}

			if (avatarFile != null)
			{
				Bitmap avatar = BitmapFactory.decodeFile(avatarFile.toString());
				if (avatar != null)
				{
					int ratio = avatar.getWidth() > avatar.getHeight() ? avatar.getHeight() : avatar.getWidth();
					int x = (avatar.getWidth() - ratio);
					x = x > 0 ? x / 2 : 0;
					int y = (avatar.getHeight() - ratio);
					y = y > 0 ? y / 2 : 0;
					avatar = BitmapUtils.crop(avatar, x, y, ratio, ratio);
					avatar = BitmapUtils.resize(avatar, 300, 300);

					mAvatar.setImageBitmap(avatar);
					saveImage(avatar, avatarFile);
				}
			}
		}
		else if (requestCode == Constants.REQUEST_CAMERA_COVER || requestCode == Constants.REQUEST_GALLERY_COVER)
		{
			if (requestCode == Constants.REQUEST_CAMERA_COVER)
			{
				try
				{
					Intent intent = new Intent("com.android.camera.action.CROP");
					intent.setDataAndType(Uri.fromFile(coverFile), "image/*");
					intent.putExtra("crop", "true");
					intent.putExtra("scale", true);
					intent.putExtra("aspectX", 960);
					intent.putExtra("aspectY", 500);
					intent.putExtra("max-width", 960);
					intent.putExtra("max-height", 500);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(coverFile));
					startActivityForResult(intent, Constants.REQUEST_GALLERY_COVER);
					return;
				}
				catch (Exception e) {}
			}

			if (coverFile != null)
			{
				Options options = new Options();
				options.inSampleSize = BitmapUtils.recursiveSample(coverFile.toString(), 960, 500);
				Bitmap b = BitmapFactory.decodeFile(coverFile.toString(), options);

				if (b != null)
				{
					b = BitmapUtils.resizeToWidth(b, 960);

					if (b.getHeight() > 500)
					{
						b = BitmapUtils.crop(b, 0, 0, 960, 500);
					}

					saveImage(b, coverFile);
					mCoverImage.setImageBitmap(b);
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void saveImage(Bitmap image, File filename)
	{
		try
		{
			filename.delete();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			image.compress(CompressFormat.JPEG, 100, bos);
			byte[] bytes = bos.toByteArray();
			FileOutputStream fos = new FileOutputStream(filename);

			for (int offset = 0, size = bytes.length; offset < bytes.length; offset += 8192, size -= 8192)
			{
				fos.write(bytes, offset, Math.min(8192, size));
			}

			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public void onClick(View v)
	{
		if (v == mAvatar)
		{
			CharSequence[] choices = getResources().getStringArray(R.array.image_choice);
			CharSequence[] items = {choices[0], choices[1]};

			DialogBuilder.create(getContext())
			.setItems(items, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					avatarFile = new File(getContext().getExternalCacheDir() + "/avatar_" + System.currentTimeMillis() + ".jpg");

					try
					{
						avatarFile.delete();
						avatarFile.createNewFile();
					}
					catch (Exception e)
					{
						Debug.out(e);
					}

					// Camera
					if (which == 0)
					{
						Uri path = Uri.fromFile(avatarFile);

						Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
						intent.putExtra(MediaStore.EXTRA_OUTPUT, path);
						startActivityForResult(intent, Constants.REQUEST_CAMERA_AVATAR);
					}
					//	Gallery
					else if (which == 1)
					{
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
						intent.setType("image/*");
						intent.putExtra("crop", "true");
						intent.putExtra("scale", true);
						intent.putExtra("outputX", 256);
						intent.putExtra("outputY", 256);
						intent.putExtra("aspectX", 1);
						intent.putExtra("aspectY", 1);
						intent.putExtra("noFaceDetection", false);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(avatarFile));
						startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), Constants.REQUEST_GALLERY_AVATAR);
					}
				}
			})
			.setTitle(getString(R.string.please_select))
			.show();
		}
		else if (v == mCoverImage)
		{
			CharSequence[] choices = getResources().getStringArray(R.array.image_choice);
			CharSequence[] items = {choices[0], choices[1]};

			DialogBuilder.create(getContext())
			.setItems(items, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					coverFile = new File(getContext().getExternalCacheDir() + "/cover_" + System.currentTimeMillis() + ".jpg");

					try
					{
						coverFile.delete();
						coverFile.createNewFile();
					}
					catch (Exception e)
					{
						Debug.out(e);
					}

					//	Camera
					if (which == 0)
					{
						Uri path = Uri.fromFile(coverFile);
						Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
						intent.putExtra(MediaStore.EXTRA_OUTPUT, path);
						startActivityForResult(intent, Constants.REQUEST_CAMERA_COVER);
					}
					//	Gallery
					else if (which == 1)
					{
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
						intent.setType("image/*");
						intent.putExtra("crop", "true");
						intent.putExtra("scale", true);
						intent.putExtra("aspectX", 960);
						intent.putExtra("aspectY", 500);
						intent.putExtra("max-width", 960);
						intent.putExtra("max-height", 500);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(coverFile));
						startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), Constants.REQUEST_GALLERY_COVER);
					}
				}
			})
			.setTitle(getString(R.string.please_select))
			.show();
		}
		else if (v == mSaveProfile)
		{
			if (avatarFile != null)
			{
				updateAvatar();
			}
			else if (coverFile != null)
			{
				updateCover();
			}
			else
			{
				updateProfile();
			}
		}
	}

	private void updateAvatar()
	{
		final ProgressDialog progress = new ProgressDialog(getContext());
		progress.setMessage(getString(R.string.uploading_avatar));
		progress.setTitle(R.string.uploading);
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);
		progress.show();

		APIManager.getInstance().updateAvatar(avatarFile, new JsonResponseHandler()
		{
			@Override public void onPublishedUploadProgressUI(long totalProcessed, long totalLength)
			{
				progress.setProgress((int)totalProcessed);
				progress.setMax((int)totalLength);
			}

			@Override public void onSuccess()
			{
				avatarFile = null;
			}

			@Override public void onFinish(boolean failed)
			{
				progress.dismiss();

				if (!failed)
				{
					if (coverFile != null)
					{
						updateCover();
					}
					else
					{
						updateProfile();
					}
				}
				else
				{
					Toast.makeText(getContext(), R.string.upload_failed, Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private void updateCover()
	{
		final ProgressDialog progress = new ProgressDialog(getContext());
		progress.setMessage(getString(R.string.uploading_cover));
		progress.setTitle(R.string.uploading);
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);
		progress.show();

		APIManager.getInstance().updateCover(coverFile, new JsonResponseHandler()
		{
			@Override public void onPublishedUploadProgressUI(long totalProcessed, long totalLength)
			{
				progress.setProgress((int)totalProcessed);
				progress.setMax((int)totalLength);
			}

			@Override public void onSuccess()
			{
				Intent mIntent = new Intent();
				mIntent.putExtra(Constants.EXTRA_REFRESH_COVER, true);
				getActivity().setResult(Constants.RESULT_REFRESH, mIntent);

				coverFile = null;
			}

			@Override public void onFinish(boolean failed)
			{
				progress.dismiss();

				if (!failed)
				{
					updateProfile();
				}
				else
				{
					Toast.makeText(getContext(), R.string.upload_failed, Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private void updateProfile()
	{
		APIManager.getInstance().updateUser
		(
			mUserName.getText().toString(),
			mBio.getText().toString(),
			mLanguages.getSelectedItem().toString(),
			mTimezones.getSelectedItem().toString(),
			new JsonResponseHandler()
			{
				private User user;

				@Override public void onSend()
				{
					mProgressDialog.show();
				}

				@Override public void onSuccess()
				{
					try
					{
						JsonElement elements = getContent();
						JsonObject userJson = elements.getAsJsonObject().get("data").getAsJsonObject();
						user = new User().createFrom(userJson);
						user.save();
						UserManager.setUser(user, getContext());
					}
					catch (Exception e)
					{
						Debug.out(e);
					}

					getActivity().runOnUiThread(new Runnable()
					{
						@Override public void run()
						{
							Toast.makeText(getContext(), R.string.profile_updated, Toast.LENGTH_LONG).show();
						}
					});
				}

				@Override public void onFinish()
				{
					mProgressDialog.dismiss();
					BusHelper.getInstance().post(new ProfileUpdatedEvent(user));
				}
			}
		);
	}
}