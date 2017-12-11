package com.winsun.fruitmix.equipment.initial.data;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/12/10.
 */

public class InjectInitialEquipmentRepository {

    public static InitialEquipmentRepository provideInstance(Context context) {

        return new InitialEquipmentRepository(ThreadManagerImpl.getInstance(),
                new InitialEquipmentRemoteDataSource(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context)));
    }

}
