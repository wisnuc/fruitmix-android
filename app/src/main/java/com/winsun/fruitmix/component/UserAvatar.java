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
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.IImageLoadListener;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.UserAvatarBinding;
import com.winsun.fruitmix.invitation.ConfirmInviteUser;
import com.winsun.fruitmix.user.DefaultCommentUser;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.util.Util;

import org.w3c.dom.Text;

/**
 * Created by Administrator on 2017/9/19.
 */

public class UserAvatar extends FrameLayout {

    public static final String TAG = UserAvatar.class.getSimpleName();

    private ViewGroup avatarTextLayout;
    private NetworkImageView avatarImageView;

    private ImageView avatarDefaultImageView;
    private TextView avatarDefaultTextView;

    private UserAvatarBinding binding;

    private String preUrl;

    private boolean unregisterListenerWhenDetachFromWindow = true;

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

        avatarTextLayout = binding.avatarTextLayout;
        avatarImageView = binding.avatarImageview;

        avatarDefaultImageView = binding.avatarDefaultImageView;
        avatarDefaultTextView = binding.avatarTextview;

    }

    public void setUnregisterListenerWhenDetachFromWindow(boolean unregisterListenerWhenDetachFromWindow) {
        this.unregisterListenerWhenDetachFromWindow = unregisterListenerWhenDetachFromWindow;
    }

    public void setUser(User user, ImageLoader imageLoader) {

        String url = user.getAvatar();

        if (url.equals(User.DEFAULT_AVATAR) || !Patterns.WEB_URL.matcher(url).matches()) {

            if(user instanceof DefaultCommentUser){

                avatarDefaultTextView.setText("");

                avatarImageView.setBackgroundResource(R.drawable.del_user);

                return;

            }

            avatarTextLayout.setVisibility(VISIBLE);
            avatarImageView.setVisibility(INVISIBLE);

            binding.setUser(user);

        } else {

            //fix bug:show user avatar but use other user avatar

/*            if (preUrl == null)
                preUrl = url;
            else if (preUrl.equals(url)) {

                Log.d(TAG, "setUser: url is as same as pre url,url: " + url);

                return;
            }*/

            if (avatarImageView.getCurrentUrl() != null && url.equals(avatarImageView.getCurrentUrl()))
                return;

            avatarImageView.setVisibility(INVISIBLE);
            avatarTextLayout.setVisibility(VISIBLE);

            binding.setUser(user);

            avatarImageView.registerImageLoadListener(new IImageLoadListener() {
                @Override
                public void onImageLoadFinish(String url, View view) {

                    Log.d(TAG, "onImageLoadFinish: " + url);

                    avatarTextLayout.setVisibility(INVISIBLE);
                    avatarImageView.setVisibility(VISIBLE);

                }

                @Override
                public void onImageLoadFail(String url, View view) {

                    Log.d(TAG, "onImageLoadFail: " + url);

                }
            });

            avatarImageView.setUnregisterListenerWhenDetachFromWindow(unregisterListenerWhenDetachFromWindow);

            MediaUtil.startLoadRemoteImageUrl(url, avatarImageView, imageLoader);

        }

    }

}
