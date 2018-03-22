package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public ViewDataBinding getViewDataBinding(Context context, ViewGroup parent) {

        viewDataBinding = BaseLeftCommentBinding.inflate(LayoutInflater.from(context), parent, false);

        FrameLayout frameLayout = viewDataBinding.commentFramelayout;

        frameLayout.addView(generateContentView(context, frameLayout));

        return viewDataBinding;

    }

    protected abstract View generateContentView(Context context, ViewGroup parent);

    public void refreshCommentView(Context context, View toolbar, UserCommentShowStrategy strategy, UserComment data) {

        viewDataBinding.setVariable(BR.userComment, data);

        viewDataBinding.setVariable(BR.userCommentShowStrategy, strategy);

        viewDataBinding.executePendingBindings();

        UserAvatar userAvatar = viewDataBinding.userAvatar;

        ImageView currentUserIcon = viewDataBinding.currentUserIcon;

        RelativeLayout.LayoutParams userAvatarLayoutParams = (RelativeLayout.LayoutParams) userAvatar.getLayoutParams();

        LinearLayout userInfoLayout = viewDataBinding.userInfoLayout;

        RelativeLayout.LayoutParams userInfoLayoutLayoutParams = (RelativeLayout.LayoutParams) userInfoLayout.getLayoutParams();

        RelativeLayout.LayoutParams commentContentLayoutLayoutParams = (RelativeLayout.LayoutParams) viewDataBinding.commentContent.getLayoutParams();

        if (strategy.isShowLeft()) {

            userAvatar.setVisibility(View.VISIBLE);

            currentUserIcon.setVisibility(View.GONE);

            userAvatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            userAvatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            userInfoLayoutLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.user_avatar);
            userInfoLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, 0);

            commentContentLayoutLayoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.user_avatar);
            commentContentLayoutLayoutParams.addRule(RelativeLayout.LEFT_OF, 0);

            commentContentLayoutLayoutParams.addRule(RelativeLayout.BELOW, R.id.user_info_layout);

            ImageLoader imageLoader = InjectHttp.provideImageGifLoaderInstance(context).getImageLoader(context);

            userAvatar.setUser(data.getCreator(), imageLoader);

            viewDataBinding.commentCreating.setVisibility(View.GONE);
            viewDataBinding.commentStateImageView.setVisibility(View.GONE);

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

            commentContentLayoutLayoutParams.addRule(RelativeLayout.BELOW, R.id.current_user_info_layout);

            if(data.isFake()){

                if (data.isFail())
                    viewDataBinding.commentStateImageView.setVisibility(View.VISIBLE);
                else {

                    //TODO:check in running task contains current user comment,if true show loading,otherwise set fail and update in db

                    viewDataBinding.commentCreating.setVisibility(View.VISIBLE);

                }

            }else {

                viewDataBinding.commentCreating.setVisibility(View.GONE);
                viewDataBinding.commentStateImageView.setVisibility(View.GONE);

            }

        }

        refreshContent(context, toolbar, data, strategy.isShowLeft());

    }

    protected abstract void refreshContent(Context context, View toolbar, UserComment data, boolean isLeftModel);

}
