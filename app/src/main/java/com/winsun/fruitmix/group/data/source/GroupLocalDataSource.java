package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.user.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2018/3/15.
 */

public class GroupLocalDataSource implements GroupDataSource {

    private DBUtils mDBUtils;

    public GroupLocalDataSource(DBUtils DBUtils) {
        mDBUtils = DBUtils;
    }

    @Override
    public void addGroup(PrivateGroup group, BaseOperateCallback callback) {

        long result = mDBUtils.insertRemoteGroups(Collections.singletonList(group));

        handleResult(callback, result);

    }

    @Override
    public void getAllGroups(BaseLoadDataCallback<PrivateGroup> callback) {

        List<PrivateGroup> groups = mDBUtils.getAllPrivateGroup();

        callback.onSucceed(groups, new OperationSuccess());

    }

    @Override
    public void deleteGroup(GroupRequestParam groupRequestParam, BaseOperateCallback callback) {


    }

    @Override
    public void quitGroup(GroupRequestParam groupRequestParam, String currentUserGUID, BaseOperateCallback callback) {

    }

    @Override
    public void clearGroups() {

    }

    @Override
    public void getAllUserCommentByGroupUUID(GroupRequestParam groupRequestParam, BaseLoadDataCallback<UserComment> callback) {


    }

    @Override
    public void insertUserComment(GroupRequestParam groupRequestParam, UserComment userComment, BaseOperateCallback callback) {

        long result = mDBUtils.insertRemoteGroupTweets(Collections.singletonList(userComment));

        handleResult(callback, result);

    }

    @Override
    public void updateGroupProperty(GroupRequestParam groupRequestParam, String property, String newValue, BaseOperateCallback callback) {

        if (property.equals("name")) {

            long result = mDBUtils.updateGroupName(groupRequestParam.getGroupUUID(), newValue);

            handleResult(callback, result);

        }

    }

    private void handleResult(BaseOperateCallback callback, long result) {
        if (result > 0)
            callback.onSucceed();
        else
            callback.onFail(new OperationSQLException());
    }

    @Override
    public void addUsersInGroup(GroupRequestParam groupRequestParam, List<User> users, BaseOperateCallback callback) {

        long result = mDBUtils.insertRemoteGroupUsers(users, groupRequestParam.getGroupUUID());

        handleResult(callback, result);

    }

    @Override
    public void deleteUsersInGroup(GroupRequestParam groupRequestParam, List<String> userGUIDs, BaseOperateCallback callback) {

        long result = mDBUtils.deleteRemoteGroupUsers(userGUIDs, groupRequestParam.getGroupUUID());

        handleResult(callback, result);

    }

    @Override
    public Pin insertPin(String groupUUID, Pin pin) {
        return null;
    }

    @Override
    public boolean modifyPin(String groupUUID, String pinName, String pinUUID) {
        return false;
    }

    @Override
    public boolean deletePin(String groupUUID, String pinUUID) {
        return false;
    }

    @Override
    public boolean insertMediaToPin(Collection<Media> medias, String groupUUID, String pinUUID) {
        return false;
    }

    @Override
    public boolean insertFileToPin(Collection<AbstractFile> files, String groupUUID, String pinUUID) {
        return false;
    }

    @Override
    public boolean updatePinInGroup(Pin pin, String groupUUID) {
        return false;
    }

    @Override
    public Pin getPinInGroup(String pinUUID, String groupUUID) {
        return null;
    }


}
