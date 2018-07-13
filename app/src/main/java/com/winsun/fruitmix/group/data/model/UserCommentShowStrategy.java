package com.winsun.fruitmix.group.data.model;

/**
 * Created by Administrator on 2017/7/20.
 */

public class UserCommentShowStrategy {

    private boolean showUserAvatar = true;
    private boolean showUserName = true;
    private boolean showTime = true;

    private boolean showLeft = false;

    public UserCommentShowStrategy(UserComment preComment, UserComment currentComment, String currentUserGUID) {

        refreshStrategy(preComment, currentComment, currentUserGUID);
    }

    //use strategy to determine show or not

    private void refreshStrategy(UserComment preComment, UserComment currentComment, String currentUserGUID) {

        showLeft = !currentComment.getCreator().getAssociatedWeChatGUID().equals(currentUserGUID);

//        showUserInfo();

/*        if (preComment == null) {
            showUserInfo();
            return;
        }

        if (preComment.getCreator().getAssociatedWeChatGUID().equals(currentComment.getCreator().getAssociatedWeChatGUID())) {
            dismissUserInfo();
        } else {
            showUserInfo();
        }*/

        if (showLeft)
            showOtherUserInfo();
        else
            showCurrentUserInfo();

    }

    private void showCurrentUserInfo() {
        showUserAvatar = false;
        showUserName = false;
        showTime = true;
    }

    private void showOtherUserInfo() {
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
