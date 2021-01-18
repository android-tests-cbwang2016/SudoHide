package com.sudocode.sudohide.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.sudocode.sudohide.ApplicationData;
import com.sudocode.sudohide.BitmapCachedLoader;
import com.sudocode.sudohide.Constants;
import com.sudocode.sudohide.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class CheckBoxAdapter extends AppListAdapter {

	private final String currentPkgName;
	private final Map<String, Boolean> changedItems;

	public CheckBoxAdapter(Context context, String pkgName, boolean showSystemApps) {
		super(context, showSystemApps);
		currentPkgName = pkgName;
		changedItems = new TreeMap<>();
		getAppList();
	}

	public Map<String, Boolean> getChangedItems() {
		return changedItems;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) convertView = mInflater.inflate(R.layout.list_item, parent, false);
		
		final TextView title = convertView.findViewById(R.id.app_name);
		final ImageView icon = convertView.findViewById(R.id.app_icon);
		final TextView subTitle = convertView.findViewById(R.id.package_name);

		final String sTitle = mDisplayItems.get(position).getTitle();
		final String key = mDisplayItems.get(position).getKey();

		icon.setTag(position);
		icon.setImageResource(R.mipmap.ic_default);
		Bitmap bmp = BitmapCachedLoader.memoryCache.get(key);
		if (bmp == null)
			(new BitmapCachedLoader(icon, mDisplayItems.get(position), getContext())).executeOnExecutor(this.mPool);
		else
			icon.setImageBitmap(bmp);

		title.setText(sTitle);
		title.setTextColor(mColorPrimary);
		subTitle.setText(key);
		subTitle.setTextColor(mColorSecondary);
		subTitle.setVisibility(mPrefs.getBoolean(Constants.KEY_SHOW_PACKAGE_NAME, false) ? View.VISIBLE : View.GONE);
		convertView.setBackground(mBackground);

		final String pref_key = key + ":" + currentPkgName;
		final CheckBox checkBox = convertView.findViewById(android.R.id.checkbox);
		checkBox.setVisibility(View.VISIBLE);
		checkBox.setChecked(appIsChecked(pref_key));
		checkBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addValue(((CheckBox)v).isChecked(), pref_key);
			}
		});

		return convertView;
	}

	private boolean appIsChecked(String packageName) {
		if (changedItems.containsKey(packageName)) {
			Boolean state = changedItems.get(packageName);
			return state != null && state;
		} else {
			return mPrefs.getBoolean(packageName, false);
		}
	}

	private void addValue(boolean value, String pref_key) {
		if (changedItems.containsKey(pref_key))
			changedItems.remove(pref_key);
		else
			changedItems.put(pref_key, value);
	}

	@Override
	public void sortList() {
		Collections.sort(mDisplayItems, new Comparator<ApplicationData>() {
			public int compare(ApplicationData app1, ApplicationData app2) {
				try {
					boolean app1checked = appIsChecked(app1.getKey() + ":" + currentPkgName);
					boolean app2checked = appIsChecked(app2.getKey() + ":" + currentPkgName);
					if (app1checked == app2checked)
						return app1.compareTo(app2);
					else if (app1checked)
						return -1;
					else
						return 1;
				} catch (Throwable t) {
					return app1.compareTo(app2);
				}
			}
		});
	}

}
