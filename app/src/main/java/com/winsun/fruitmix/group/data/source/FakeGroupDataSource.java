package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.group.data.model.Ping;
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

    private static FakeGroupDataSource instance;

    private List<PrivateGroup> privateGroups;

    public static final String AIMI_UUID = "aimi_uuid";
    public static final String NAOMI_UUID = "naomi_uuid";
    public static final String MYSELF_UUID = "myself_uuid";

    private FakeGroupDataSource() {
        privateGroups = new ArrayList<>();
    }

    public static GroupDataSource getInstance() {

        if (instance == null) {
            instance = new FakeGroupDataSource();
        }

        instance.addTestData();

        return instance;
    }

    public void addTestData() {

        if (!privateGroups.isEmpty())
            return;

        List<User> users = new ArrayList<>();

        User aimi = new User();
        aimi.setUserName("Aimi");
        aimi.setUuid(AIMI_UUID);
        users.add(aimi);

        User naomi = new User();
        naomi.setUserName("Naomi");
        naomi.setUuid(NAOMI_UUID);
        users.add(naomi);

        User myself = new User();
        myself.setUserName("myself");
        myself.setUuid(MYSELF_UUID);
        users.add(myself);

        List<UserComment> userComments = new ArrayList<>();

        UserComment userComment1 = new TextComment(aimi, 1494475200, "照片扔进毕业十年聚");
        userComments.add(userComment1);

        UserComment userComment2 = new TextComment(naomi, 1494820800, "务必放进毕业十年聚");
        userComments.add(userComment2);

        UserComment userComment3 = new TextComment(myself, 1497067200, "同学们速度快点");
        userComments.add(userComment3);

        List<Ping> pings = new ArrayList<>();

        Ping ping1 = new Ping();
        ping1.name = "testPing1";

        pings.add(ping1);

        Ping ping2 = new Ping();
        ping2.name = "testPing2";

        pings.add(ping2);

        Ping ping3 = new Ping();
        ping3.name = "testPing3";

        pings.add(ping3);

        Ping ping4 = new Ping();
        ping4.name = "testPing4";

        pings.add(ping4);

        String groupName1 = "大学同学";

        PrivateGroup privateGroup1 = new PrivateGroup("1", groupName1, new ArrayList<>(users));

        privateGroup1.addPings(pings);

        privateGroup1.addUserComments(userComments);

        privateGroups.add(privateGroup1);

        String groupName2 = "外卖小分队";

        PrivateGroup privateGroup2 = new PrivateGroup("2", groupName2, new ArrayList<>(users));
        privateGroup2.addUserComments(userComments);

        privateGroups.add(privateGroup2);

        String groupName3 = "软件学院同学会";

        PrivateGroup privateGroup3 = new PrivateGroup("3", groupName3, new ArrayList<>(users));
        privateGroup3.addUserComments(userComments);

        privateGroups.add(privateGroup3);

        String groupUuid4 = "4";
        String groupName4 = "吃货群";

        PrivateGroup privateGroup4 = new PrivateGroup(groupUuid4, groupName4, new ArrayList<>(users));
        privateGroup4.addUserComments(userComments);

        privateGroups.add(privateGroup4);

        String groupName5 = "校广播站";

        PrivateGroup privateGroup5 = new PrivateGroup("5", groupName5, new ArrayList<>(users));
        privateGroup5.addUserComments(userComments);

        privateGroups.add(privateGroup5);

        String groupName6 = "211宿舍派对";

        PrivateGroup privateGroup6 = new PrivateGroup("6", groupName6, new ArrayList<>(users));
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

    @Override
    public void clearGroups() {
        privateGroups.clear();
    }

    @Override
    public PrivateGroup getGroupByUUID(String groupUUID) {

        for (PrivateGroup privateGroup : privateGroups) {
            if (privateGroup.getUUID().equals(groupUUID))
                return privateGroup;
        }

        return null;

    }

    private PrivateGroup getOriginalGroupByUUID(String groupUUID) {

        PrivateGroup originalPrivateGroup = null;

        for (PrivateGroup privateGroup : privateGroups) {
            if (privateGroup.getUUID().equals(groupUUID))
                originalPrivateGroup = privateGroup;
        }

        if (originalPrivateGroup != null) {

            PrivateGroup privateGroup = new PrivateGroup(originalPrivateGroup.getUUID(), originalPrivateGroup.getName(), originalPrivateGroup.getFriends());

            privateGroup.addUserComments(originalPrivateGroup.getUserComments());
            privateGroup.addPings(originalPrivateGroup.getPings());

            return privateGroup;
        }

        return null;

    }


    @Override
    public UserComment insertUserComment(String groupUUID, UserComment userComment) {

        PrivateGroup privateGroup = getOriginalGroupByUUID(groupUUID);

        if (privateGroup != null)
            privateGroup.addUserComment(userComment);

        return userComment;
    }
}
