package in.lib.utils;

import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

/**
 * View injection class which runs at runtime rather than compile time like ButterKnife. The benefit to this is there's no generated code which some IDEs (such as Eclipse) fail to generate properly and can often lead to broken references.
 * Also contains an onClick annotation.
 *
 * <h1>Usage:</h1>
 * <code>
 * // View inject for variable
 * @InjectView(R.id.view_id) public View view; // var needs to be public
 * @InjectView(id = "view_id") public View view; // var needs to be public, use this in libraries because IDs are not constant
 * @InjectView public View view; // var needs to be public. Will use the member name (lower case, underscore before capitals) for the lookup. E.G: myView will search for R.id.my_view
 *
 * // OnClick for variable
 * @OnClick public View view; // var needs to be initialized *before* calling Views.inject. Uses current class which implements View.OnClickListener
 * @OnClick(method = "methodName") public View view; // method name must exist with 1 parameter for View
 *
 * // OnClick for method
 * @OnClick public void onViewIdClick(View v){} // Method name must match (on)?(.*)Click where .* is the view name capitalised. The prefix "on" is optional. This will be converted to lower case, underscore before capitals
 * @OnClick(id = "view_id") public void onViewIdClick(View v){} // String id. Use this in libraries because IDs are not constant
 * @OnClick(R.id.view_id) public void onViewIdClick(View v){} // Use standard ID reference
 *
 * // Methods work without parameters also
 * @OnClick public void onViewIdClick(){}
 * @OnClick(id = "view_id") public void onViewIdClick(){}
 * @OnClick(R.id.view_id) public void onViewIdClick(){}
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
	 * Use this in the {@link onDestroyView} of your fragment
	 * @param target
	 */
	public static void reset(Object target)
	{
		if (target.getClass().getFields() != null)
		{
			for (Field field : target.getClass().getFields())
			{
				if (field.isAnnotationPresent(InjectView.class))
				{
					Annotation[] annotations = field.getDeclaredAnnotations();

					for (Annotation a : annotations)
					{
						try
						{
							if (a.annotationType().equals(InjectView.class))
							{
								field.set(target, null);
								break;
							}
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
	 * {@link source}
	 * @param target The class to inject
	 * @param source The activity to find the views
	 */
	public static void inject(Object target, Activity source)
	{
		inject(target, source, Finder.ACTIVITY);
	}

	/**
	 * Injects all @InjectView and @OnClick members and methods in target from
	 * {@link source}
	 * @param target The class to inject
	 * @param source The view to find the views
	 */
	public static void inject(Object target, View source)
	{
		inject(target, source, Finder.VIEW);
	}

	private static void inject(final Object target, Object source, Finder finder)
	{
		if (target.getClass().getFields() != null)
		{
			Context c = source instanceof Activity ? (Activity)source : ((View)source).getContext();
			for (Field field : target.getClass().getFields())
			{
				if (field.isAnnotationPresent(InjectView.class))
				{
					Annotation[] annotations = field.getDeclaredAnnotations();

					for (Annotation a : annotations)
					{
						try
						{
							if (a.annotationType().equals(InjectView.class))
							{
								int id = ((InjectView)a).value();
								if (id < 1)
								{
									String key = ((InjectView)a).id();
									if (TextUtils.isEmpty(key))
									{
										key = field.getName();
										key = key.replaceAll("(.)([A-Z])", "$1_$2").toLowerCase(Locale.ENGLISH);
									}

									id = c.getResources().getIdentifier(key, "id", c.getPackageName());
								}

								View v = finder.findById(source, id);

								if (v != null)
								{
									field.set(target, v);
								}

								break;
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}

				if (field.isAnnotationPresent(OnClick.class))
				{
					Annotation[] annotations = field.getDeclaredAnnotations();

					for (Annotation a : annotations)
					{
						try
						{
							if (a.annotationType().equals(OnClick.class))
							{
								if (field.get(target) != null)
								{
									final View view = ((View)field.get(target));

									if (!TextUtils.isEmpty(((OnClick)a).method()))
									{
										final String clickName = ((OnClick)a).method();
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

								break;
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}

			for (Method method : target.getClass().getMethods())
			{
				if (method.isAnnotationPresent(OnClick.class))
				{
					Annotation[] annotations = method.getDeclaredAnnotations();

					for (Annotation a : annotations)
					{
						try
						{
							if (a.annotationType().equals(OnClick.class))
							{
								final OnClick annotation = (OnClick)a;
								final String clickName = method.getName();

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

									id = c.getResources().getIdentifier(key, "id", c.getPackageName());
								}

								View view = finder.findById(source, id);
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
											try
											{
												Class c = Class.forName(target.getClass().getCanonicalName());
												Method m = c.getMethod(clickName);
												m.invoke(target);
											}
											catch (Exception e2)
											{
												throw new IllegalArgumentException("Failed to find method " + clickName + " with nil or View params");
											}
										}
									}
								});
							}
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
}