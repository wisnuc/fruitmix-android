package com.winsun.fruitmix.model;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.winsun.fruitmix.gif.GifLoader;
import com.winsun.fruitmix.gif.GifLruCache;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/12.
 */

public enum ImageGifLoaderInstance {

    INSTANCE;

    public static final String TAG = ImageGifLoaderInstance.class.getSimpleName();

    private ImageLoader mImageLoader;

    private GifLoader mGifLoader;

    private Map<String, String> headers;

    public ImageLoader getImageLoader(Context context) {

        if (mImageLoader == null) {

            RequestQueue mRequestQueue = RequestQueueInstance.getInstance(context).getRequestQueue();
            mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());

            if (headers == null) {
                headers = new ArrayMap<>();
                headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
                Log.i(TAG, FNAS.JWT);
            }

            mImageLoader.setHeaders(headers);
        }

        return mImageLoader;

    }

    public GifLoader getGifLoader(Context context) {

        if (mGifLoader == null) {

            RequestQueue mRequestQueue = RequestQueueInstance.getInstance(context).getRequestQueue();
            mGifLoader = new GifLoader(mRequestQueue, GifLruCache.instance());

            if (headers == null) {
                headers = new ArrayMap<>();
                headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
                Log.i(TAG, FNAS.JWT);
            }

            mImageLoader.setHeaders(headers);

        }

        return mGifLoader;

    }

}
