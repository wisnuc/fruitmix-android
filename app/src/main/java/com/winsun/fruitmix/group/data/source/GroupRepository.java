package com.winsun.fruitmix.group.data.source;

import android.support.annotation.NonNull;
import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/20.
 */

public class GroupRepository extends BaseDataRepository {

    public static final String TAG = GroupRepository.class.getSimpleName();

    private static GroupRepository ourInstance;

    private GroupDataSource groupDataSource;

    private String cloudToken;

    private Map<String, PrivateGroup> mPrivateGroups;

    public static GroupRepository getInstance(GroupDataSource groupDataSource, ThreadManager threadManager) {
        if (ourInstance == null)
            ourInstance = new GroupRepository(threadManager, groupDataSource);

        return ourInstance;
    }

    public static void destroyInstance() {
        ourInstance = null;
    }

    public GroupRepository(ThreadManager threadManager, GroupDataSource groupDataSource) {
        super(threadManager);
        this.groupDataSource = groupDataSource;

        mPrivateGroups = new HashMap<>();
    }

    public void setCurrentUser(User currentUser) {

        if (groupDataSource instanceof FakeGroupDataSource)
            ((FakeGroupDataSource) groupDataSource).setCurrentUser(currentUser);
    }

    public void setCloudToken(String cloudToken) {

        this.cloudToken = cloudToken;

        if (groupDataSource instanceof GroupRemoteDataSource)
            ((GroupRemoteDataSource) groupDataSource).setCloudToken(cloudToken);

    }

    public String getCloudToken() {

        return cloudToken;

    }

