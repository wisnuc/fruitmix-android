package com.winsun.fruitmix.model;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2016/8/4.
 */
public enum RequestQueueInstance {

    REQUEST_QUEUE_INSTANCE;

    private RequestQueue mRequestQueue;

    RequestQueueInstance() {
        mRequestQueue = Volley.newRequestQueue(Util.APPLICATION_CONTEXT);
    }

    public RequestQueue getmRequestQueue() {
        return mRequestQueue;
    }

}
