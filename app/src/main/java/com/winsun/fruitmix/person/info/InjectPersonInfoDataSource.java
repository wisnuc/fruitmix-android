package com.winsun.fruitmix.person.info;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/10/19.
 */

public class InjectPersonInfoDataSource {

    public static PersonInfoDataSource provideInstance(Context context) {
        return new PersonInfoRepository(ThreadManagerImpl.getInstance(),
                new PersonInfoRemoteDataSource(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context)));
    }

}
