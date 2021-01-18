package com.sudocode.sudohide.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sudocode.sudohide.ApplicationData;
import com.sudocode.sudohide.BitmapCachedLoader;
import com.sudocode.sudohide.Constants;
import com.sudocode.sudohide.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import static com.sudocode.sudohide.MainActivity.pref;

public class MainAdapter extends AppListAdapter {

	private Set<String> mHidingConfigurationKeySet;

	public MainAdapter(Context context, boolean showSystemApps) {
		super(context, showSystemApps);
		mContext = context;
		mInflater = LayoutInflater.from(context);
		getHideConfig();
		getAppList();
	}

	public void getHideConfig() {
		mHidingConfigurationKeySet = mPrefs.getAll().keySet();
	}

	public String getKey(int position) {
		return mDisplayItems.get(position).getKey();
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
		if (appIsHidden(key)) {
			subTitle.setTextColor(mColorAccent);
			title.setTextColor(mColorAccent);
		}
		convertView.setBackground(mBackground);

		icon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = mContext.getPackageManager().getLaunchIntentForPackage(key);
				if (it == null) return;
				it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(it);
			}
		});

		return convertView;
	}

	private boolean appIsHidden(String packageName) {
		if (pref.getBoolean(packageName + ":" + Constants.KEY_HIDE_FROM_SYSTEM, false)) return true;
		for (String key: mHidingConfigurationKeySet)
		if (key.endsWith(packageName) && mPrefs.getBoolean(key, false)) return true;
		return false;
	}

	@Override
	public void sortList() {
		Collections.sort(mDisplayItems, new Comparator<ApplicationData>() {
			public int compare(ApplicationData app1, ApplicationData app2) {
				try {
					boolean app1hidden = appIsHidden(app1.getKey());
					boolean app2hidden = appIsHidden(app2.getKey());
					if (app1hidden == app2hidden)
						return app1.compareTo(app2);
					else if (app1hidden)
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