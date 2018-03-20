package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.gif.GifRequest;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationFail;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/20.
 */

public class FakeGroupDataSource implements GroupDataSource {

    private static FakeGroupDataSource instance;

    private List<PrivateGroup> mPrivateGroups;

    public static final String AIMI_UUID = "aimi_uuid";
    public static final String NAOMI_UUID = "naomi_uuid";
    public static final String MYSELF_UUID = "myself_uuid";

    private FakeGroupDataSource() {
        mPrivateGroups = new ArrayList<>();
    }

    public static GroupDataSource getInstance() {

        if (instance == null) {
            instance = new FakeGroupDataSource();
        }

        return instance;
    }

    public void setCurrentUser(User currentUser) {

        addTestData(currentUser);

    }

    private void addTestData(User myself) {

        if (!mPrivateGroups.isEmpty())
            return;

        String groupUuid1 = "1";

        String stationID = "stationID";

        List<User> users = new ArrayList<>();

        User aimi = new User();
        aimi.setUserName("Aimi");
        aimi.setUuid(AIMI_UUID);
        users.add(aimi);

        User naomi = new User();
        naomi.setUserName("Naomi");
        naomi.setUuid(NAOMI_UUID);
        users.add(naomi);

//        User myself = new User();
//        myself.setUserName("myself");
//        myself.setUuid(MYSELF_UUID);

        users.add(myself);

        List<UserComment> userComments = new ArrayList<>();

        UserComment userComment = new TextComment(Util.createLocalUUid(), aimi, 1494475200, groupUuid1, stationID, "照片扔进毕业十年聚,请务必放进毕业十年聚，别忘啦");
        userComments.add(userComment);

        userComment = new TextComment(Util.createLocalUUid(), naomi, 1494820800, groupUuid1, stationID, "务必放进毕业十年聚");
        userComments.add(userComment);

        userComment = new TextComment(Util.createLocalUUid(), myself, 1497067200, groupUuid1, stationID, "同学们速度快点");
        userComments.add(userComment);

        userComment = new TextComment(Util.createLocalUUid(), aimi, 1500189121, groupUuid1, stationID, "快点");
        userComments.add(userComment);

        userComment = new TextComment(Util.createLocalUUid(), naomi, 1500189301, groupUuid1, stationID, "来了");
        userComments.add(userComment);

        userComment = new TextComment(Util.createLocalUUid(), aimi, 1500189361, groupUuid1, stationID, "ok");
        userComments.add(userComment);

        userComment = new TextComment(Util.createLocalUUid(), myself, 1500189421, groupUuid1, stationID, "come on");
        userComments.add(userComment);

        userComment = new TextComment(Util.createLocalUUid(), naomi, 1500189481, groupUuid1, stationID, "coming");
        userComments.add(userComment);

        userComment = new TextComment(Util.createLocalUUid(), aimi, 1500189541, groupUuid1, stationID, "waiting");
        userComments.add(userComment);

        userComment = new TextComment(Util.createLocalUUid(), naomi, 1500189601, groupUuid1, stationID, "here");
        userComments.add(userComment);

        List<Pin> pins = new ArrayList<>();

        Pin pin1 = new Pin("1", "testPing1");

        pins.add(pin1);

        Pin pin2 = new Pin("2", "testPing2");

        pins.add(pin2);

        Pin pin3 = new Pin("3", "testPing3");

        pins.add(pin3);

        Pin pin4 = new Pin("4", "testPing4");

        pins.add(pin4);

        String groupName1 = "大学同学";

        PrivateGroup privateGroup1 = new PrivateGroup(groupUuid1, groupName1, myself.getAssociatedWeChatGUID(), stationID, new ArrayList<>(users));

        privateGroup1.addPins(pins);

        privateGroup1.addUserComments(userComments);

        mPrivateGroups.add(privateGroup1);

        String groupUuid2 = "2";
        String groupName2 = "外卖小分队";

        PrivateGroup privateGroup2 = new PrivateGroup(groupUuid2, groupName2, myself.getAssociatedWeChatGUID(), stationID, new ArrayList<>(users));
        privateGroup2.addUserComments(userComments);

        mPrivateGroups.add(privateGroup2);

        String groupUuid3 = "3";
        String groupName3 = "软件学院同学会";

        PrivateGroup privateGroup3 = new PrivateGroup(groupUuid3, groupName3, myself.getAssociatedWeChatGUID(), stationID, new ArrayList<>(users));
        privateGroup3.addUserComments(userComments);

        mPrivateGroups.add(privateGroup3);

        String groupUuid4 = "4";
        String groupName4 = "吃货群";

        PrivateGroup privateGroup4 = new PrivateGroup(groupUuid4, groupName4, myself.getAssociatedWeChatGUID(), stationID, new ArrayList<>(users));
        privateGroup4.addUserComments(userComments);

        mPrivateGroups.add(privateGroup4);

        String groupUuid5 = "5";
        String groupName5 = "校广播站";

        PrivateGroup privateGroup5 = new PrivateGroup(groupUuid5, groupName5, myself.getAssociatedWeChatGUID(), stationID, new ArrayList<>(users));
        privateGroup5.addUserComments(userComments);

        mPrivateGroups.add(privateGroup5);

        String groupUuid6 = "6";
        String groupName6 = "211宿舍派对";

        PrivateGroup privateGroup6 = new PrivateGroup(groupUuid6, groupName6, myself.getAssociatedWeChatGUID(), stationID, new ArrayList<>(users));
        privateGroup6.addUserComments(userComments);

        mPrivateGroups.add(privateGroup6);


    }

