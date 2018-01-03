package com.winsun.fruitmix.equipment.maintenance.data;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2018/1/2.
 */

public class InjectMaintenanceDataSource {

    public static MaintenanceDataSource provideInstance(Context context) {
        return new MaintenanceRepository(ThreadManagerImpl.getInstance(), new MaintenanceRemoteDataSource(
                InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context)
        ));
    }

}
