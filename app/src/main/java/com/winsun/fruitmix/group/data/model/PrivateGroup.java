package com.winsun.fruitmix.group.data.model;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/20.
 */

public class PrivateGroup {

    private String uuid;

    private String name;

    private List<User> users;

    private String ownerGUID;

    private List<UserComment> userComments;

    private String stationID;

    private UserComment lastComment;

    private boolean stationOnline;

    private List<Pin> pins;

    private long createTime;
    private long modifyTime;

    public PrivateGroup(String uuid, String name, String ownerGUID) {
        this.uuid = uuid;
        this.name = name;
        this.ownerGUID = ownerGUID;

        userComments = new ArrayList<>();

        pins = new ArrayList<>();

        users = new ArrayList<>();
    }

    public PrivateGroup(String uuid, String name, String ownerGUID, List<User> users) {
        this.uuid = uuid;
        this.name = name;
        this.ownerGUID = ownerGUID;
        this.users = users;

        userComments = new ArrayList<>();

        pins = new ArrayList<>();
    }

    public String getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getNameWithHandleEmptyName(Context context) {

        if (getName().isEmpty()) {

            StringBuilder stringBuilder = new StringBuilder();

            for (User user : getUsers()) {
                stringBuilder.append(user.getUserName());
                stringBuilder.append(",");
            }

            String name = stringBuilder.toString();
            name = name.substring(0, name.length() - 1);

//            if (name.length() > 10) {
//                name = name.substring(0, 10);
//                name += context.getString(R.string.android_ellipsize);
//            }

            return name;

        } else
            return getName();

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerGUID() {
        return ownerGUID;
    }

    public List<User> getUsers() {
        return users;
    }

    public void clearUsers() {
        users.clear();
    }

    public void addUsers(Collection<User> users) {
        this.users.addAll(users);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public boolean deleteUsers(Collection<User> users) {
        return this.users.removeAll(users);
    }

    public List<UserComment> getUserComments() {
        return userComments;
    }

    public void addUserComment(UserComment userComment) {
        userComments.add(userComment);
    }

    public void addUserComments(Collection<UserComment> newUserComments) {
        userComments.addAll(newUserComments);
    }

    public void setLastComment(UserComment lastComment) {
        this.lastComment = lastComment;
    }

    public UserComment getLastComment() {
//        return userComments.size() > 0 ? userComments.get(userComments.size() - 1) : null;

        return lastComment;

    }

    public String getLastCommentDate(Context context) {

        UserComment userComment = getLastComment();

        if (userComment != null) {
            return userComment.getDate(context);
        } else
            return "";

    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public String getStationID() {
        return stationID;
    }

    public boolean isStationOnline() {
        return stationOnline;
    }

    public void setStationOnline(boolean online) {
        stationOnline = online;
    }

    public List<Pin> getPins() {
        return pins;
    }

    public void addPin(Pin pin) {
        pins.add(pin);
    }

    public void addPins(List<Pin> newPins) {
        pins.addAll(newPins);
    }

    public Pin getPin(String pinUUID) {

        List<Pin> pins = getPins();

        for (Pin pin : pins) {
            if (pin.getUuid().equals(pinUUID))
                return pin;
        }

        return null;
    }

    public boolean deletePin(String pinUUID) {

        List<Pin> pins = getPins();

        Iterator<Pin> iterator = pins.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().getUuid().equals(pinUUID)) {
                iterator.remove();
                return true;
            }

        }

        return false;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public PrivateGroup cloneSelf() {

        PrivateGroup privateGroup = new PrivateGroup(getUUID(), getName(), getOwnerGUID(), new ArrayList<>(getUsers()));

        privateGroup.addUserComments(getUserComments());

        for (Pin pin : getPins()) {
            privateGroup.addPin(pin.cloneSelf());
        }

        return privateGroup;
    }


}
