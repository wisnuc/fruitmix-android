package com.winsun.fruitmix.model;

import com.winsun.fruitmix.R;

/**
 * Created by Administrator on 2016/8/26.
 */
public class User {

    private String userName;
    private String uuid;
    private String avatar;
    private String email;
    private String defaultAvatar;
    private String defaultAvatarBgColor;
    private String home;
    private String library;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDefaultAvatar() {
        return defaultAvatar;
    }

    public void setDefaultAvatar(String defaultAvatar) {
        this.defaultAvatar = defaultAvatar;
    }

    public void setDefaultAvatarBgColor(String defaultAvatarBgColor) {
        this.defaultAvatarBgColor = defaultAvatarBgColor;
    }

    public String getDefaultAvatarBgColor() {
        return defaultAvatarBgColor;
    }

    public int getDefaultAvatarBgColorResourceId() {

        int color = Integer.parseInt(defaultAvatarBgColor);
        switch (color) {
            case 0:
                return R.drawable.user_portrait_bg_blue;
            case 1:
                return R.drawable.user_portrait_bg_green;
            case 2:
                return R.drawable.user_portrait_bg_yellow;
            default:
                return R.drawable.user_portrait_bg_blue;
        }

    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }
}
