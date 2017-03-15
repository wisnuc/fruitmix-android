package com.android.volley.toolbox;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * Created by Administrator on 2016/8/3.
 */
public class ImageLruCache implements ImageLoader.ImageCache {

    public static final String TAG = ImageLruCache.class.getSimpleName();

    private static LruCache<String, Bitmap> mMemoryCache;

    private static ImageLruCache lruImageCache;

    private ImageLruCache() {
        // Get the Max available memory
        int maxMemory = (int) Runtime.getRuntime().maxMemory();

        int cacheSize = maxMemory / 2;

        Log.i(TAG, "ImageLruCache: cacheSize =" + cacheSize);

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    public static ImageLruCache instance() {
        if (lruImageCache == null) {
            lruImageCache = new ImageLruCache();
        }
        return lruImageCache;
    }

    @Override
    public Bitmap getBitmap(String arg0) {

        if (mMemoryCache.get(arg0) != null)
            Log.d(TAG, "getBitmap: arg0: " + arg0);

        return mMemoryCache.get(arg0);
    }

    @Override
    public void putBitmap(String arg0, Bitmap arg1) {

        if (getBitmap(arg0) == null && arg1.getByteCount() <= 100 * 1024) {

            Log.d(TAG, "putBitmap: arg0:" + arg0 + " size: " + arg1.getByteCount());

            mMemoryCache.put(arg0, arg1);
        }
    }
}
