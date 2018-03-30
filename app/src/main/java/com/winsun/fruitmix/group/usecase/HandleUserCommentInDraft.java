package com.winsun.fruitmix.group.usecase;

import android.util.Log;

import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.source.GroupTweetInDraftDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/23.
 */

public class HandleUserCommentInDraft {

    public static final String TAG = HandleUserCommentInDraft.class.getSimpleName();

    private static HandleUserCommentInDraft handleUserCommentInDraft;

    private GroupTweetInDraftDataSource mGroupTweetInDraftDataSource;

    private List<String> runningUserCommentUUIDs;

    private HandleUserCommentInDraft(GroupTweetInDraftDataSource groupTweetInDraftDataSource) {
        mGroupTweetInDraftDataSource = groupTweetInDraftDataSource;

        runningUserCommentUUIDs = new ArrayList<>();

    }

    public static HandleUserCommentInDraft getInstance(GroupTweetInDraftDataSource groupTweetInDraftDataSource) {

        if (handleUserCommentInDraft == null)
            handleUserCommentInDraft = new HandleUserCommentInDraft(groupTweetInDraftDataSource);

        return handleUserCommentInDraft;
    }

    public static void destroyInstance() {
        handleUserCommentInDraft = null;
    }

    public synchronized void handleUserCommentInDraft(String groupUUID, String currentUserGUID) {

        List<UserComment> userComments = mGroupTweetInDraftDataSource.getAllComments(groupUUID, currentUserGUID);

        for (UserComment userComment : userComments) {

            if (userComment.isFake() && !userComment.isFail() && userComment.getRealUUIDWhenFake() == null && !runningUserCommentUUIDs.contains(userComment.getUuid())) {
                userComment.setFail(true);

                mGroupTweetInDraftDataSource.updateCommentIsFail(userComment.getUuid(), groupUUID, currentUserGUID, true);

                Log.d(TAG, "handleUserCommentInDraft updateCommentIsFail commentFakeUUID: " + userComment.getUuid());
            }

        }

    }

    public synchronized void insertUserCommentIntoRunningList(UserComment userComment) {

        runningUserCommentUUIDs.add(userComment.getUuid());

    }

    public synchronized void deleteUserCommentFromRunningList(UserComment userComment) {
        runningUserCommentUUIDs.remove(userComment.getUuid());
    }


}
