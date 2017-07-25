package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/20.
 */

public class PrivateGroup {

    private String uuid;

    private String name;

    private List<User> friends;

    private List<UserComment> userComments;

    public PrivateGroup(String uuid, String name, List<User> friends) {
        this.uuid = uuid;
        this.name = name;
        this.friends = friends;

        userComments = new ArrayList<>();
    }

    public String getUUID() {
        return uuid;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void addFriend(User user) {
        friends.add(user);
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

    public UserComment getLastComment() {
        return userComments.get(userComments.size() - 1);
    }

    public String getLastCommentDate() {

        return getLastComment().getDate();

    }


}
