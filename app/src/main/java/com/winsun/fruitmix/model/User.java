package com.winsun.fruitmix.model;

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

    public String getDefaultAvatarBgColor() {
        return defaultAvatarBgColor;
    }

    public void setDefaultAvatarBgColor(String defaultAvatarBgColor) {
        this.defaultAvatarBgColor = defaultAvatarBgColor;
    }
}
