package com.winsun.fruitmix.stations;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/9/14.
 */

public class InjectStation {

    public static StationsDataSource provideStationDataSource(Context context) {

        return StationsRepository.getInstance(ThreadManagerImpl.getInstance(),
                StationsRemoteDataSource.getInstance(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context)));

    }

}
