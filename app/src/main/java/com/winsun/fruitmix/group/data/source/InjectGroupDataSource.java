package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;

/**
 * Created by Administrator on 2017/8/4.
 */

public class InjectGroupDataSource {

    public static GroupRepository provideGroupRepository() {

        GroupDataSource fakeGroupDataSource = FakeGroupDataSource.getInstance();

        return GroupRepository.getInstance(fakeGroupDataSource, ThreadManagerImpl.getInstance());

    }

}
