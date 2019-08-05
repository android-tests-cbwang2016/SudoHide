package com.sudocode.sudohide;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

@SuppressLint("StaticFieldLeak")
public class BitmapCachedLoader extends AsyncTask<Void, Void, Bitmap> {

	public static LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>((int)(Runtime.getRuntime().maxMemory() / 1024) / 2) {
		@Override
		protected int sizeOf(String key, Bitmap icon) {
			if (icon != null)
				return icon.getAllocationByteCount() / 1024;
			else
				return 130 * 130 * 4 / 1024;
		}
	};

	private WeakReference<Object> targetRef;
	private WeakReference<Object> appInfo;
	private Context ctx;
	private int theTag = -1;
	
	public BitmapCachedLoader(Object target, Object info, Context context) {
		targetRef = new WeakReference<Object>(target);
		appInfo = new WeakReference<Object>(info);
		ctx = context.getApplicationContext();
		Object tag = ((ImageView)target).getTag();
		if (tag != null) theTag = (Integer)tag;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		Bitmap bmp = null;
		Drawable icon = null;
		int newIconSize = ctx.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);

		ApplicationData ad = ((ApplicationData)appInfo.get());
		if (ad != null) try {
			if (ad.getKey() == null || ad.getKey().equals("")) return null;
			icon = ctx.getPackageManager().getApplicationIcon(ad.getKey());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		if (icon instanceof BitmapDrawable) {
			bmp = ((BitmapDrawable)icon).getBitmap();
			Matrix matrix = new Matrix();
			matrix.postScale(((float)newIconSize) / bmp.getWidth(), ((float)newIconSize) / bmp.getHeight());
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
			
			//Log.e("mem_left", String.valueOf(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()));
			if (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() > 8 * 1024 * 1024)
				memoryCache.put(ad.getKey(), bmp);
			else
				Runtime.getRuntime().gc();
		}
		
		return bmp;
	}
	
	@Override
	protected void onPostExecute(Bitmap bmp) {
		if (targetRef != null && targetRef.get() != null && bmp != null) {
			Object tag = ((ImageView)targetRef.get()).getTag();
			if (tag != null && theTag == (Integer)tag) {
				ImageView itemIcon = ((ImageView)targetRef.get());
				if (itemIcon != null) itemIcon.setImageBitmap(bmp);
			}
		}
		//Log.e("mem_used", String.valueOf(Helpers.memoryCache.size()) + " KB / " + String.valueOf(Runtime.getRuntime().totalMemory() / 1024) + " KB");
	}
}
