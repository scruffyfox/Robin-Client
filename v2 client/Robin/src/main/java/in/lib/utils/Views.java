package in.lib.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import in.rob.client.R;

/**
 * View injection class which runs at runtime rather than compile time like ButterKnife. The benefit to this is there's no generated code which some IDEs (such as Eclipse) fail to generate properly and can often lead to broken references.
 * Also contains an onClick annotation.
 *
 * <h1>Usage:</h1>
 * <code>
 * // View inject for variable
 * @InjectView(R.id.view_id) private View view;
 * @InjectView(id = "view_id") private View view; // use this in libraries because IDs are not constant
 * @InjectView private View view; // Will use the member name (lower case, underscore before capitals) for the lookup. E.G: myView will search for R.id.my_view
 *
 * // OnClick for variable
 * @OnClick private View view; // var needs to be initialized *before* calling Views.inject. Uses current class which implements View.OnClickListener
 * @OnClick(method = "methodName") private View view; // method name must exist with 1 parameter for View
 *
 * // OnClick for method
 * @OnClick private void onViewIdClick(View v){} // Method name must match (on)?(.*)Click where .* is the view name capitalised. The prefix "on" is optional. This will be converted to lower case, underscore before capitals
 * @OnClick(id = "view_id") private void onViewIdClick(View v){} // String id. Use this in libraries because IDs are not constant
 * @OnClick(R.id.view_id) private void onViewIdClick(View v){} // Use standard ID reference
 *
 * // Methods work without parameters also
 * @OnClick private void onViewIdClick(){}
 * @OnClick(id = "view_id") private void onViewIdClick(){}
 * @OnClick(R.id.view_id) private void onViewIdClick(){}
 * </code>
 *
 * To execute the injector service, you must call <code>Views.inject(this);</code> after setting the content.
 * You can also call <code>Views.inject(this, view)</code> in fragments or view holders to populate the members from a base view.
 *
 *
 * <h1>License</h1>
 * <pre>
 * Copyright 2013 Callum Taylor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <pre>
 *
 * Finder enum code used from <a href="https://github.com/JakeWharton/butterknife">ButterKnife</a>
 */
