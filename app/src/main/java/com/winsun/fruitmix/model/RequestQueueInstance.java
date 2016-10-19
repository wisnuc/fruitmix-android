package com.winsun.fruitmix.model;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Administrator on 2016/8/4.
 */
public class RequestQueueInstance {

    private RequestQueue requestQueue;

    private static RequestQueueInstance instance;

    private RequestQueueInstance(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static RequestQueueInstance getInstance(Context context){
        if(instance == null){
            instance = new RequestQueueInstance(context);
        }
        return instance;
    }


    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

}
