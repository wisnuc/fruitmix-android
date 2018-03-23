package com.winsun.fruitmix.group;

import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupLocalDataSource;
import com.winsun.fruitmix.group.data.source.GroupRemoteDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.group.data.source.GroupTweetInDraftDataSource;
import com.winsun.fruitmix.group.usecase.HandleUserCommentInDraft;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.user.User;
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
    private GroupDataSource mGroupDataSource;

    @Mock
    private GroupLocalDataSource groupLocalDataSource;

    @Mock
    private GroupTweetInDraftDataSource mGroupTweetInDraftDataSource;

    @Mock
    private User currentUser;

    @Mock
    private MediaDataSourceRepository mMediaDataSourceRepository;

    @Mock
    private HandleUserCommentInDraft mHandleUserCommentInDraft;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        groupRepository = GroupRepository.getInstance(mGroupDataSource, groupLocalDataSource, mGroupTweetInDraftDataSource,
                new MockThreadManager(), currentUser, mMediaDataSourceRepository,mHandleUserCommentInDraft);

    }

    @Test
    public void testInsert() {

        groupRepository.insertUserComment(new GroupRequestParam("", ""), new UserComment(Util.createLocalUUid(), null, 0, "", ""), new BaseOperateCallbackImpl());

        verify(mGroupDataSource).insertUserComment(any(GroupRequestParam.class), any(UserComment.class), any(BaseOperateDataCallback.class));

    }


}
