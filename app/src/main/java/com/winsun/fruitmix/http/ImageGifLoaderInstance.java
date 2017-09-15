package com.winsun.fruitmix.http;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLruCache;
import com.winsun.fruitmix.gif.GifLoader;
import com.winsun.fruitmix.gif.GifLruCache;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.RequestQueueInstance;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import java.util.Map;

/**
 * Created by Administrator on 2017/1/12.
 */

public class ImageGifLoaderInstance {

    public static final String TAG = ImageGifLoaderInstance.class.getSimpleName();

    private ImageLoader mImageLoader;

    private GifLoader mGifLoader;

    private Map<String, String> headers;

    private static ImageGifLoaderInstance instance;

    private String token;

    public static ImageGifLoaderInstance getInstance() {

        if (instance == null)
            instance = new ImageGifLoaderInstance();

        return instance;
    }

    public static void destroyInstance() {

        Log.d(TAG, "destroyInstance: ");

        instance = null;
    }

    public void setToken(String token) {

        Log.d(TAG, "setToken: " + token);

        this.token = token;

        if (mImageLoader != null) {

            Log.d(TAG, "setToken: create header");

            createHeader();

            mImageLoader.setHeaders(headers);

            if (mGifLoader != null) {
                mGifLoader.setHeaders(headers);
            }

        }

    }

    public ImageLoader getImageLoader(Context context) {

        if (mImageLoader == null) {

            RequestQueue mRequestQueue = RequestQueueInstance.getInstance(context).getRequestQueue();
            mImageLoader = new ImageLoader(mRequestQueue, ImageLruCache.instance());

            if (headers == null) {

                Log.d(TAG, "getImageLoader: create header");

                createHeader();
            }

            mImageLoader.setHeaders(headers);
        }

        return mImageLoader;

    }

    private void createHeader() {
        headers = new ArrayMap<>();
        headers.put(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + LoginUseCase.getToken());
        Log.i(TAG, "FNAS JWT: " + LoginUseCase.getToken());
    }

    public GifLoader getGifLoader(Context context) {

        if (mGifLoader == null) {

            RequestQueue mRequestQueue = RequestQueueInstance.getInstance(context).getRequestQueue();
            mGifLoader = new GifLoader(mRequestQueue, GifLruCache.instance());

            if (headers == null) {

                Log.d(TAG, "getGifLoader: create header");

                createHeader();
            }

            mImageLoader.setHeaders(headers);

        }

        return mGifLoader;

    }

}
