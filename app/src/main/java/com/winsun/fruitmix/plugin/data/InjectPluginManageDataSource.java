package com.winsun.fruitmix.plugin.data;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/12/22.
 */

public class InjectPluginManageDataSource {

    public static PluginManageDataSource provideInstance(Context context) {
        return new PluginManageRepository(ThreadManagerImpl.getInstance(),
                new PluginManageRemoteDataSource(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context)));
    }

}
