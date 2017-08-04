package com.winsun.fruitmix.http;

import android.content.Context;

/**
 * Created by Administrator on 2017/7/14.
 */

public class InjectHttp {

    public static IHttpUtil provideIHttpUtil(Context context) {

        return OkHttpUtil.getInstance();

//        CheckIpHttpUtil checkIpHttpUtil = CheckIpHttpUtil.getInstance(OkHttpUtil.getInstance(),
//                InjectEquipmentManger.provideEquipmentSearchManager(context));
//
//        checkIpHttpUtil.setCurrentEquipmentName( InjectLoggedInUser.provideLoggedInUserRepository(context).getCurrentLoggedInUser().getEquipmentName());


    }

    public static HttpRequestFactory provideHttpRequestFactory() {

        return HttpRequestFactory.getInstance();
    }

    public static IHttpFileUtil provideIHttpFileUtil() {
        return OkHttpUtil.getInstance();
    }

    public static ImageGifLoaderInstance provideImageGifLoaderIntance(){

        return ImageGifLoaderInstance.getInstance();

    }

}
