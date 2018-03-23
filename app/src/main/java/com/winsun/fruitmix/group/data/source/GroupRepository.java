package com.winsun.fruitmix.group.data.source;

import android.support.annotation.NonNull;
import android.util.Log;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.eventbus.GetNewCommentFinishedEvent;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.SystemMessageTextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.usecase.HandleUserCommentInDraft;
import com.winsun.fruitmix.media.MediaDataSourceRepository;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.FilterRule;
import com.winsun.fruitmix.util.ItemFilterKt;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/20.
 */

public class GroupRepository extends BaseDataRepository {

    public static final String TAG = GroupRepository.class.getSimpleName();

    private static GroupRepository ourInstance;

    private GroupDataSource mGroupRemoteDataSource;

    private GroupLocalDataSource mGroupLocalDataSource;

    private GroupTweetInDraftDataSource mGroupTweetInDraftDataSource;

    private MediaDataSourceRepository mMediaDataSourceRepository;

    private Map<String, PrivateGroup> mPrivateGroups;

    private String mCurrentUserGUID;

    private HandleUserCommentInDraft mHandleUserCommentInDraft;

    public static GroupRepository getInstance(GroupDataSource groupDataSource, GroupLocalDataSource groupLocalDataSource,
                                              GroupTweetInDraftDataSource groupTweetInDraftDataSource,
                                              ThreadManager threadManager, User currentUser,
                                              MediaDataSourceRepository mediaDataSourceRepository,
                                              HandleUserCommentInDraft handleUserCommentInDraft) {
        if (ourInstance == null)
            ourInstance = new GroupRepository(threadManager, groupDataSource, groupLocalDataSource, groupTweetInDraftDataSource,
                    currentUser, mediaDataSourceRepository, handleUserCommentInDraft);

        return ourInstance;
    }

    public static void destroyInstance() {

        HandleUserCommentInDraft.destroyInstance();

        ourInstance = null;
    }

    public GroupRepository(ThreadManager threadManager, GroupDataSource mGroupRemoteDataSource,
                           GroupLocalDataSource groupLocalDataSource, GroupTweetInDraftDataSource groupTweetInDraftDataSource,
                           User currentUser, MediaDataSourceRepository mediaDataSourceRepository,
                           HandleUserCommentInDraft handleUserCommentInDraft) {
        super(threadManager);
        this.mGroupRemoteDataSource = mGroupRemoteDataSource;
        mGroupLocalDataSource = groupLocalDataSource;

        mGroupTweetInDraftDataSource = groupTweetInDraftDataSource;

        mMediaDataSourceRepository = mediaDataSourceRepository;

        mCurrentUserGUID = currentUser.getAssociatedWeChatGUID();

        mHandleUserCommentInDraft = handleUserCommentInDraft;

        mPrivateGroups = new LinkedHashMap<>();

    }

    public void setCurrentUser(User currentUser) {

        if (mGroupRemoteDataSource instanceof FakeGroupDataSource)
            ((FakeGroupDataSource) mGroupRemoteDataSource).setCurrentUser(currentUser);
    }

    public void getGroupList(final BaseLoadDataCallback<PrivateGroup> callback) {
        getGroupList(true, callback);
    }

