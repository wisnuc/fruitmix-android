package com.winsun.fruitmix.contact.data;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2018/1/27.
 */

public class InjectContactDataSource {

    public static ContactDataSource provideInstance(Context context) {

        return new ContactDataRepository(ThreadManagerImpl.getInstance(),
                new ContactRemoteDataSource(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context)));

    }

}
