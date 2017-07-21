package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/20.
 */

public class FakeGroupDataSource implements GroupDataSource {



    private List<PrivateGroup> privateGroups;

    public FakeGroupDataSource() {
        privateGroups = new ArrayList<>();
    }

    public void addTestData() {

        if (!privateGroups.isEmpty())
            return;

        List<User> users = new ArrayList<>();

        User aimi = new User();
        aimi.setUserName("Aimi");
        users.add(aimi);

        User naomi = new User();
        naomi.setUserName("Naomi");
        users.add(naomi);

        User myself = new User();
        myself.setUserName("myself");
        users.add(myself);

        List<UserComment> userComments = new ArrayList<>();

        UserComment userComment1 = new TextComment(aimi, 1494475200, "照片扔进毕业十年聚");
        userComments.add(userComment1);

        UserComment userComment2 = new TextComment(naomi, 1494820800, "务必放进毕业十年聚");
        userComments.add(userComment2);

        UserComment userComment3 = new TextComment(myself, 1497067200, "同学们速度快点");
        userComments.add(userComment3);


        String groupName1 = "大学同学";

        PrivateGroup privateGroup1 = new PrivateGroup(groupName1, new ArrayList<>(users));
        privateGroup1.addUserComments(userComments);

        privateGroups.add(privateGroup1);

        String groupName2 = "外卖小分队";

        PrivateGroup privateGroup2 = new PrivateGroup(groupName2, new ArrayList<>(users));
        privateGroup2.addUserComments(userComments);

        privateGroups.add(privateGroup2);

        String groupName3 = "软件学院同学会";

        PrivateGroup privateGroup3 = new PrivateGroup(groupName3, new ArrayList<>(users));
        privateGroup3.addUserComments(userComments);

        privateGroups.add(privateGroup3);

        String groupName4 = "吃货群";

        PrivateGroup privateGroup4 = new PrivateGroup(groupName4, new ArrayList<>(users));
        privateGroup4.addUserComments(userComments);

        privateGroups.add(privateGroup4);

        String groupName5 = "校广播站";

        PrivateGroup privateGroup5 = new PrivateGroup(groupName5, new ArrayList<>(users));
        privateGroup5.addUserComments(userComments);

        privateGroups.add(privateGroup5);

        String groupName6 = "211宿舍派对";

        PrivateGroup privateGroup6 = new PrivateGroup(groupName6, new ArrayList<>(users));
        privateGroup6.addUserComments(userComments);

        privateGroups.add(privateGroup6);


    }

    @Override
    public void addGroup(Collection<PrivateGroup> groups) {
        privateGroups.addAll(groups);
    }

    @Override
    public List<PrivateGroup> getAllGroups() {
        return new ArrayList<>(privateGroups);
    }
}
