package com.winsun.fruitmix.group.data.model;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.group.view.customview.MultiFileCommentView;
import com.winsun.fruitmix.group.view.customview.SingleFileCommentView;
import com.winsun.fruitmix.group.view.customview.TextCommentView;
import com.winsun.fruitmix.group.view.customview.UserCommentView;

/**
 * Created by Administrator on 2017/7/24.
 */

public class UserCommentViewFactory {

    private static UserCommentViewFactory instance;

    private ImageLoader imageLoader;

    private UserCommentViewFactory(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public static UserCommentViewFactory getInstance(ImageLoader imageLoader) {

        if (instance == null)
            instance = new UserCommentViewFactory(imageLoader);

        return instance;
    }

    private static final int TYPE_TEXT = 1;
    private static final int TYPE_VOICE = 2;
    private static final int TYPE_OTHER = 3;

    private static final int TYPE_SINGLE_FILE = 4;
    private static final int TYPE_MULTIPLE_FILE = 5;

    public int getUserCommentViewType(UserComment userComment) {

        if (userComment instanceof TextComment)
            return TYPE_TEXT;
        else if (userComment instanceof MultiPhotoComment || userComment instanceof MultiFileComment)
            return TYPE_MULTIPLE_FILE;
        else if (userComment instanceof SinglePhotoComment || userComment instanceof SingleFileComment)
            return TYPE_SINGLE_FILE;
        else
            return TYPE_OTHER;

    }

    public UserCommentView createUserCommentView(int type) {

        if (type == TYPE_TEXT)
            return new TextCommentView();
        else if (type == TYPE_MULTIPLE_FILE)
            return new MultiFileCommentView(imageLoader);
        else if (type == TYPE_SINGLE_FILE)
            return new SingleFileCommentView(imageLoader);
        else
            return null;

    }


}
