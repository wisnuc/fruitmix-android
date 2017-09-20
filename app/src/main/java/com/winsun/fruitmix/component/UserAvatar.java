package com.winsun.fruitmix.component;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.UserAvatarBinding;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.MediaUtil;

/**
 * Created by Administrator on 2017/9/19.
 */

public class UserAvatar extends FrameLayout {

    private TextView avatarTextView;
    private NetworkImageView avatarImageView;

    private UserAvatarBinding binding;

    public UserAvatar(@NonNull Context context) {
        super(context);

        init(context);
    }

    public UserAvatar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public UserAvatar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);

    }

    private void init(@NonNull Context context) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.user_avatar, this, true);

        avatarTextView = binding.avatarTextview;
        avatarImageView = binding.avatarImageview;
    }

    public void setUser(User user, ImageLoader imageLoader) {

        if (user.getAvatar().equals(User.DEFAULT_AVATAR)) {

            avatarTextView.setVisibility(VISIBLE);
            avatarImageView.setVisibility(GONE);

            binding.setUser(user);

        } else {

            avatarImageView.setVisibility(VISIBLE);
            avatarTextView.setVisibility(GONE);

            String url = user.getAvatar();

            MediaUtil.startLoadRemoteImageUrl(url,avatarImageView,imageLoader);

        }

    }


}
