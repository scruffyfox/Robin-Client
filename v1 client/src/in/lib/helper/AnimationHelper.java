package in.lib.helper;

import in.rob.client.R;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class AnimationHelper
{
	public static void slideUp(View view)
	{
		AnimationSet animation = new AnimationSet(true);

		TranslateAnimation slideUp = new TranslateAnimation(0, 0, 300, 0);
		slideUp.setDuration(400);
		slideUp.setFillAfter(true);

		DecelerateInterpolator interpolator = new DecelerateInterpolator();
		slideUp.setInterpolator(interpolator);

		animation.addAnimation(slideUp);
		view.setAnimation(animation);
	}

	public static void slideDown(View view)
	{
		AnimationSet animation = new AnimationSet(true);
		TranslateAnimation slideDown = new TranslateAnimation(0, 0, -300, 0);
		slideDown.setDuration(400);
		slideDown.setFillAfter(true);

		DecelerateInterpolator interpolator = new DecelerateInterpolator();
		slideDown.setInterpolator(interpolator);

		animation.addAnimation(slideDown);
		view.setAnimation(animation);
	}

	public static void fadeOut(final View view)
	{
		AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
		{
			animation.setDuration(400);
			animation.setInterpolator(new LinearInterpolator());
			animation.setAnimationListener(new AnimationListener()
			{
				@Override public void onAnimationEnd(Animation animation)
				{
					view.setVisibility(View.GONE);
				}
				@Override public void onAnimationRepeat(Animation animation){}

				@Override public void onAnimationStart(Animation animation){}
			});
		}

		view.startAnimation(animation);
	}

	public static void pullRefreshActionBar(final View overlay, View abs)
	{
		AnimationSet animationSet = new AnimationSet(true);
		{
			TranslateAnimation animation2 = new TranslateAnimation
			(
				Animation.RELATIVE_TO_SELF,
				0f,
				Animation.RELATIVE_TO_SELF,
				0f,
				Animation.RELATIVE_TO_PARENT,
				-1f,
				Animation.RELATIVE_TO_PARENT,
				0f
			);

			animation2.setFillAfter(true);
			animation2.setDuration(300);
			animation2.setInterpolator(new LinearInterpolator());

			AlphaAnimation animation3 = new AlphaAnimation(0.0f, 1.0f);
			animation3.setFillAfter(true);
			animation3.setFillBefore(true);
			animation3.setDuration(100);
			animation3.setInterpolator(new LinearInterpolator());

			animationSet.addAnimation(animation2);
			animationSet.addAnimation(animation3);
		}

		AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
		{
			animation.setFillAfter(true);
			animation.setDuration(400);
			animation.setInterpolator(new LinearInterpolator());
		}

		animationSet.setAnimationListener(new AnimationListener()
		{
			@Override public void onAnimationEnd(Animation animation){}

			@Override public void onAnimationRepeat(Animation animation){}
			@Override public void onAnimationStart(Animation animation)
			{
				overlay.setVisibility(View.VISIBLE);
			}
		});

		abs.startAnimation(animation);
		overlay.startAnimation(animationSet);
	}

	public static void pullRefreshActionBarCancel(final View overlay, View abs)
	{
		AnimationSet animationSet = new AnimationSet(true);
		{
			TranslateAnimation animation2 = new TranslateAnimation
			(
				Animation.RELATIVE_TO_SELF,
				0f,
				Animation.RELATIVE_TO_SELF,
				0f,
				Animation.RELATIVE_TO_PARENT,
				0f,
				Animation.RELATIVE_TO_PARENT,
				-1f
			);

			animation2.setDuration(300);
			animation2.setInterpolator(new LinearInterpolator());

			AlphaAnimation animation3 = new AlphaAnimation(1.0f, 0.0f);
			animation3.setFillAfter(true);
			animation3.setFillBefore(true);
			animation3.setDuration(300);
			animation3.setInterpolator(new LinearInterpolator());

			animationSet.addAnimation(animation2);
			animationSet.addAnimation(animation3);
		}

		AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
		{
			animation.setFillAfter(true);
			animation.setDuration(400);
			animation.setInterpolator(new LinearInterpolator());
		}

		animationSet.setAnimationListener(new AnimationListener()
		{
			@Override public void onAnimationEnd(Animation animation)
			{
				overlay.setVisibility(View.GONE);
				((TextView)overlay.findViewById(R.id.refresh_text)).setText(R.string.ptr_pull);
			}
			@Override public void onAnimationRepeat(Animation animation){}
			@Override public void onAnimationStart(Animation animation){}
		});

		abs.startAnimation(animation);
		overlay.startAnimation(animationSet);
	}
}