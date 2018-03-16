package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.base.data.BaseDataOperator;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.User;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2018/3/1.
 */

public class GroupDataSourceConditionCheckWrapper implements GroupDataSource {

    private GroupRemoteDataSource mGroupRemoteDataSource;

    private BaseDataOperator mBaseDataOperator;

    public GroupDataSourceConditionCheckWrapper(GroupRemoteDataSource groupRemoteDataSource, BaseDataOperator baseDataOperator) {
        mGroupRemoteDataSource = groupRemoteDataSource;
        mBaseDataOperator = baseDataOperator;
    }

    @Override
    public void addGroup(final PrivateGroup group, final BaseOperateCallback callback) {

        mBaseDataOperator.preConditionCheck(false,new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                mGroupRemoteDataSource.addGroup(group, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {
                        callback.onSucceed();
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        if (mBaseDataOperator.needRetryWhenFail(operationResult))
                            addGroup(group, callback);
                        else
                            callback.onFail(operationResult);

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mGroupRemoteDataSource.addGroup(group, callback);

            }
        });

    }

    @Override
    public void getAllGroups(BaseLoadDataCallback<PrivateGroup> callback) {

        mGroupRemoteDataSource.getAllGroups(callback);

    }

    @Override
    public void deleteGroup(final GroupRequestParam groupRequestParam, final BaseOperateCallback callback) {

        mBaseDataOperator.preConditionCheck(false,new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                mGroupRemoteDataSource.deleteGroup(groupRequestParam, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {
                        callback.onSucceed();
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        if (mBaseDataOperator.needRetryWhenFail(operationResult))
                            deleteGroup(groupRequestParam, callback);
                        else
                            callback.onFail(operationResult);

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mGroupRemoteDataSource.deleteGroup(groupRequestParam, callback);

            }
        });

    }

    @Override
    public void quitGroup(final GroupRequestParam groupRequestParam, final String currentUserGUID, final BaseOperateCallback callback) {

        mBaseDataOperator.preConditionCheck(false,new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                mGroupRemoteDataSource.quitGroup(groupRequestParam, currentUserGUID, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {
                        callback.onSucceed();
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        if (mBaseDataOperator.needRetryWhenFail(operationResult))
                            quitGroup(groupRequestParam, currentUserGUID, callback);
                        else
                            callback.onFail(operationResult);

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mGroupRemoteDataSource.quitGroup(groupRequestParam, currentUserGUID, callback);

            }
        });

    }

    @Override
    public void clearGroups() {

    }

    @Override
    public void getAllUserCommentByGroupUUID(final GroupRequestParam groupRequestParam, final BaseLoadDataCallback<UserComment> callback) {

        mBaseDataOperator.preConditionCheck(false,new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                mGroupRemoteDataSource.getAllUserCommentByGroupUUID(groupRequestParam, new BaseLoadDataCallback<UserComment>() {
                    @Override
                    public void onSucceed(List<UserComment> data, OperationResult operationResult) {
                        callback.onSucceed(data, operationResult);
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        if (mBaseDataOperator.needRetryWhenFail(operationResult))
                            getAllUserCommentByGroupUUID(groupRequestParam, callback);
                        else
                            callback.onFail(operationResult);

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mGroupRemoteDataSource.getAllUserCommentByGroupUUID(groupRequestParam, callback);

            }
        });

    }

    @Override
    public void insertUserComment(final GroupRequestParam groupRequestParam, final UserComment userComment, final BaseOperateCallback callback) {

        mBaseDataOperator.preConditionCheck(false,new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                mGroupRemoteDataSource.insertUserComment(groupRequestParam, userComment, new BaseOperateCallback() {

                    @Override
                    public void onSucceed() {
                        callback.onSucceed();
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        if (mBaseDataOperator.needRetryWhenFail(operationResult))
                            insertUserComment(groupRequestParam, userComment, callback);
                        else
                            callback.onFail(operationResult);

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mGroupRemoteDataSource.insertUserComment(groupRequestParam, userComment, callback);

            }
        });

    }

    @Override
    public void updateGroupProperty(final GroupRequestParam groupRequestParam, final String property, final String newValue, final BaseOperateCallback callback) {

        mBaseDataOperator.preConditionCheck(false,new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                mGroupRemoteDataSource.updateGroupProperty(groupRequestParam, property, newValue, new BaseOperateCallback() {

                    @Override
                    public void onSucceed() {
                        callback.onSucceed();
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        if (mBaseDataOperator.needRetryWhenFail(operationResult))
                            updateGroupProperty(groupRequestParam, property, newValue, callback);
                        else
                            callback.onFail(operationResult);

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mGroupRemoteDataSource.updateGroupProperty(groupRequestParam, property, newValue, callback);

            }
        });

    }

    @Override
    public void addUsersInGroup(final GroupRequestParam groupRequestParam, final List<User> users, final BaseOperateCallback callback) {

        mBaseDataOperator.preConditionCheck(false,new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                mGroupRemoteDataSource.addUsersInGroup(groupRequestParam, users, new BaseOperateCallback() {

                    @Override
                    public void onSucceed() {
                        callback.onSucceed();
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        if (mBaseDataOperator.needRetryWhenFail(operationResult))
                            addUsersInGroup(groupRequestParam, users, callback);
                        else
                            callback.onFail(operationResult);

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mGroupRemoteDataSource.addUsersInGroup(groupRequestParam, users, callback);

            }
        });

    }

    @Override
    public void deleteUsersInGroup(final GroupRequestParam groupRequestParam, final List<String> userGUIDs, final BaseOperateCallback callback) {

        mBaseDataOperator.preConditionCheck(false,new BaseOperateCallback() {
            @Override
            public void onSucceed() {

                mGroupRemoteDataSource.deleteUsersInGroup(groupRequestParam, userGUIDs, new BaseOperateCallback() {

                    @Override
                    public void onSucceed() {
                        callback.onSucceed();
                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        if (mBaseDataOperator.needRetryWhenFail(operationResult))
                            deleteUsersInGroup(groupRequestParam, userGUIDs, callback);
                        else
                            callback.onFail(operationResult);

                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mGroupRemoteDataSource.deleteUsersInGroup(groupRequestParam, userGUIDs, callback);

            }
        });

    }

    @Override
    public Pin insertPin(String groupUUID, Pin pin) {
        return mGroupRemoteDataSource.insertPin(groupUUID, pin);
    }

    @Override
    public boolean modifyPin(String groupUUID, String pinName, String pinUUID) {
        return mGroupRemoteDataSource.modifyPin(groupUUID, pinName, pinUUID);
    }

    @Override
    public boolean deletePin(String groupUUID, String pinUUID) {
        return mGroupRemoteDataSource.deletePin(groupUUID, pinUUID);
    }

    @Override
    public boolean insertMediaToPin(Collection<Media> medias, String groupUUID, String pinUUID) {
        return mGroupRemoteDataSource.insertMediaToPin(medias, groupUUID, pinUUID);
    }

    @Override
    public boolean insertFileToPin(Collection<AbstractFile> files, String groupUUID, String pinUUID) {
        return mGroupRemoteDataSource.insertFileToPin(files, groupUUID, pinUUID);
    }

    @Override
    public boolean updatePinInGroup(Pin pin, String groupUUID) {
        return mGroupRemoteDataSource.updatePinInGroup(pin, groupUUID);
    }

    @Override
    public Pin getPinInGroup(String pinUUID, String groupUUID) {
        return mGroupRemoteDataSource.getPinInGroup(pinUUID, groupUUID);
    }

}
