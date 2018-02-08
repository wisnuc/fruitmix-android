package com.winsun.fruitmix.group.data.source;

import android.content.Context;

import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/8/4.
 */

public class InjectGroupDataSource {

    public static GroupRepository provideGroupRepository(Context context) {


        return GroupRepository.getInstance(GroupRemoteDataSource.getInstance(InjectHttp.provideIHttpUtil(context), InjectHttp.provideHttpRequestFactory(context),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(context)), ThreadManagerImpl.getInstance());

//        GroupDataSource fakeGroupDataSource = FakeGroupDataSource.getInstance();
//        return GroupRepository.getInstance(fakeGroupDataSource,ThreadManagerImpl.getInstance());

    }

}
