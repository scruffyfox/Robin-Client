#README

To use the library:

In your XML, include the `RefreshableListView`

```xml
<net.callumtaylor.swipetorefresh.view.RefreshableListView
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:id="@android:id/list"
	android:divider="?attr/storm_list_view_divider_color"
	android:scrollbarStyle="outsideOverlay"
/>
```

Implement `OnRefreshListener` in your fragment

And in your fragment, in `onActivityCreate` add

```java
refreshHelper = RefreshHelper.wrapRefreshable(getActivity(), (RefreshableListView)getListView(), this);
```

Now, when the list is refreshed, `onRefresh()` will be called.

You can then call `ListView.onRefreshComplete();` to stop the inderterminate refreshable.

There is also a time where you would use a single fragment which gets replaced, you may end up with multiple zombie pull to refresh views in your action bar. To fix this, in your base activity simple call `RefreshHelper.reset(getActivity());`

##Style

You can customise the style of the PTR by overriding `ptr_text_style`, `ptr_progress_style`, and `ptr_progress_inderteminate_style` in your theme.

###Example styles

`@style/ptr_text_style`

```xml
<style name="RefreshProgressText">
	<item name="android:textColor">#ffffffff</item>
</style>
```

`@style/ptr_progress_style`

```xml
<style name="RefreshProgressInderterminateThemeLight">
	<item name="android:indeterminateDrawable">@drawable/progress_indeterminate_horizontal_holo_light</item>
	<item name="android:minHeight">3dip</item>
	<item name="android:maxHeight">3dip</item>
	<item name="android:indeterminateOnly">true</item>
</style>
```

`@style/ptr_progress_inderteminate_style`

```xml
<style name="RefreshProgressInderterminateThemeLight" parent="android:Widget.ProgressBar.Horizontal">
	<item name="android:indeterminateDrawable">@drawable/progress_indeterminate_horizontal_holo_light</item>
	<item name="android:minHeight">4dip</item>
	<item name="android:maxHeight">4dip</item>
	<item name="android:indeterminateOnly">true</item>
</style>
```

#LICENSE

```
Copyright 2013 Callum Taylor

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```