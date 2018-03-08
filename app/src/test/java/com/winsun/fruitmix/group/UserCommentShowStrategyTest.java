package com.winsun.fruitmix.group;

import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/24.
 */

public class UserCommentShowStrategyTest {

    private UserCommentShowStrategy userCommentShowStrategy;

    @Test
    public void testCurrentUserShowStrategy() {

        String currentUserGUID = "testCurrentUserUUID";

        User user = new User();
        user.setAssociatedWeChatGUID(currentUserGUID);

        UserComment preUserComment = new UserComment(Util.createLocalUUid(), user, 0, "", "");

        UserComment currentUserComment = new UserComment(Util.createLocalUUid(), user, 0, "", "");

        userCommentShowStrategy = new UserCommentShowStrategy(preUserComment, currentUserComment, currentUserGUID);

        assertFalse(userCommentShowStrategy.isShowLeft());

        assertFalse(userCommentShowStrategy.isShowUserName());
        assertTrue(userCommentShowStrategy.isShowTime());
        assertFalse(userCommentShowStrategy.isShowUserAvatar());

    }

    @Test
    public void testOtherUserShowStrategy() {

        String otherUserGUID = "otherUserGUID";
        String currentUserGUID = "currentUserGUID";

        User otherUser = new User();
        otherUser.setAssociatedWeChatGUID(otherUserGUID);

        UserComment otherUserComment = new UserComment(Util.createLocalUUid(), otherUser, 0, "", "");

        userCommentShowStrategy = new UserCommentShowStrategy(null, otherUserComment, currentUserGUID);

        assertTrue(userCommentShowStrategy.isShowLeft());

        assertTrue(userCommentShowStrategy.isShowUserName());
        assertTrue(userCommentShowStrategy.isShowTime());
        assertTrue(userCommentShowStrategy.isShowUserAvatar());

    }

    @Ignore
    public void testCurrentUserCommentNotEqualsPreUserComment() {

        String preCommentCreatorGUID = "testPreCommentCreatorGUID";
        String currentCommentCreatorGUID = "testCurrentCommentCreatorGUID";

        User preCreator = new User();
        preCreator.setAssociatedWeChatGUID(preCommentCreatorGUID);

        User currentCreator = new User();
        currentCreator.setAssociatedWeChatGUID(currentCommentCreatorGUID);

        userCommentShowStrategy = new UserCommentShowStrategy(new UserComment(Util.createLocalUUid(), preCreator, 0, "", ""), new UserComment(Util.createLocalUUid(), currentCreator, 0, "", ""), currentCommentCreatorGUID);

        assertShowUserInfo();

    }

    @Ignore
    public void testFirstUserComment() {

        userCommentShowStrategy = new UserCommentShowStrategy(null, new UserComment(Util.createLocalUUid(), new User(), 0, "", ""), "");

        assertShowUserInfo();

    }

    private void assertShowUserInfo() {
        assertTrue(userCommentShowStrategy.isShowUserName());
        assertTrue(userCommentShowStrategy.isShowTime());
        assertTrue(userCommentShowStrategy.isShowUserAvatar());
    }

}