    private void getGroupList(final boolean getGroupFromRemote, final BaseLoadDataCallback<PrivateGroup> callback) {

        final BaseLoadDataCallback<PrivateGroup> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                mGroupLocalDataSource.getAllGroups(mCurrentUserGUID, new BaseLoadDataCallback<PrivateGroup>() {
                    @Override
                    public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                        List<PrivateGroup> currentUserGroup = ItemFilterKt.filterItem(data, new FilterRule<PrivateGroup>() {
                            @Override
                            public boolean isFiltered(PrivateGroup item) {

                                for (User user : item.getUsers())
                                    if (user.getAssociatedWeChatGUID().equals(mCurrentUserGUID))
                                        return true;

                                return false;
                            }
                        });

                        if (currentUserGroup.size() != 0) {
                            mPrivateGroups.clear();

                            for (PrivateGroup privateGroup : currentUserGroup) {
                                mPrivateGroups.put(privateGroup.getUUID(), privateGroup);
                            }

                            runOnMainThreadCallback.onSucceed(currentUserGroup, operationResult);

                            if (getGroupFromRemote)
                                getGroupFromRemoteWhenLocalGroupNotEmpty(new HashMap<>(mPrivateGroups));

                        } else {

                            if (getGroupFromRemote)
                                getGroupFromRemoteWhenLocalGroupEmpty();
                            else
                                runOnMainThreadCallback.onSucceed(currentUserGroup, operationResult);

                        }

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        if (getGroupFromRemote)
                            getGroupFromRemoteWhenLocalGroupEmpty();
                        else
                            callback.onFail(operationResult);

                    }
                });

            }

            private void getGroupFromRemoteWhenLocalGroupEmpty() {
                mGroupRemoteDataSource.getAllGroups(new BaseLoadDataCallback<PrivateGroup>() {
                    @Override
                    public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                        for (PrivateGroup privateGroup : data) {

                            PrivateGroup preGroup = mPrivateGroups.get(privateGroup.getUUID());

                            if (preGroup != null)
                                privateGroup.setUnreadCommentCount(preGroup.getUnreadCommentCount());

                        }

                        mPrivateGroups.clear();

                        for (PrivateGroup privateGroup : data) {
                            mPrivateGroups.put(privateGroup.getUUID(), privateGroup);
                        }

                        runOnMainThreadCallback.onSucceed(data, operationResult);

                        handleLocalGroupEmpty();

                        for (final PrivateGroup privateGroup : data) {

                            getNewCommentInGroup(privateGroup.getUUID(), privateGroup.getStationID(),
                                    privateGroup.getLastRetrievedCommentIndex(), new BaseOperateCallbackImpl() {
                                        @Override
                                        public void onSucceed() {
                                            super.onSucceed();

                                            handleFinishGetNewComment(privateGroup.getUUID());
                                        }
                                    });

                        }

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {
                        runOnMainThreadCallback.onFail(operationResult);
                    }
                });
            }

            private void handleLocalGroupEmpty() {
                for (PrivateGroup group : mPrivateGroups.values())
                    mGroupLocalDataSource.addGroup(mCurrentUserGUID, group);
            }

            private void getGroupFromRemoteWhenLocalGroupNotEmpty(final Map<String, PrivateGroup> mLocalGroupMaps) {

                mGroupRemoteDataSource.getAllGroups(new BaseLoadDataCallback<PrivateGroup>() {
                    @Override
                    public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                        List<PrivateGroup> result = generateNeedGetNewCommentGroups(data, mLocalGroupMaps);

                        runOnMainThreadCallback.onSucceed(data, operationResult);

                        for (final PrivateGroup group : result) {
                            getNewCommentInGroup(group.getUUID(), group.getStationID(),
                                    group.getLastRetrievedCommentIndex(), new BaseOperateCallbackImpl() {
                                        @Override
                                        public void onSucceed() {
                                            super.onSucceed();

                                            handleFinishGetNewComment(group.getUUID());
                                        }
                                    });
                        }

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {


                    }
                });

            }

            private List<PrivateGroup> generateNeedGetNewCommentGroups(List<PrivateGroup> remoteGroups, final Map<String, PrivateGroup> mLocalGroupMaps) {

                List<PrivateGroup> needGetNewCommentGroups = new ArrayList<>();

                for (final PrivateGroup remoteGroup : remoteGroups) {

                    PrivateGroup localGroup = mLocalGroupMaps.get(remoteGroup.getUUID());

                    if (localGroup == null) {

                        remoteGroup.setUnreadCommentCount(0);

                        mGroupLocalDataSource.addGroup(mCurrentUserGUID, remoteGroup);

                        needGetNewCommentGroups.add(remoteGroup);

                    } else {

                        remoteGroup.setUnreadCommentCount(localGroup.getUnreadCommentCount());

                        remoteGroup.setLastRetrievedCommentIndex(localGroup.getLastRetrievedCommentIndex());

                        if (remoteGroup.getLastCommentIndex() > localGroup.getLastCommentIndex()) {

                            updateGroup(remoteGroup, localGroup);

                            needGetNewCommentGroups.add(remoteGroup);

                        }

                    }

                    mLocalGroupMaps.remove(remoteGroup.getUUID());

                }

                for (PrivateGroup localGroup : mLocalGroupMaps.values()) {
                    localGroup.setReadOnly(true);
                }

                mPrivateGroups.clear();

                for (PrivateGroup privateGroup : remoteGroups) {
                    mPrivateGroups.put(privateGroup.getUUID(), privateGroup);
                }

                for (PrivateGroup privateGroup : mLocalGroupMaps.values()) {
                    mPrivateGroups.put(privateGroup.getUUID(), privateGroup);
                }

                return needGetNewCommentGroups;

            }

        });

    }

    private void handleFinishGetNewComment(String groupUUID) {

        EventBus.getDefault().postSticky(new GetNewCommentFinishedEvent(Util.GET_NEW_COMMENT_FINISHED,
                new OperationSuccess(), groupUUID));

    }


    private void updateGroup(PrivateGroup remoteGroup, PrivateGroup localGroup) {

        remoteGroup.setUnreadCommentCount(localGroup.getUnreadCommentCount());
        remoteGroup.setLastRetrievedCommentIndex(localGroup.getLastRetrievedCommentIndex());

        mGroupLocalDataSource.updateGroups(mCurrentUserGUID, Collections.singletonList(remoteGroup));

        if (remoteGroup.getLastComment() != null) {

            if (localGroup.getLastComment() != null) {
                mGroupLocalDataSource.updateGroupLastComment(mCurrentUserGUID, remoteGroup.getLastComment());
            } else
                mGroupLocalDataSource.insertGroupLastComment(mCurrentUserGUID, remoteGroup.getLastComment());

        }

    }

    public void getGroupListFromDB(BaseLoadDataCallback<PrivateGroup> callback) {
        getGroupList(false, callback);
    }

    public void updateGroupUnreadCommentCountInDB(String groupUUID, long newUnreadCommentCount) {

        mGroupLocalDataSource.updateGroupUnreadCommentCount(mCurrentUserGUID, groupUUID, newUnreadCommentCount);

    }

    public void getGroupFromMemory(String groupUUID, BaseLoadDataCallback<PrivateGroup> callback) {

        callback.onSucceed(Collections.singletonList(mPrivateGroups.get(groupUUID)), new OperationSuccess());

    }

    public PrivateGroup getGroupFromMemory(String groupUUID) {
        return mPrivateGroups.get(groupUUID);
    }

    public List<PrivateGroup> getAllGroupFromMemory() {
        return new ArrayList<>(mPrivateGroups.values());
    }

    public void refreshGroupInMemory(List<PrivateGroup> newGroups) {

        for (final PrivateGroup newGroup : newGroups) {

            String groupUUID = newGroup.getUUID();

            PrivateGroup currentGroup = mPrivateGroups.get(groupUUID);

            if (currentGroup == null) {

                newGroup.setUnreadCommentCount(0);

                mPrivateGroups.put(groupUUID, newGroup);

                mGroupLocalDataSource.addGroup(mCurrentUserGUID, newGroup);

                getNewCommentInGroup(newGroup.getUUID(), newGroup.getStationID(), newGroup.getLastRetrievedCommentIndex()
                        , new BaseOperateCallbackImpl() {
                            @Override
                            public void onSucceed() {
                                super.onSucceed();
                                handleFinishGetNewComment(newGroup.getUUID());
                            }
                        });

            } else {

                long currentGroupLastCommentIndex = currentGroup.getLastCommentIndex();

                long newGroupLastCommentIndex = newGroup.getLastCommentIndex();

                if (newGroupLastCommentIndex > currentGroupLastCommentIndex) {

/*                    currentGroup.setOwnerGUID(newGroup.getOwnerGUID());
                    currentGroup.setName(newGroup.getName());
                    currentGroup.setStationName(newGroup.getStationName());
                    currentGroup.setStationOnline(newGroup.isStationOnline());
                    currentGroup.setLastComment(newGroup.getLastComment());
                    currentGroup.setStationID(newGroup.getStationID());
                    currentGroup.setModifyTime(newGroup.getModifyTime());
                    currentGroup.setCreateTime(newGroup.getCreateTime());

                    currentGroup.clearUsers();
                    currentGroup.addUsers(newGroup.getUsers());*/

                    updateGroup(newGroup, currentGroup);

                    mPrivateGroups.put(currentGroup.getUUID(), newGroup);

                    getNewCommentInGroup(newGroup.getUUID(), newGroup.getStationID(), newGroup.getLastRetrievedCommentIndex()
                            , new BaseOperateCallbackImpl() {
                                @Override
                                public void onSucceed() {
                                    super.onSucceed();
                                    handleFinishGetNewComment(newGroup.getUUID());
                                }
                            });

                }

            }

        }

    }

    //TODO:refactor get new tweet logic: after get mqtt message,refresh group and then get new tweet,insert into db and then send message to update ui,ui get tweet source only from db

    private synchronized void getNewCommentInGroup(final String groupUUID, String stationID, final long localGroupCommentIndex, final BaseOperateCallback callback) {

        final GroupRequestParam groupRequestParam = new GroupRequestParam(groupUUID, stationID);

        mGroupRemoteDataSource.getUserCommentRange(groupRequestParam, 0, localGroupCommentIndex, 0,
                new BaseLoadDataCallback<UserComment>() {
                    @Override
                    public void onSucceed(List<UserComment> data, OperationResult operationResult) {

                        if (data.size() != 0) {

                            handleGetUserCommentSucceed(data, groupUUID, null);

                            mGroupLocalDataSource.insertUserComment(mCurrentUserGUID, groupRequestParam, data);

                            updateLastRetrieveCommentIndex(getLastCommentIndex(data), groupUUID);

                            handleUserCommentInDraft(data, groupUUID);

                            updateUnreadCommentCount(mCurrentUserGUID, groupRequestParam, callback);

                        } else
                            callback.onSucceed();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        callback.onFail(operationResult);

                    }
                });

    }

    private void updateLastRetrieveCommentIndex(long localGroupCommentIndex, String groupUUID) {
        if (localGroupCommentIndex == -1) {

            mPrivateGroups.get(groupUUID).setLastRetrievedCommentIndex(0);

            mGroupLocalDataSource.updateGroupLastRetrieveCommentIndex(mCurrentUserGUID, groupUUID, 0);

        } else {

            mPrivateGroups.get(groupUUID).setLastRetrievedCommentIndex(localGroupCommentIndex);

            mGroupLocalDataSource.updateGroupLastRetrieveCommentIndex(mCurrentUserGUID, groupUUID, localGroupCommentIndex);

        }
    }

    private long getLastCommentIndex(List<UserComment> userComments) {

        long lastCommentIndex = 0;

        for (UserComment userComment : userComments) {

            if (userComment.getIndex() > lastCommentIndex)
                lastCommentIndex = userComment.getIndex();

        }

        return lastCommentIndex;

    }

    private void updateUnreadCommentCount(final String currentUserGUID, final GroupRequestParam groupRequestParam, final BaseOperateCallback callback) {

        mGroupLocalDataSource.getAllUserCommentByGroupUUID(currentUserGUID, groupRequestParam,
                new BaseLoadDataCallbackImpl<UserComment>() {
                    @Override
                    public void onSucceed(List<UserComment> data, OperationResult operationResult) {
                        super.onSucceed(data, operationResult);

                        List<UserComment> otherUserComments = ItemFilterKt.filterItem(data, new FilterRule<UserComment>() {
                            @Override
                            public boolean isFiltered(UserComment item) {
                                return !item.getCreator().getAssociatedWeChatGUID().equals(currentUserGUID);
                            }
                        });

                        int otherUserCommentSize = otherUserComments.size();

                        PrivateGroup group = mPrivateGroups.get(groupRequestParam.getGroupUUID());

                        if (group != null && group.getUnreadCommentCount() != otherUserCommentSize) {
                            group.setUnreadCommentCount(otherUserCommentSize);

                            updateGroupUnreadCommentCountInDB(group.getUUID(), otherUserCommentSize);
                        }

                        callback.onSucceed();
                    }
                });

    }


    private void handleUserCommentInDraft(List<UserComment> data, String groupUUID) {
        mHandleUserCommentInDraft.handleUserCommentInDraft(groupUUID, mCurrentUserGUID);

        List<UserComment> userCommentsInDraft = mGroupTweetInDraftDataSource.getAllComments(groupUUID, mCurrentUserGUID);

        userCommentsInDraft = ItemFilterKt.filterItem(userCommentsInDraft,
                new FilterRule<UserComment>() {
                    @Override
                    public boolean isFiltered(UserComment item) {
                        return item.getRealUUIDWhenFake() != null && item.getRealUUIDWhenFake().length() > 0;
                    }
                });

        if (userCommentsInDraft.size() > 0) {

            for (UserComment userCommentInDraft : userCommentsInDraft) {

                for (UserComment userComment : data) {
                    if (userComment.getUuid().equals(userCommentInDraft.getRealUUIDWhenFake()))
                        mGroupTweetInDraftDataSource.deleteCommentInDraft(groupUUID, mCurrentUserGUID, userCommentInDraft.getRealUUIDWhenFake());
                }

            }

        }
    }


    public void deleteGroup(final GroupRequestParam groupRequestParam, final BaseOperateCallback callback) {

        final BaseOperateCallback runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mGroupRemoteDataSource.deleteGroup(groupRequestParam, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        mGroupLocalDataSource.deleteGroup(mCurrentUserGUID, groupRequestParam);

                        mPrivateGroups.remove(groupRequestParam.getGroupUUID());

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

    public void quitGroup(final GroupRequestParam groupRequestParam, final String currentUserGUID, final BaseOperateCallback callback) {

        final BaseOperateCallback runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mGroupRemoteDataSource.quitGroup(groupRequestParam, currentUserGUID, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        mGroupLocalDataSource.quitGroup(groupRequestParam, currentUserGUID);

                        mPrivateGroups.remove(groupRequestParam.getGroupUUID());

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

    public void getAllUserCommentByGroupUUID(final GroupRequestParam groupRequestParam, final BaseLoadDataCallback<UserComment> callback) {

        final BaseLoadDataCallback<UserComment> runOnMainThreadCallback = createLoadCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                handleGetNewCommentFinished();

/*
                String groupUUID = groupRequestParam.getGroupUUID();

                PrivateGroup group = mPrivateGroups.get(groupUUID);

                if (group == null) {

                    getNewCommentInGroup(groupUUID, groupRequestParam.getStationID(), -1, new BaseOperateCallback() {
                        @Override
                        public void onSucceed() {

                            handleGetNewCommentFinished();

                        }

                        @Override
                        public void onFail(OperationResult operationResult) {

                            handleGetNewCommentFinished();

                        }
                    });

                } else {

                    getNewCommentInGroup(groupUUID, groupRequestParam.getStationID(), group.getLastRetrievedCommentIndex(), new BaseOperateCallback() {
                        @Override
                        public void onSucceed() {

                            handleGetNewCommentFinished();

                        }

                        @Override
                        public void onFail(OperationResult operationResult) {

                            handleGetNewCommentFinished();

                        }
                    });
                }
*/

            }

            private void handleGetNewCommentFinished() {

                mGroupLocalDataSource.getAllUserCommentByGroupUUID(mCurrentUserGUID, groupRequestParam, new BaseLoadDataCallback<UserComment>() {
                    @Override
                    public void onSucceed(final List<UserComment> data, OperationResult operationResult) {

                        data.addAll(mGroupTweetInDraftDataSource.getAllComments(groupRequestParam.getGroupUUID(), mCurrentUserGUID));

                        mMediaDataSourceRepository.getLocalMediaWithoutThreadChange(new BaseLoadDataCallback<Media>() {
                            @Override
                            public void onSucceed(List<Media> medias, OperationResult operationResult) {

                                handleGetUserCommentSucceed(data, groupRequestParam.getGroupUUID(), medias);

                                runOnMainThreadCallback.onSucceed(data, operationResult);

                            }

                            @Override
                            public void onFail(OperationResult operationResult) {

                                handleGetUserCommentSucceed(data, groupRequestParam.getGroupUUID(), Collections.<Media>emptyList());

                                runOnMainThreadCallback.onSucceed(data, operationResult);
                            }
                        });

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {
                        runOnMainThreadCallback.onFail(operationResult);
                    }
                });
            }
        });

    }

    private void handleGetUserCommentSucceed(List<UserComment> data, String groupUUID,
                                             List<Media> localMedias) {
        PrivateGroup group = getGroupFromMemory(groupUUID);

        List<User> groupUsers = group.getUsers();

        for (UserComment userComment : data) {

            Util.fillUserCommentUser(groupUsers, userComment);

            userComment.setGroupUUID(group.getUUID());
            userComment.setStationID(group.getStationID());

            if (userComment instanceof SystemMessageTextComment)
                ((SystemMessageTextComment) userComment).fillAddOrDeleteUser(groupUsers);
            else if (localMedias != null && userComment instanceof MediaComment)
                checkMediaInCommentIsLocalAndHandle((MediaComment) userComment, localMedias);

        }

    }

    private void checkMediaInCommentIsLocalAndHandle(MediaComment mediaComment, List<Media> localMedias) {

        List<Media> mediasInComment = mediaComment.getMedias();

        Map<String, Media> localMediaMaps = LocalCache.BuildMediaMapKeyIsUUID(localMedias);

        for (Media media : mediasInComment) {

            if (localMediaMaps.containsKey(media.getUuid())) {

                handleMediaIsLocal(localMediaMaps, media);

            }

        }

    }

    private void handleMediaIsLocal(Map<String, Media> localMediaMaps, Media media) {
        Media localMedia = localMediaMaps.get(media.getUuid());

        media.setLocal(true);

        media.setThumb(localMedia.getThumb());
        media.setMiniThumbPath(localMedia.getMiniThumbPath());
        media.setOriginalPhotoPath(localMedia.getOriginalPhotoPath());

    }


    public void addGroup(final PrivateGroup privateGroup, BaseOperateCallback callback) {

        final BaseOperateCallback runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                mGroupRemoteDataSource.addGroup(privateGroup, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

//                        mGroupLocalDataSource.addGroup(mCurrentUserGUID, privateGroup);

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

    public void retryFailUserComment(final GroupRequestParam groupRequestParam, final UserComment userComment, BaseOperateCallback callback) {
        insertUserComment(true, groupRequestParam, userComment, callback);
    }

    public void insertUserComment(final GroupRequestParam groupRequestParam, final UserComment userComment, BaseOperateCallback callback) {
        insertUserComment(false, groupRequestParam, userComment, callback);
    }

    private void insertUserComment(final boolean retryFailUserComment, final GroupRequestParam groupRequestParam, final UserComment userComment, BaseOperateCallback callback) {

        final BaseOperateCallback runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                if (retryFailUserComment) {

                    userComment.setFail(false);

                    mGroupTweetInDraftDataSource.updateCommentIsFail(userComment.getUuid(), groupRequestParam.getGroupUUID(),
                            mCurrentUserGUID, false);

                } else
                    mGroupTweetInDraftDataSource.insertCommentIntoDraft(mCurrentUserGUID, userComment);

                mHandleUserCommentInDraft.insertUserCommentIntoRunningList(userComment);

                runOnMainThreadCallback.onSucceed();

                mGroupRemoteDataSource.insertUserComment(groupRequestParam, userComment, new BaseOperateDataCallback<UserComment>() {
                    @Override
                    public void onSucceed(UserComment data, OperationResult result) {

//                        mGroupLocalDataSource.insertUserComment(mCurrentUserGUID, groupRequestParam, data);

                        mGroupTweetInDraftDataSource.updateCommentRealUUID(userComment.getUuid(), userComment.getGroupUUID(),
                                mCurrentUserGUID, data.getUuid());

                        mHandleUserCommentInDraft.deleteUserCommentFromRunningList(userComment);

//                        runOnMainThreadCallback.onSucceed();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        mGroupTweetInDraftDataSource.updateCommentIsFail(userComment.getUuid(), userComment.getGroupUUID(),
                                mCurrentUserGUID, true);

                        mHandleUserCommentInDraft.deleteUserCommentFromRunningList(userComment);

                        runOnMainThreadCallback.onFail(operationResult);

                    }
                });

            }
        });

    }

    public void updateGroupName(final GroupRequestParam groupRequestParam, final String newValue, final BaseOperateCallback callback) {

        final String property = "name";

        final BaseOperateCallback runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mGroupRemoteDataSource.updateGroupProperty(groupRequestParam, property, newValue, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        PrivateGroup group = mPrivateGroups.get(groupRequestParam.getGroupUUID());

                        group.setName(newValue);

                        mGroupLocalDataSource.updateGroupProperty(mCurrentUserGUID, groupRequestParam, property, newValue);

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
                mGroupRemoteDataSource.addUsersInGroup(groupRequestParam, users, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        PrivateGroup group = mPrivateGroups.get(groupRequestParam.getGroupUUID());

                        group.addUsers(users);

                        mGroupLocalDataSource.addUsersInGroup(mCurrentUserGUID, groupRequestParam, users);

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

        final List<String> userGUIDs = getSelectedUserGUIDs(users);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mGroupRemoteDataSource.deleteUsersInGroup(groupRequestParam, userGUIDs, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        PrivateGroup group = mPrivateGroups.get(groupRequestParam.getGroupUUID());

                        boolean result = group.deleteUsers(users);

                        Log.d(TAG, "onSucceed: delete users in group result: " + result);

                        mGroupLocalDataSource.deleteUsersInGroup(mCurrentUserGUID, groupRequestParam, userGUIDs);

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
                runOnMainThreadCallback.onSucceed(mGroupRemoteDataSource.insertPin(groupUUID, pin), new OperationSuccess());
            }
        });

    }

    public void insertMediaToPin(final Collection<Media> medias, final String groupUUID, final String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                runOnMainThreadCallback.onSucceed(mGroupRemoteDataSource.insertMediaToPin(medias, groupUUID, pinUUID), new OperationSuccess());
            }
        });

    }

    public void insertFileToPin(final Collection<AbstractFile> files, final String groupUUID, final String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                runOnMainThreadCallback.onSucceed(mGroupRemoteDataSource.insertFileToPin(files, groupUUID, pinUUID), new OperationSuccess());
            }
        });

    }

    public void updatePinInGroup(final Pin pin, final String groupUUID, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                boolean result = mGroupRemoteDataSource.updatePinInGroup(pin, groupUUID);

                sendCallback(runOnMainThreadCallback, result);

            }
        });

    }

    public Pin getPinInGroup(String pinUUID, String groupUUID) {

        return mGroupRemoteDataSource.getPinInGroup(pinUUID, groupUUID);

    }

    public void modifyPin(final String groupUUID, final String pinName, final String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                boolean result = mGroupRemoteDataSource.modifyPin(groupUUID, pinName, pinUUID);

                sendCallback(runOnMainThreadCallback, result);

            }
        });

    }

    public void deletePin(final String groupUUID, final String pinUUID, BaseOperateDataCallback<Boolean> callback) {

        final BaseOperateDataCallback<Boolean> runOnMainThreadCallback = createOperateCallbackRunOnMainThread(callback);

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {

                boolean result = mGroupRemoteDataSource.deletePin(groupUUID, pinUUID);

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
