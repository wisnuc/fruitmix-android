package com.winsun.fruitmix.group;

import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;
import com.winsun.fruitmix.user.User;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/24.
 */

public class UserCommentShowStrategyTest {

    private UserCommentShowStrategy userCommentShowStrategy;

    @Test
    public void testCurrentUserShowStrategy() {

        String currentUserUUID = "testCurrentUserUUID";

        User user = new User();
        user.setUuid(currentUserUUID);

        UserComment preUserComment = new UserComment(user, 0);

        UserComment currentUserComment = new UserComment(user, 0);

        userCommentShowStrategy = new UserCommentShowStrategy(preUserComment, currentUserComment, currentUserUUID);

        assertFalse(userCommentShowStrategy.isShowLeft());

    }

    @Test
    public void testCurrentUserCommentNotEqualsPreUserComment() {

        String preCommentCreatorUUID = "testPreCommentCreatorUUID";
        String currentCommentCreatorUUID = "testCurrentCommentCreatorUUID";

        User preCreator = new User();
        preCreator.setUuid(preCommentCreatorUUID);

        User currentCreator = new User();
        currentCreator.setUuid(currentCommentCreatorUUID);

        userCommentShowStrategy = new UserCommentShowStrategy(new UserComment(preCreator, 0), new UserComment(currentCreator, 0), currentCommentCreatorUUID);

        assertShowUserInfo();

    }

    @Test
    public void testFirstUserComment() {

        userCommentShowStrategy = new UserCommentShowStrategy(null, new UserComment(new User(), 0), "");

        assertShowUserInfo();

    }

    private void assertShowUserInfo() {
        assertTrue(userCommentShowStrategy.isShowUserName());
        assertTrue(userCommentShowStrategy.isShowTime());
        assertTrue(userCommentShowStrategy.isShowUserAvatar());
    }

}
