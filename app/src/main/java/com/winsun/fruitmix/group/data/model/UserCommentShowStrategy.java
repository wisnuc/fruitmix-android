package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.util.FNAS;

/**
 * Created by Administrator on 2017/7/20.
 */

public class UserCommentShowStrategy {

    private boolean showUserAvatar = true;
    private boolean showUserName = true;
    private boolean showTime = true;

    private boolean showLeft = false;

    public UserCommentShowStrategy(UserComment preComment, UserComment currentComment, String currentUserUUID) {

        refreshStrategy(preComment, currentComment, currentUserUUID);
    }

    //use strategy to determine show or not

    private void refreshStrategy(UserComment preComment, UserComment currentComment, String currentUserUUID) {

        showLeft = !currentComment.getCreator().getUuid().equals(currentUserUUID);

        if (preComment == null) {
            showUserInfo();
            return;
        }

        if (preComment.getCreator().getUuid().equals(currentComment.getCreator().getUuid())) {
            dismissUserInfo();
        } else {
            showUserInfo();
        }

    }

    private void dismissUserInfo() {
        showUserAvatar = false;
        showUserName = false;
        showTime = false;
    }

    private void showUserInfo() {
        showUserAvatar = true;
        showUserName = true;
        showTime = true;
    }

    public boolean isShowLeft() {
        return showLeft;
    }

    public boolean isShowUserAvatar() {
        return showUserAvatar;
    }

    public boolean isShowUserName() {
        return showUserName;
    }

    public boolean isShowTime() {
        return showTime;
    }
}
