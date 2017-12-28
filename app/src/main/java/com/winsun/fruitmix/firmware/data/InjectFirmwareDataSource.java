package com.winsun.fruitmix.firmware.data;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/12/28.
 */

public class InjectFirmwareDataSource {

    public static FirmwareDataSource provideInstance(Context context) {
        return new FirmwareRepository(ThreadManagerImpl.getInstance(), new FirmwareRemoteDataSource(InjectHttp.provideIHttpUtil(context),
                InjectHttp.provideHttpRequestFactory(context)));
    }

}
