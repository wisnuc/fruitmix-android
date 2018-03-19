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

public class GroupLocalDataSource {

    private DBUtils mDBUtils;

    public GroupLocalDataSource(DBUtils DBUtils) {
        mDBUtils = DBUtils;
    }

    public long addGroup(String currentUserGUID, PrivateGroup group) {

        return mDBUtils.insertRemoteGroups(Collections.singletonList(group), currentUserGUID);

    }

    public void getAllGroups(String currentUserGUID, BaseLoadDataCallback<PrivateGroup> callback) {

        List<PrivateGroup> groups = mDBUtils.getAllPrivateGroup(currentUserGUID);

        callback.onSucceed(groups, new OperationSuccess());

    }

    public void insertGroupLastComment(String currentUserGUID, UserComment userComment) {

        mDBUtils.insertRemoteGroupLastComment(currentUserGUID, userComment);

    }

    public void updateGroups(String currentUserGUID, Collection<PrivateGroup> groups) {

        for (PrivateGroup group : groups) {

            mDBUtils.updateGroup(group, currentUserGUID);

        }

    }

    public void updateGroupLastComment(String currentUserGUID, UserComment userComment) {

        mDBUtils.updateGroupLastComment(currentUserGUID, userComment);

    }


    public void deleteGroup(String currentUserGUID, GroupRequestParam groupRequestParam) {

        mDBUtils.deleteAllRemoteGroupUsers(groupRequestParam.getGroupUUID(), currentUserGUID);

    }

    public void quitGroup(GroupRequestParam groupRequestParam, String currentUserGUID) {

        mDBUtils.deleteRemoteGroupUsers(Collections.singletonList(currentUserGUID), groupRequestParam.getGroupUUID(), currentUserGUID);

    }

    public void clearGroups() {

    }

    public void getAllUserCommentByGroupUUID(String currentUserGUID, GroupRequestParam groupRequestParam, BaseLoadDataCallback<UserComment> callback) {

        List<UserComment> userComments = mDBUtils.getUserComments(groupRequestParam.getGroupUUID(), currentUserGUID);

        callback.onSucceed(userComments, new OperationSuccess());

    }

    public long insertUserComment(String currentUserGUID, GroupRequestParam groupRequestParam, Collection<UserComment> userComments) {

        return mDBUtils.insertRemoteGroupTweets(currentUserGUID, userComments);

    }

    public long insertUserComment(String currentUserGUID, GroupRequestParam groupRequestParam, UserComment userComment) {

        return mDBUtils.insertRemoteGroupTweets(currentUserGUID, Collections.singletonList(userComment));

    }

    public long updateGroupProperty(String currentUserGUID, GroupRequestParam groupRequestParam, String property, String newValue) {

        if (property.equals("name")) {

            return mDBUtils.updateGroupName(currentUserGUID, groupRequestParam.getGroupUUID(), newValue);

        }

        return 0;

    }

    private void handleResult(BaseOperateCallback callback, long result) {
        if (result > 0)
            callback.onSucceed();
        else
            callback.onFail(new OperationSQLException());
    }

    public long addUsersInGroup(String currentUserGUID, GroupRequestParam groupRequestParam, List<User> users) {

        return mDBUtils.insertRemoteGroupUsers(users, groupRequestParam.getGroupUUID(), currentUserGUID);

    }

    public long deleteUsersInGroup(String currentUserGUID, GroupRequestParam groupRequestParam, List<String> userGUIDs) {

        return mDBUtils.deleteRemoteGroupUsers(userGUIDs, groupRequestParam.getGroupUUID(), currentUserGUID);

    }


}
