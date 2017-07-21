package com.winsun.fruitmix.inject;

import android.content.Context;

import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.util.FNAS;

/**
 * Created by Administrator on 2017/7/14.
 */

public class Inject {

    public static IHttpUtil provideIHttpUtil(Context context) {

        return new OkHttpUtil();

//        return new CheckIpHttpUtil(new OkHttpUtil(), HttpRequestFactory.getInstance().getEquipmentName(), new EquipmentSearchManager(context));

    }

    public static HttpRequestFactory provideHttpRequestFactory() {

        return HttpRequestFactory.getInstance(FNAS.JWT,FNAS.Gateway);
    }

}
