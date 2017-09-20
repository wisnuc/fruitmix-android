package com.winsun.fruitmix.user;

import com.winsun.fruitmix.R;

/**
 * Created by Administrator on 2016/8/26.
 */
public class User {

    public static final String DEFAULT_AVATAR = "defaultAvatar.jpg";

    private String userName;
    private String uuid;
    private String avatar;
    private String email;
    private String defaultAvatar;
    private int defaultAvatarBgColor;
    private String home;
    private String library;
    private boolean admin;

    private String associatedWechatGUID;

    public User() {

        setUserName("");
        setUuid("");
        setAvatar(DEFAULT_AVATAR);
        setEmail("");
        setDefaultAvatar("");
        setHome("");
        setLibrary("");

        setAssociatedWechatGUID("");

    }

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

    public void setDefaultAvatarBgColor(int defaultAvatarBgColor) {
        this.defaultAvatarBgColor = defaultAvatarBgColor;
    }

    public int getDefaultAvatarBgColor() {
        return defaultAvatarBgColor;
    }

    public int getDefaultAvatarBgColorResourceId() {

        switch (defaultAvatarBgColor) {
            case 1:
                return R.drawable.user_portrait_bg_blue;
            case 2:
                return R.drawable.user_portrait_bg_green;
            case 3:
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setAssociatedWechatGUID(String associatedWechatGUID) {
        this.associatedWechatGUID = associatedWechatGUID;
    }

    public String getAssociatedWechatGUID() {
        return associatedWechatGUID;
    }

    public static String generateCreateRemoteUserBody(String userName, String userPassword) {

        return "{\"type\":\"local\",\"username\":\"" + userName + "\",\"password\":\"" + userPassword + "\"}";

    }

}
