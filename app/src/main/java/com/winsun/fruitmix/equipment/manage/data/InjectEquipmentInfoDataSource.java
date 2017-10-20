package com.winsun.fruitmix.equipment.manage.data;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/10/17.
 */

public class InjectEquipmentInfoDataSource {

    public static EquipmentInfoDataSource provideInstance(Context context){
        return new EquipmentInfoRepository(ThreadManagerImpl.getInstance(),
                new EquipmentInfoRemoteDataSource(InjectHttp.provideIHttpUtil(context),InjectHttp.provideHttpRequestFactory(context)));
    }

}
