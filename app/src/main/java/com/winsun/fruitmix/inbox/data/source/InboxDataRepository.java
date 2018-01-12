package com.winsun.fruitmix.inbox.data.source;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupDataSource;
import com.winsun.fruitmix.inbox.data.model.GroupUserComment;
import com.winsun.fruitmix.inbox.data.source.InboxDataSource;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/11.
 */

public class InboxDataRepository implements InboxDataSource {

    private GroupDataSource mGroupDataSource;

    public InboxDataRepository(GroupDataSource groupDataSource) {
        mGroupDataSource = groupDataSource;
    }

    @Override
    public void getAllGroupInfoAboutUser(String userUUID, BaseLoadDataCallback<GroupUserComment> callback) {

        List<PrivateGroup> groups = mGroupDataSource.getAllGroups();

        List<GroupUserComment> groupUserComments = new ArrayList<>();

        for (PrivateGroup group : groups) {

            List<User> users = group.getUsers();

            boolean find = false;
            for (User user : users) {
                if (user.getUuid().equals(userUUID)) {
                    find = true;
                    break;
                }
            }

            if (find) {

                for (UserComment userComment : group.getUserComments()) {
                    groupUserComments.add(new GroupUserComment(userComment, group.getUUID(), group.getName()));
                }

            }
        }

        callback.onSucceed(groupUserComments, new OperationSuccess());

    }
}