    @Override
    public void addGroup(PrivateGroup group, BaseOperateCallback callback) {

        mPrivateGroups.add(group);

        callback.onSucceed();

    }

    @Override
    public void getAllGroups(BaseLoadDataCallback<PrivateGroup> callback) {

        List<PrivateGroup> privateGroups = new ArrayList<>(mPrivateGroups.size());

        for (PrivateGroup privateGroup : mPrivateGroups) {
            privateGroups.add(privateGroup.cloneSelf());
        }

        callback.onSucceed(privateGroups, new OperationSuccess());

    }

    @Override
    public void deleteGroup(GroupRequestParam groupRequestParam, BaseOperateCallback callback) {

    }

    @Override
    public void quitGroup(GroupRequestParam groupRequestParam, String currentUserGUID, BaseOperateCallback callback) {

    }

    @Override
    public void clearGroups() {
        mPrivateGroups.clear();
    }

    @Override
    public void getAllUserCommentByGroupUUID(GroupRequestParam groupRequestParam, BaseLoadDataCallback<UserComment> callback) {

        PrivateGroup originalPrivateGroup = null;

        PrivateGroup result = null;

        String groupUUID = groupRequestParam.getGroupUUID();

        for (PrivateGroup privateGroup : mPrivateGroups) {
            if (privateGroup.getUUID().equals(groupUUID))
                originalPrivateGroup = privateGroup;
        }

        if (originalPrivateGroup != null) {

            result = originalPrivateGroup.cloneSelf();

            callback.onSucceed(result.getUserComments(), new OperationSuccess());
        } else {
            callback.onFail(new OperationFail("result is null"));
        }


    }

    @Override
    public void getUserCommentRange(GroupRequestParam groupRequestParam, long first, long last, int count, BaseLoadDataCallback<UserComment> callback) {

    }

    private PrivateGroup getOriginalGroupByUUID(String groupUUID) {

        for (PrivateGroup privateGroup : mPrivateGroups) {
            if (privateGroup.getUUID().equals(groupUUID))
                return privateGroup;
        }

        return null;

    }


    @Override
    public void insertUserComment(GroupRequestParam groupRequestParam, UserComment userComment, BaseOperateDataCallback<UserComment> callback) {

        String groupUUID = groupRequestParam.getGroupUUID();

        PrivateGroup privateGroup = getOriginalGroupByUUID(groupUUID);

        if (privateGroup != null)
            privateGroup.addUserComment(userComment);

        callback.onSucceed(userComment,new OperationSuccess());

    }

