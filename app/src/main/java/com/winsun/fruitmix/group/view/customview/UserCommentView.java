package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.BaseLeftCommentBinding;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;

/**
 * Created by Administrator on 2017/7/20.
 */

public abstract class UserCommentView {

    private BaseLeftCommentBinding viewDataBinding;

    public ViewDataBinding getViewDataBinding(Context context) {

        viewDataBinding = BaseLeftCommentBinding.inflate(LayoutInflater.from(context), null, false);

        View view = viewDataBinding.getRoot();

        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.comment_content);

        frameLayout.addView(generateContentView(context));

        return viewDataBinding;

    }

    protected abstract View generateContentView(Context context);

    public void refreshCommentView(Context context,UserCommentShowStrategy strategy, UserComment data) {

        viewDataBinding.setVariable(BR.userComment, data);
        viewDataBinding.setVariable(BR.userCommentShowStrategy, strategy);

        viewDataBinding.executePendingBindings();

        ImageView userAvatar = viewDataBinding.userAvatar;

        RelativeLayout.LayoutParams userAvatarLayoutParams = (RelativeLayout.LayoutParams) userAvatar.getLayoutParams();

        LinearLayout userInfoLayout = viewDataBinding.userInfoLayout;

        RelativeLayout.LayoutParams userInfoLayoutLayoutParams = (RelativeLayout.LayoutParams) userInfoLayout.getLayoutParams();

        FrameLayout commentContentLayout = viewDataBinding.commentContent;

        RelativeLayout.LayoutParams commentContentLayoutLayoutParams = (RelativeLayout.LayoutParams) commentContentLayout.getLayoutParams();

        if (strategy.isShowLeft()) {

            userAvatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            userAvatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            userInfoLayoutLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.user_avatar);
            userInfoLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, 0);

            commentContentLayoutLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.user_avatar);
            commentContentLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, 0);

        } else {

            userAvatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            userAvatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            userInfoLayoutLayoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            userInfoLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, R.id.user_avatar);

            commentContentLayoutLayoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            commentContentLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, R.id.user_avatar);

        }


        refreshContent(context,data, strategy.isShowLeft());

    }

    protected abstract void refreshContent(Context context,UserComment data, boolean isLeftModel);

}
