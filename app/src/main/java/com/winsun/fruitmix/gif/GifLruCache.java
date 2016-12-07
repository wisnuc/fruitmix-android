package com.winsun.fruitmix.gif;

import android.support.v4.util.LruCache;

/**
 * Created by Administrator on 2016/8/3.
 */
public class GifLruCache implements GifLoader.GifCache {

    private static LruCache<String, byte[]> mMemoryCache;

    private static GifLruCache lruImageCache;

    private GifLruCache(){
        // Get the Max available memory
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, byte[]>(cacheSize){
            @Override
            protected int sizeOf(String key, byte[] data){
                return data.length;
            }
        };
    }

    public static GifLruCache instance(){
        if(lruImageCache == null){
            lruImageCache = new GifLruCache();
        }
        return lruImageCache;
    }

    @Override
    public byte[] getData(String arg0) {
        return mMemoryCache.get(arg0);
    }

    @Override
    public void putData(String arg0, byte[] arg1) {
        if(getData(arg0) == null){
            mMemoryCache.put(arg0, arg1);
        }
    }
}
