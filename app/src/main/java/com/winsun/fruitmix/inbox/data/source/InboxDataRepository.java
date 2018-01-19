package com.winsun.fruitmix.inbox.data.source;

import android.support.annotation.NonNull;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.inbox.data.model.GroupUserComment;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/11.
 */

public class InboxDataRepository implements InboxDataSource {

    private GroupRepository mGroupDataSource;

    public InboxDataRepository(GroupRepository groupDataSource) {
        mGroupDataSource = groupDataSource;
    }

    @Override
    public void getAllGroupInfoAboutUser(final String userUUID, final BaseLoadDataCallback<GroupUserComment> callback) {

        mGroupDataSource.getGroupList(new BaseLoadDataCallback<PrivateGroup>() {
            @Override
            public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                List<GroupUserComment> groupUserComments = getGroupUserComments(userUUID, data);

                callback.onSucceed(groupUserComments, new OperationSuccess());
            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        });


    }

    @NonNull
    private List<GroupUserComment> getGroupUserComments(String userUUID, List<PrivateGroup> groups) {
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

                    if (userComment instanceof MediaComment) {

                        groupUserComments.add(new GroupUserComment(userComment, group.getUUID(), group.getName()));

                    } else if (userComment instanceof FileComment) {

                        groupUserComments.add(new GroupUserComment(userComment, group.getUUID(), group.getName()));

                    }

                }

            }
        }
        return groupUserComments;
    }
}
