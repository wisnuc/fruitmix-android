package com.winsun.fruitmix.component;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.toolbox.IImageLoadListener;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.UserAvatarBinding;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/9/19.
 */

public class UserAvatar extends FrameLayout {

    public static final String TAG = UserAvatar.class.getSimpleName();

    private TextView avatarTextView;
    private NetworkImageView avatarImageView;

    private UserAvatarBinding binding;

    private String preUrl;

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

        String url = user.getAvatar();

        if (url.equals(User.DEFAULT_AVATAR) || !Patterns.WEB_URL.matcher(url).matches()) {

            avatarTextView.setVisibility(VISIBLE);
            avatarImageView.setVisibility(GONE);

            binding.setUser(user);

        } else {

            //fix bug:show user avatar but use other user avatar

/*            if (preUrl == null)
                preUrl = url;
            else if (preUrl.equals(url)) {

                Log.d(TAG, "setUser: url is as same as pre url,url: " + url);

                return;
            }*/

            avatarImageView.setVisibility(GONE);
            avatarTextView.setVisibility(VISIBLE);

            binding.setUser(user);

            avatarImageView.registerImageLoadListener(new IImageLoadListener() {
                @Override
                public void onImageLoadFinish(String url, View view) {

                    avatarTextView.setVisibility(GONE);
                    avatarImageView.setVisibility(VISIBLE);

                }

                @Override
                public void onImageLoadFail(String url, View view) {

                }
            });

            MediaUtil.startLoadRemoteImageUrl(url, avatarImageView, imageLoader);

        }

    }


}
