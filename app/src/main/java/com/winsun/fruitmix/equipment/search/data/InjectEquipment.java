package com.winsun.fruitmix.equipment.search.data;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/7/27.
 */

public class InjectEquipment {

    public static EquipmentSearchManager provideEquipmentSearchManager(Context context) {
        return EquipmentSearchManager.getInstance(context);
    }

    public static EquipmentDataSource provideEquipmentDataSource(Context context) {

        return EquipmentDataRepository.getInstance(ThreadManagerImpl.getInstance(),new EquipmentRemoteDataSource(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context)));

    }

}
