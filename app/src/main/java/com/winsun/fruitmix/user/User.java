package com.winsun.fruitmix.user;

import android.content.Context;

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

    private boolean isFirstUser;

    private boolean disabled;

    private String associatedWeChatGUID;
    private String associatedWeChatUserName;

    public User() {

        setUserName("");
        setUuid("");
        setAvatar(DEFAULT_AVATAR);
        setEmail("");
        setDefaultAvatar("");
        setHome("");
        setLibrary("");

        setAssociatedWeChatGUID("");
        setAssociatedWeChatUserName("");

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

    public boolean isFirstUser() {
        return isFirstUser;
    }

    public void setFirstUser(boolean firstUser) {
        isFirstUser = firstUser;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setAssociatedWeChatGUID(String associatedWeChatGUID) {
        this.associatedWeChatGUID = associatedWeChatGUID;
    }

    public String getAssociatedWeChatGUID() {
        return associatedWeChatGUID;
    }

    public boolean isBoundedWeChat(){
        return getAssociatedWeChatGUID().length() > 0;
    }

    public void setAssociatedWeChatUserName(String associatedWeChatUserName) {
        this.associatedWeChatUserName = associatedWeChatUserName;
    }

    public String getAssociatedWeChatUserName() {
        return associatedWeChatUserName;
    }

    public static String generateCreateRemoteUserBody(String userName, String userPassword) {

        return "{\"type\":\"local\",\"username\":\"" + userName + "\",\"password\":\"" + userPassword + "\"}";

    }

    public String getFormatUserName(Context context) {
        return getFormatUserName(context,20);
    }

    public String getFormatUserName(Context context,int lengthLimit) {
        String userName = getUserName();

        if (userName.length() > lengthLimit) {
            userName = userName.substring(0, lengthLimit);
            userName += context.getString(R.string.android_ellipsize);
        }

        return userName;
    }


    public String getUserType(Context context) {

        if (isFirstUser())
            return context.getString(R.string.super_admin);
        else if (isAdmin())
            return context.getString(R.string.admin);
        else
            return context.getString(R.string.ordinary_user);

    }


}
