package com.winsun.fruitmix.equipment;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;

/**
 * Created by Administrator on 2017/7/27.
 */

public class InjectEquipment {

    public static EquipmentSearchManager provideEquipmentSearchManager(Context context) {
        return EquipmentSearchManager.getInstance(context);
    }

    public static EquipmentRemoteDataSource provideEquipmentRemoteDataSource(Context context) {

        return new EquipmentRemoteDataSource(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory());

    }

}