public class Views
{
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	public @interface InjectView
	{
		int value() default 0;
		String id() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE})
	public @interface Injectable
	{

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.METHOD})
	public @interface OnClick
	{
		int value() default 0;
		String id() default "";
		String method() default "";
	}

	private enum Finder
	{
		VIEW
		{
			@SuppressWarnings("unchecked") @Override public <T extends View> T findById(Object source, int id)
			{
				return (T)((View)source).findViewById(id);
			}
		},
		ACTIVITY
		{
			@SuppressWarnings("unchecked") @Override public <T extends View> T findById(Object source, int id)
			{
				return (T)((Activity)source).findViewById(id);
			}
		};

		public abstract <T extends View> T findById(Object source, int id);
	}

	/**
	 * Resets all @InjectView annotated members to null.
	 * Use this in the `onDestroyView()` of your fragment
	 * @param target
	 */
	public static void reset(Object target)
	{
		ArrayList<Field> fields = new ArrayList<Field>();
		Class objOrSuper = target.getClass();

		if (!objOrSuper.isAnnotationPresent(Injectable.class))
		{
			Debug.out("No Injectable annotation for class " + objOrSuper);
			return;
		}

		while (objOrSuper.isAnnotationPresent(Injectable.class))
		{
			for (Field field : objOrSuper.getDeclaredFields())
			{
				if (field.isAnnotationPresent(InjectView.class) || field.isAnnotationPresent(OnClick.class))
				{
					fields.add(field);
				}
			}

			objOrSuper = objOrSuper.getSuperclass();
		}

		for (Field field : fields)
		{
			Annotation a = field.getAnnotation(InjectView.class);
			if (a != null)
			{
				try
				{
					field.setAccessible(true);
					field.set(target, null);
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Finds a view from an id in a view
	 * @param id
	 * @param v
	 * @return
	 */
	public static <T extends View> T findViewById(int id, View v)
	{
		return Finder.VIEW.findById(v, id);
	}

	/**
	 * Finds a view from an id in an activity
	 * @param id
	 * @param a
	 * @return
	 */
	public static <T extends View> T findViewById(int id, Activity a)
	{
		return Finder.ACTIVITY.findById(a, id);
	}

	/**
	 * Injects all @InjectView and @OnClick members and methods in an activity
	 * @param target The activity to inject and find the views
	 */
	public static void inject(Activity target)
	{
		inject(target, target, Finder.ACTIVITY);
	}

	/**
	 * Injects all @InjectView and @OnClick members and methods in target from
	 * {@param source}
	 * @param target The class to inject
	 * @param source The activity to find the views
	 */
	public static void inject(Object target, Activity source)
	{
		inject(target, source, Finder.ACTIVITY);
	}

	/**
	 * Injects all @InjectView and @OnClick members and methods in target from
	 * {@param source}
	 * @param target The class to inject
	 * @param source The view to find the views
	 */
	public static void inject(Object target, View source)
	{
		inject(target, source, Finder.VIEW);
	}

	private static void inject(final Object target, Object source, Finder finder)
	{
		if (target.getClass().getDeclaredFields() != null)
		{
			Context c = source instanceof Activity ? (Activity)source : ((View)source).getContext();

			ArrayList<Method> methods = new ArrayList<Method>();
			ArrayList<Field> fields = new ArrayList<Field>();
			Class objOrSuper = target.getClass();

			if (!objOrSuper.isAnnotationPresent(Injectable.class))
			{
				Debug.out("No Injectable annotation for class " + objOrSuper);
				return;
			}

			while (objOrSuper.isAnnotationPresent(Injectable.class))
			{
				for (Field field : objOrSuper.getDeclaredFields())
				{
					if (field.isAnnotationPresent(InjectView.class) || field.isAnnotationPresent(OnClick.class))
					{
						fields.add(field);
					}
				}

				for (Method method : objOrSuper.getDeclaredMethods())
				{
					if (method.isAnnotationPresent(OnClick.class))
					{
						methods.add(method);
					}
				}

				objOrSuper = objOrSuper.getSuperclass();
			}

			for (Field field : fields)
			{
				if (field.isAnnotationPresent(InjectView.class))
				{
					InjectView a = (InjectView)field.getAnnotation(InjectView.class);

					try
					{
						field.setAccessible(true);

						int id = ((InjectView)a).value();
						if (id < 1)
						{
							String key = ((InjectView)a).id();
							if (TextUtils.isEmpty(key))
							{
								key = field.getName();
								key = key.replaceAll("(.)([A-Z])", "$1_$2").toLowerCase(Locale.ENGLISH);
							}

							Field idField = R.id.class.getField(key);
							id = idField.getInt(null);
						}

						View v = finder.findById(source, id);

						if (v != null)
						{
							field.set(target, v);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				if (field.isAnnotationPresent(OnClick.class))
				{
					OnClick a = (OnClick)field.getAnnotation(OnClick.class);

					try
					{
						if (field.get(target) != null)
						{
							final View view = ((View)field.get(target));

							if (!TextUtils.isEmpty(a.method()))
							{
								final String clickName = a.method();
								view.setOnClickListener(new View.OnClickListener()
								{
									@Override public void onClick(View v)
									{
										try
										{
											Class c = Class.forName(target.getClass().getCanonicalName());
											Method m = c.getMethod(clickName, View.class);
											m.invoke(target, v);
										}
										catch (Exception e)
										{
											throw new IllegalArgumentException("Method not found " + clickName);
										}
									}
								});
							}
							else
							{
								view.setOnClickListener((View.OnClickListener)target);
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}

			for (final Method method : methods)
			{
				if (method.isAnnotationPresent(OnClick.class))
				{
					final OnClick annotation = (OnClick)method.getAnnotation(OnClick.class);
					final String clickName = method.getName();

					try
					{
						int id = annotation.value();
						if (id < 1)
						{
							String key = annotation.id();

							if (TextUtils.isEmpty(key))
							{
								key = clickName;
								key = key.replaceAll("^(on)?(.*)Click$", "$2");
								key = key.replaceAll("(.)([A-Z])", "$1_$2").toLowerCase(Locale.ENGLISH);
							}

							Field field = R.id.class.getField(key);
							id = field.getInt(null);
						}

						View view = finder.findById(source, id);
						view.setOnClickListener(new View.OnClickListener()
						{
							@Override public void onClick(View v)
							{
								try
								{
									if (method != null && method.getParameterTypes().length > 0)
									{
										Class<?> paramType = method.getParameterTypes()[0];
										method.setAccessible(true);
										method.invoke(target, paramType.cast(v));
									}
									else if (method != null && method.getParameterTypes().length < 1)
									{
										method.setAccessible(true);
										method.invoke(target);
									}
									else
									{
										new IllegalArgumentException("Failed to find method " + clickName + " with nil or View params").printStackTrace();
									}
								}
								catch (InvocationTargetException e)
								{
									e.printStackTrace();
								}
								catch (IllegalAccessException e)
								{
									e.printStackTrace();
								}
							}
						});
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}
