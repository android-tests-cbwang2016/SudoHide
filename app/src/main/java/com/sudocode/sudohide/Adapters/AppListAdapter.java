package com.sudocode.sudohide.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.sudocode.sudohide.ApplicationData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

abstract class AppListAdapter extends BaseAdapter implements Filterable {

	private final boolean mShowSystemApps;
	final SharedPreferences mPrefs;
	List<ApplicationData> mDisplayItems = new ArrayList<>();
	Context mContext;
	LayoutInflater mInflater;
	ThreadPoolExecutor mPool;
	private Filter filter;
	int mColorPrimary;
	int mColorSecondary;
	int mColorAccent;
	Drawable mBackground;

	AppListAdapter(Context context, final boolean mShowSystemApps) {
		super();
		this.mContext = context;
		this.mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		this.mShowSystemApps = mShowSystemApps;
		this.mInflater = LayoutInflater.from(context);
		this.obtainStyle();

		int cpuCount = Runtime.getRuntime().availableProcessors();
		mPool = new ThreadPoolExecutor(cpuCount + 1, cpuCount * 2 + 1, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public void obtainStyle() {
		TypedArray a = mContext.obtainStyledAttributes(new TypedValue().data, new int[]{ android.R.attr.textColorPrimary, android.R.attr.textColorSecondary, android.R.attr.colorAccent, android.R.attr.background });
		mColorPrimary = a.getColor(0, Color.BLACK);
		mColorSecondary = a.getColor(1, Color.DKGRAY);
		mColorAccent = a.getColor(2, Color.rgb(200, 44, 44));
		mBackground = a.getDrawable(3);
		a.recycle();
	}

	public void getAppList() {
		final AppListGetter appListGetter = AppListGetter.getInstance(this.mContext);
		appListGetter.setOnDataAvailableListener(new AppListGetter.OnDatAvailableListener() {
			@Override
			public void onDataAvailable() {
				mDisplayItems = appListGetter.getAvailableData(mShowSystemApps);
				sortList();
				AppListAdapter.this.notifyDataSetChanged();
			}
		});
		appListGetter.callOnDataAvailable();
	}

	boolean isShowSystemApps() {
		return mShowSystemApps;
	}

	void setDisplayItems(List<ApplicationData> displayItems) {
		this.mDisplayItems = displayItems;
		sortList();
	}

	Context getContext() {
		return mContext;
	}

	@Override
	public int getCount() {
		return mDisplayItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mDisplayItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mDisplayItems.get(position).hashCode();
	}

	@Override
	public Filter getFilter() {
		if (filter == null) filter = new AppListFilter(this);
		return filter;
	}

	public void sortList() {}

}
