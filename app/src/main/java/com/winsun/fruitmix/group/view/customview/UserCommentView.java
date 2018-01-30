package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.UserAvatar;
import com.winsun.fruitmix.databinding.BaseLeftCommentBinding;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;
import com.winsun.fruitmix.http.InjectHttp;

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

    public void refreshCommentView(Context context, View toolbar, UserCommentShowStrategy strategy, UserComment data) {

        viewDataBinding.setVariable(BR.userComment, data);

        viewDataBinding.setVariable(BR.userCommentShowStrategy, strategy);

        viewDataBinding.executePendingBindings();

        UserAvatar userAvatar = viewDataBinding.userAvatar;

        ImageView currentUserIcon = viewDataBinding.currentUserIcon;

        RelativeLayout.LayoutParams userAvatarLayoutParams = (RelativeLayout.LayoutParams) userAvatar.getLayoutParams();

        LinearLayout userInfoLayout = viewDataBinding.userInfoLayout;

        RelativeLayout.LayoutParams userInfoLayoutLayoutParams = (RelativeLayout.LayoutParams) userInfoLayout.getLayoutParams();

        FrameLayout commentContentLayout = viewDataBinding.commentContent;

        RelativeLayout.LayoutParams commentContentLayoutLayoutParams = (RelativeLayout.LayoutParams) commentContentLayout.getLayoutParams();

        if (strategy.isShowLeft()) {

            userAvatar.setVisibility(View.VISIBLE);

            currentUserIcon.setVisibility(View.GONE);

            userAvatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            userAvatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            userInfoLayoutLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.user_avatar);
            userInfoLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, 0);

            commentContentLayoutLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.user_avatar);
            commentContentLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, 0);

            ImageLoader imageLoader = InjectHttp.provideImageGifLoaderInstance(context).getImageLoader(context);

            userAvatar.setUser(data.getCreator(),imageLoader);

        } else {

            userAvatar.setVisibility(View.GONE);

            currentUserIcon.setVisibility(View.VISIBLE);

            viewDataBinding.createTime.setVisibility(View.GONE);
            viewDataBinding.userName.setVisibility(View.GONE);

//            userAvatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
//            userAvatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);


            userInfoLayoutLayoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            userInfoLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, R.id.user_avatar);

            commentContentLayoutLayoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
//            commentContentLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, R.id.user_avatar);

            commentContentLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, R.id.current_user_icon);

        }

        refreshContent(context, toolbar, data, strategy.isShowLeft());

    }

    protected abstract void refreshContent(Context context, View toolbar, UserComment data, boolean isLeftModel);

}