    public void getGroupList(BaseLoadDataCallback<PrivateGroup> callback) {

        final BaseLoadDataCallback<PrivateGroup> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                groupDataSource.getAllGroups(new BaseLoadDataCallback<PrivateGroup>() {
                    @Override
                    public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                        mPrivateGroups.clear();

                        for (PrivateGroup privateGroup : data) {
                            mPrivateGroups.put(privateGroup.getUUID(), privateGroup);
                        }

                        runOnMainThreadCallback.onSucceed(data, operationResult);
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {
                        runOnMainThreadCallback.onFail(operationResult);
                    }
                });
            }
        });

    }

    public void getGroupFromMemory(String groupUUID, BaseLoadDataCallback<PrivateGroup> callback) {

        callback.onSucceed(Collections.singletonList(mPrivateGroups.get(groupUUID)), new OperationSuccess());

    }

    public PrivateGroup getGroupFromMemory(String groupUUID) {
        return mPrivateGroups.get(groupUUID);
    }

    public List<PrivateGroup> getAllGroupFromMemory(){
        return new ArrayList<>(mPrivateGroups.values());
    }

    public void refreshGroupInMemory(List<PrivateGroup> newGroups){

        for (PrivateGroup newGroup:newGroups){

            String groupUUID = newGroup.getUUID();

            PrivateGroup currentGroup = mPrivateGroups.get(groupUUID);

            if(currentGroup == null)
                mPrivateGroups.put(groupUUID,newGroup);
            else {

                long currentGroupMTime = currentGroup.getModifyTime();

                long newGroupMTime = newGroup.getModifyTime();

                if(newGroupMTime > currentGroupMTime){

                    mPrivateGroups.put(groupUUID,newGroup);

                }

            }

        }

    }


    public void deleteGroup(final GroupRequestParam groupRequestParam, final BaseOperateCallback callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                groupDataSource.deleteGroup(groupRequestParam, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    public void quitGroup(final GroupRequestParam groupRequestParam, final String currentUserGUID, final BaseOperateCallback callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                groupDataSource.quitGroup(groupRequestParam, currentUserGUID, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    public void getAllUserCommentByGroupUUID(final GroupRequestParam groupRequestParam, final BaseLoadDataCallback<UserComment> callback) {

        final BaseLoadDataCallback<UserComment> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                groupDataSource.getAllUserCommentByGroupUUID(groupRequestParam, new BaseLoadDataCallback<UserComment>() {
                    @Override
                    public void onSucceed(List<UserComment> data, OperationResult operationResult) {

                        PrivateGroup group = getGroupFromMemory(groupRequestParam.getGroupUUID());

                        List<User> groupUsers = group.getUsers();

                        for (UserComment userComment : data) {

                            Util.fillUserCommentUser(groupUsers, userComment);

                            userComment.setGroupUUID(group.getUUID());
                            userComment.setStationID(group.getStationID());

                        }

                        runOnMainThreadCallback.onSucceed(data, operationResult);

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {
                        runOnMainThreadCallback.onFail(operationResult);
                    }
                });
            }
        });

    }

    public void addGroup(final PrivateGroup privateGroup, BaseOperateCallback callback) {

        final BaseOperateCallback runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                groupDataSource.addGroup(privateGroup, runOnMainThreadCallback);

            }
        });

    }

    public void insertUserComment(final GroupRequestParam groupRequestParam, final UserComment userComment, BaseOperateCallback callback) {

        final BaseOperateCallback runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                groupDataSource.insertUserComment(groupRequestParam, userComment, runOnMainThreadCallback);

            }
        });

    }

    public void updateGroupName(final GroupRequestParam groupRequestParam, final String newValue, final BaseOperateCallback callback) {

        final BaseOperateCallback runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                groupDataSource.updateGroupProperty(groupRequestParam, "name", newValue, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        PrivateGroup group = mPrivateGroups.get(groupRequestParam.getGroupUUID());

                        group.setName(newValue);

                        runOnMainThreadCallback.onSucceed();
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        runOnMainThreadCallback.onFail(operationResult);

                    }
                });
            }
        });

    }

    public void addUsersToGroup(final GroupRequestParam groupRequestParam, final List<User> users, final BaseOperateCallback callback) {

        final BaseOperateCallback runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                groupDataSource.addUsersInGroup(groupRequestParam, getSelectedUserGUIDs(users), new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        PrivateGroup group = mPrivateGroups.get(groupRequestParam.getGroupUUID());

                        group.addUsers(users);

                        runOnMainThreadCallback.onSucceed();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        runOnMainThreadCallback.onFail(operationResult);

                    }
                });
            }
        });

    }

    public void deleteUsersToGroup(final GroupRequestParam groupRequestParam, final List<User> users, final BaseOperateCallback callback) {

        final BaseOperateCallback runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                groupDataSource.deleteUsersInGroup(groupRequestParam, getSelectedUserGUIDs(users), new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        PrivateGroup group = mPrivateGroups.get(groupRequestParam.getGroupUUID());

                        boolean result = group.deleteUsers(users);

                        Log.d(TAG, "onSucceed: delete users in group result: " + result);

                        runOnMainThreadCallback.onSucceed();
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {


                        runOnMainThreadCallback.onFail(operationResult);
                    }
                });
            }
        });

    }

    @NonNull
    private List<String> getSelectedUserGUIDs(List<User> users) {
        List<String> selectedUserGUID = new ArrayList<>(users.size());

        for (User user : users) {
            selectedUserGUID.add(user.getAssociatedWeChatGUID());
        }
        return selectedUserGUID;
    }

    public void insertPin(final String groupUUID, final Pin pin, BaseOperateDataCallback<Pin> callback) {

        final BaseOperateDataCallback<Pin> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                runOnMainThreadCallback.onSucceed(groupDataSource.insertPin(groupUUID, pin), new OperationSuccess());
            }
        });

    }

    public void insertMediaToPin(final Collection<Media> medias, final String groupUUID, final String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                runOnMainThreadCallback.onSucceed(groupDataSource.insertMediaToPin(medias, groupUUID, pinUUID), new OperationSuccess());
            }
        });

    }

    public void insertFileToPin(final Collection<AbstractFile> files, final String groupUUID, final String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                runOnMainThreadCallback.onSucceed(groupDataSource.insertFileToPin(files, groupUUID, pinUUID), new OperationSuccess());
            }
        });

    }

    public void updatePinInGroup(final Pin pin, final String groupUUID, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                boolean result = groupDataSource.updatePinInGroup(pin, groupUUID);

                sendCallback(runOnMainThreadCallback, result);

            }
        });

    }

    public Pin getPinInGroup(String pinUUID, String groupUUID) {

        return groupDataSource.getPinInGroup(pinUUID, groupUUID);

    }

    public void modifyPin(final String groupUUID, final String pinName, final String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                boolean result = groupDataSource.modifyPin(groupUUID, pinName, pinUUID);

                sendCallback(runOnMainThreadCallback, result);

            }
        });

    }

    public void deletePin(final String groupUUID, final String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                boolean result = groupDataSource.deletePin(groupUUID, pinUUID);

                sendCallback(runOnMainThreadCallback, result);

            }
        });

    }

    private void sendCallback(BaseOperateDataCallback<Boolean> callback, boolean result) {
        if (result)
            callback.onSucceed(true, new OperationSuccess());
        else
            callback.onFail(new OperationSQLException());
    }

}
