package com.winsun.fruitmix.inbox.data.source;

import android.support.annotation.NonNull;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.group.data.model.MultiFileComment;
import com.winsun.fruitmix.group.data.model.MultiPhotoComment;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.SingleFileComment;
import com.winsun.fruitmix.group.data.model.SinglePhotoComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.inbox.data.model.GroupFileComment;
import com.winsun.fruitmix.inbox.data.model.GroupMediaComment;
import com.winsun.fruitmix.inbox.data.model.GroupUserComment;
import com.winsun.fruitmix.inbox.data.source.InboxDataSource;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.user.User;

import java.util.ArrayList;
import java.util.Collections;
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

                    if (userComment instanceof SinglePhotoComment) {

                        groupUserComments.add(new GroupMediaComment(userComment, group.getUUID(), group.getName(),
                                Collections.singletonList(((SinglePhotoComment) userComment).getMedia())));

                    } else if (userComment instanceof MultiPhotoComment) {

                        groupUserComments.add(new GroupMediaComment(userComment, group.getUUID(), group.getName(),
                                ((MultiPhotoComment) userComment).getMedias()));

                    } else if (userComment instanceof SingleFileComment) {

                        groupUserComments.add(new GroupFileComment(userComment, group.getUUID(), group.getName(),
                                Collections.singletonList(((SingleFileComment) userComment).getFile())));

                    } else if (userComment instanceof MultiFileComment) {

                        groupUserComments.add(new GroupFileComment(userComment, group.getUUID(), group.getName(),
                                ((MultiFileComment) userComment).getFiles()));

                    }

                }

            }
        }
        return groupUserComments;
    }
}
