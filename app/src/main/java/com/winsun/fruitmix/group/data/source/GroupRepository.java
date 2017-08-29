package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by Administrator on 2017/7/20.
 */

public class GroupRepository extends BaseDataRepository {

    private static GroupRepository ourInstance;

    private GroupDataSource groupDataSource;

    public static GroupRepository getInstance(GroupDataSource groupDataSource, ThreadManager threadManager) {
        if (ourInstance == null)
            ourInstance = new GroupRepository(threadManager, groupDataSource);
        return ourInstance;
    }

    public GroupRepository(ThreadManager threadManager, GroupDataSource groupDataSource) {
        super(threadManager);
        this.groupDataSource = groupDataSource;
    }

    public void setCurrentUser(User currentUser) {

        if (groupDataSource instanceof FakeGroupDataSource)
            ((FakeGroupDataSource) groupDataSource).setCurrentUser(currentUser);
    }

    public void getGroupList(BaseLoadDataCallback<PrivateGroup> callback) {

        final BaseLoadDataCallback<PrivateGroup> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                runOnMainThreadCallback.onSucceed(groupDataSource.getAllGroups(), new OperationSuccess());
            }
        });


    }

    public PrivateGroup getGroup(String groupUUID) {

        return groupDataSource.getGroupByUUID(groupUUID);

    }

    public void addGroup(PrivateGroup privateGroup, BaseOperateDataCallback<Boolean> callback) {

        groupDataSource.addGroup(Collections.singleton(privateGroup));

        callback.onSucceed(true, new OperationSuccess());

    }


    public void insertUserComment(String groupUUID, UserComment userComment, BaseOperateDataCallback<UserComment> callback) {

        callback.onSucceed(groupDataSource.insertUserComment(groupUUID, userComment), new OperationSuccess());

    }

    public void insertPin(String groupUUID, Pin pin, BaseOperateDataCallback<Pin> callback) {

        callback.onSucceed(groupDataSource.insertPin(groupUUID, pin), new OperationSuccess());

    }

    public void insertMediaToPin(Collection<Media> medias, String groupUUID, String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        callback.onSucceed(groupDataSource.insertMediaToPin(medias, groupUUID, pinUUID), new OperationSuccess());

    }

    public void insertFileToPin(Collection<AbstractFile> files, String groupUUID, String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        callback.onSucceed(groupDataSource.insertFileToPin(files, groupUUID, pinUUID), new OperationSuccess());

    }

    public void updatePinInGroup(Pin pin, String groupUUID, BaseOperateDataCallback<Boolean> callback) {

        boolean result = groupDataSource.updatePinInGroup(pin, groupUUID);

        sendCallback(callback, result);

    }

    public Pin getPinInGroup(String pinUUID, String groupUUID) {

        return groupDataSource.getPinInGroup(pinUUID, groupUUID);

    }

    public void modifyPin(String groupUUID, String pinName, String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        boolean result = groupDataSource.modifyPin(groupUUID, pinName, pinUUID);

        sendCallback(callback, result);
    }

    public void deletePin(String groupUUID, String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        boolean result = groupDataSource.deletePin(groupUUID, pinUUID);

        sendCallback(callback, result);

    }

    private void sendCallback(BaseOperateDataCallback<Boolean> callback, boolean result) {
        if (result)
            callback.onSucceed(true, new OperationSuccess());
        else
            callback.onFail(new OperationSQLException());
    }

}
