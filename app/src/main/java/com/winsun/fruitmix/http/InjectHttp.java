package com.winsun.fruitmix.http;

import android.content.Context;

import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.newdesign201804.file.upload.UploadFileInterface;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;

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
//
//        checkIpHttpUtil.setHttpRequestFactory(InjectHttp.provideHttpRequestFactory());

    }

    public static HttpRequestFactory provideHttpRequestFactory(Context context) {

        return HttpRequestFactory.getInstance(InjectSystemSettingDataSource.provideSystemSettingDataSource(context));

    }

    public static IHttpFileUtil provideIHttpFileUtil() {
        return OkHttpUtil.getInstance();
    }

    public static UploadFileInterface provideUploadFileInterface() {
        return OkHttpUtil.getInstance();
    }

    public static ImageGifLoaderInstance provideImageGifLoaderInstance(Context context) {

        return ImageGifLoaderInstance.getInstance(InjectHttp.provideHttpRequestFactory(context));

    }

}
