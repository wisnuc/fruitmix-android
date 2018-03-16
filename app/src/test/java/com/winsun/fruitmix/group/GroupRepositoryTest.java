package com.winsun.fruitmix.group;

import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.Util;

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

    @Mock
    private GroupDataSource groupLocalDataSource;

    @Mock
    private MediaDataSourceRepository mMediaDataSourceRepository;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        groupRepository = GroupRepository.getInstance(groupDataSource,groupLocalDataSource,new MockThreadManager(),mMediaDataSourceRepository);

    }

    @Test
    public void testInsert() {

        groupRepository.insertUserComment(new GroupRequestParam("",""), new UserComment(Util.createLocalUUid(),null, 0,"",""), new BaseOperateCallbackImpl());

        verify(groupDataSource).insertUserComment(any(GroupRequestParam.class), any(UserComment.class), any(BaseOperateCallback.class));

    }


}