    @Override
    public void updateGroupProperty(GroupRequestParam groupRequestParam, String property, String newValue, BaseOperateCallback callback) {

    }

    @Override
    public void addUsersInGroup(GroupRequestParam groupRequestParam, List<User> users, BaseOperateCallback callback) {

    }

    @Override
    public void deleteUsersInGroup(GroupRequestParam groupRequestParam, List<String> userGUIDs, BaseOperateCallback callback) {

    }

    @Override
    public Pin insertPin(String groupUUID, Pin pin) {

        PrivateGroup privateGroup = getOriginalGroupByUUID(groupUUID);

        if (privateGroup != null)
            privateGroup.addPin(pin);

        return pin;
    }

    @Override
    public boolean modifyPin(String groupUUID, String pinName, String pinUUID) {

        PrivateGroup privateGroup = getOriginalGroupByUUID(groupUUID);

        if (privateGroup != null) {

            Pin originalPin = privateGroup.getPin(pinUUID);

            originalPin.setName(pinName);

            return true;
        }

        return false;
    }

    @Override
    public boolean deletePin(String groupUUID, String pinUUID) {

        PrivateGroup privateGroup = getOriginalGroupByUUID(groupUUID);

        if (privateGroup != null) {

            privateGroup.deletePin(pinUUID);
            return true;

        }

        return false;
    }

    @Override
    public boolean insertFileToPin(Collection<AbstractFile> files, String groupUUID, String pinUUID) {

        PrivateGroup privateGroup = getOriginalGroupByUUID(groupUUID);

        if (privateGroup != null) {

            Pin pin = privateGroup.getPin(pinUUID);

            pin.addFiles(files);

            return true;
        }

        return false;
    }

    @Override
    public boolean insertMediaToPin(Collection<Media> medias, String groupUUID, String pinUUID) {

        PrivateGroup privateGroup = getOriginalGroupByUUID(groupUUID);

        if (privateGroup != null) {

            Pin pin = privateGroup.getPin(pinUUID);

            pin.addMedias(medias);

            return true;
        }

        return false;
    }

    @Override
    public Pin getPinInGroup(String pinUUID, String groupUUID) {

        PrivateGroup privateGroup = getOriginalGroupByUUID(groupUUID);

        if (privateGroup == null)
            return null;

        Pin pin = privateGroup.getPin(pinUUID);

        if (pin == null)
            return null;
        else
            return pin.cloneSelf();


    }

    private Pin getOriginalPinInGroup(String pinUUID, String groupUUID) {

        PrivateGroup privateGroup = getOriginalGroupByUUID(groupUUID);

        if (privateGroup == null)
            return null;

        Pin pin = privateGroup.getPin(pinUUID);

        if (pin == null)
            return null;
        else
            return pin;

    }

    @Override
    public boolean updatePinInGroup(Pin pin, String groupUUID) {

        Pin originalPin = getOriginalPinInGroup(pin.getUuid(), groupUUID);

        if (originalPin == null)
            return false;

        List<Media> originalMedias = originalPin.getMedias();
        List<Media> modifiedMedias = pin.getMedias();

        Iterator<Media> mediaIterator = originalMedias.iterator();

        while (mediaIterator.hasNext()) {

            Media media = mediaIterator.next();

            if (!modifiedMedias.contains(media)) {
                mediaIterator.remove();
            }

        }

        for (Media media : modifiedMedias) {

            if (!originalMedias.contains(media)) {
                originalMedias.add(media);
            }

        }

        List<AbstractFile> originalFiles = originalPin.getFiles();
        List<AbstractFile> modifiedFiles = pin.getFiles();

        Iterator<AbstractFile> fileIterator = originalFiles.iterator();

        while (fileIterator.hasNext()) {
            AbstractFile file = fileIterator.next();

            if (!modifiedFiles.contains(file)) {
                fileIterator.remove();
            }

        }

        for (AbstractFile file : modifiedFiles) {

            if (!originalFiles.contains(file)) {
                originalFiles.add(file);
            }

        }

        return true;
    }


}
