package com.android.volley.toolbox;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by Administrator on 2016/8/3.
 */
public class ImageLruCache implements ImageLoader.ImageCache {

    private static LruCache<String, Bitmap> mMemoryCache;

    private static ImageLruCache lruImageCache;

    private ImageLruCache(){
        // Get the Max available memory
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap){
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    public static ImageLruCache instance(){
        if(lruImageCache == null){
            lruImageCache = new ImageLruCache();
        }
        return lruImageCache;
    }

    @Override
    public Bitmap getBitmap(String arg0) {
        return mMemoryCache.get(arg0);
    }

    @Override
    public void putBitmap(String arg0, Bitmap arg1) {
        if(getBitmap(arg0) == null){
            mMemoryCache.put(arg0, arg1);
        }
    }
}
