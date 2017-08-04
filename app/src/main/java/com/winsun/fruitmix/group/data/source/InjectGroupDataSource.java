package com.winsun.fruitmix.group.data.source;

/**
 * Created by Administrator on 2017/8/4.
 */

public class InjectGroupDataSource {

    public static GroupRepository provideGroupRepository() {

        GroupDataSource fakeGroupDataSource = FakeGroupDataSource.getInstance();

        return GroupRepository.getInstance(fakeGroupDataSource);

    }

}
