package com.winsun.fruitmix.group;

import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.mock.MockThreadManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Created by Administrator on 2017/7/20.
 */

public class GroupRepositoryTest {

    private GroupRepository groupRepository;

    @Mock
    private GroupDataSource groupDataSource;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        groupRepository = GroupRepository.getInstance(groupDataSource,new MockThreadManager());

    }

    @Test
    public void testInsert() {

        groupRepository.insertUserComment("", new UserComment(null, 0), new BaseOperateDataCallbackImpl<UserComment>());

        verify(groupDataSource).insertUserComment(anyString(), any(UserComment.class));

    }


}
