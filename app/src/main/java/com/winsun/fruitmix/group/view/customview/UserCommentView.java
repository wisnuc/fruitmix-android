package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.View;
import android.widget.FrameLayout;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.BaseLeftCommentBinding;
import com.winsun.fruitmix.group.data.model.UserComment;

/**
 * Created by Administrator on 2017/7/20.
 */

public abstract class UserCommentView<T extends UserComment> {

    private View view;

    public View getView(Context context) {

        view = View.inflate(context, R.layout.base_left_comment, null);

        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.comment_content);

        frameLayout.addView(generateContentView(context));

        return view;

    }

    public abstract View generateContentView(Context context);

    public void refreshCommentView(T data) {

        BaseLeftCommentBinding baseCommentBinding = DataBindingUtil.findBinding(view);

        baseCommentBinding.setUserComment(data);
        baseCommentBinding.executePendingBindings();

        refreshContent(data);


    }

    public abstract void refreshContent(T data);


}
