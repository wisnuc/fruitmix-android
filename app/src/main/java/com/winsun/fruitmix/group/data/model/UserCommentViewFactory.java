package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.group.view.customview.TextCommentView;
import com.winsun.fruitmix.group.view.customview.UserCommentView;

/**
 * Created by Administrator on 2017/7/24.
 */

public class UserCommentViewFactory {

    private static UserCommentViewFactory instance;

    private UserCommentViewFactory() {
    }

    public static UserCommentViewFactory getInstance() {

        if (instance == null)
            instance = new UserCommentViewFactory();

        return instance;
    }

    private static final int TYPE_TEXT = 1;
    private static final int TYPE_VOICE = 2;
    private static final int TYPE_OTHER = 3;

    public int getUserCommentViewType(UserComment userComment) {

        if (userComment instanceof TextComment)
            return TYPE_TEXT;
        else
            return TYPE_OTHER;

    }

    public UserCommentView createUserCommentView(int type) {

        if (type == TYPE_TEXT)
            return new TextCommentView();
        else
            return null;

    }


}
