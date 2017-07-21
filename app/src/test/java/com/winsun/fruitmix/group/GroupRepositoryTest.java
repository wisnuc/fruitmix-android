package com.winsun.fruitmix.group;

import com.winsun.fruitmix.group.data.source.GroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by Administrator on 2017/7/20.
 */

public class GroupRepositoryTest {

    private GroupRepository groupRepository;

    @Mock
    private GroupDataSource groupDataSource;

    @Test
    public void setup(){

        MockitoAnnotations.initMocks(this);

        groupRepository = GroupRepository.getInstance(groupDataSource);

    }

}
