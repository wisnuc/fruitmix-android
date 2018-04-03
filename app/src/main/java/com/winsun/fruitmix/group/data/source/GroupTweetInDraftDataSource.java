package com.winsun.fruitmix.group.data.source;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.GroupUtilKt;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.SystemMessageTextComment;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2018/3/21.
 */

public class GroupTweetInDraftDataSource {

    public static final String TAG = GroupTweetInDraftDataSource.class.getSimpleName();

    private DBUtils mDBUtils;

    public GroupTweetInDraftDataSource(DBUtils DBUtils) {
        mDBUtils = DBUtils;
    }

    public List<UserComment> getAllComments(String groupUUID, String currentUserGUID) {

        return mDBUtils.getUserCommentsInDraft(groupUUID, currentUserGUID);

    }

    public void deleteCommentInDraft(String groupUUID, String currentUserGUID, String realCommentUUID) {

        long result = mDBUtils.deleteRemoteCommentInDraft(realCommentUUID, groupUUID, currentUserGUID);

        Log.d(TAG, "deleteCommentInDraft: " + result);
    }

    public void updateCommentRealUUID(String fakeCommentUUID, String groupUUID, String currentUserGUID, String realCommentUUID) {

        long result = mDBUtils.updateGroupCommentRealUUIDInDraft(fakeCommentUUID, currentUserGUID, groupUUID, realCommentUUID);

        Log.d(TAG, "updateCommentRealUUID: " + result);

    }

    public void updateCommentIsFail(String fakeCommentUUID, String groupUUID, String currentUserGUID, boolean isFail) {

        long result = mDBUtils.updateGroupCommentIsFailInDraft(fakeCommentUUID, currentUserGUID, groupUUID, isFail);

        Log.d(TAG, "updateCommentIsFail: " + result);
    }

    public void insertCommentIntoDraft(String userGUID, UserComment userComment) {

        UserComment actualComment;

        if (userComment instanceof FileComment) {

            List<AbstractFile> files = ((FileComment) userComment).getFiles();

            if (GroupUtilKt.checkFilesAllContainsMedias(files)) {

                actualComment = new MediaComment(userComment.getUuid(), userComment.getCreator(),
                        userComment.getCreateTime(), userComment.getGroupUUID(), userComment.getStationID(),
                        GroupUtilKt.convertFilesToMedias(files));

            } else
                actualComment = userComment;

        } else
            actualComment = userComment;

        actualComment.setFake(true);
        actualComment.setContentJsonStr(createContentJsonStr((TextComment) userComment));

        long result = mDBUtils.insertRemoteGroupTweetsInDraft(userGUID, Collections.singletonList(userComment));

        Log.d(TAG, "insertCommentIntoDraft: " + result);
    }

    private String createContentJsonStr(TextComment textComment) {

        JsonObject root = new JsonObject();

        root.addProperty("uuid", textComment.getUuid());
        root.addProperty("ctime", System.currentTimeMillis());
        root.addProperty("index", textComment.getIndex());
        root.addProperty("comment", textComment.getText());
        root.addProperty("tweeter", textComment.getCreator().getAssociatedWeChatGUID());

        if (textComment instanceof SystemMessageTextComment) {
            root.addProperty("type", "boxmessage");
        } else if (textComment instanceof MediaComment || textComment instanceof FileComment) {

            root.addProperty("type", "list");

            JsonArray list = new JsonArray();

            if (textComment instanceof MediaComment) {

                List<Media> medias = ((MediaComment) textComment).getMedias();

                for (Media media : medias) {

                    JsonObject item = createItem(media.getName(), media.getUuid(), media.getSize());

                    JsonObject metadata = new JsonObject();
                    metadata.addProperty("w", media.getWidth());
                    metadata.addProperty("h", media.getHeight());

                    metadata.addProperty("m", media.getType());

                    metadata.addProperty("date", media.getFormattedTime());

                    item.add("metadata", metadata);

                    list.add(item);
                }


            } else {

                List<AbstractFile> files = ((FileComment) textComment).getFiles();

                for (AbstractFile file : files) {

                    RemoteFile remoteFile = (RemoteFile) file;

                    JsonObject item = createItem(remoteFile.getName(), remoteFile.getUuid(), remoteFile.getSize());

                    item.addProperty("rootUUID", remoteFile.getRootFolderUUID());
                    item.addProperty("dirUUID", remoteFile.getParentFolderUUID());

                    list.add(item);
                }

            }

            root.add("list", list);

        } else
            root.addProperty("type", "text");

        return root.toString();

    }

    private JsonObject createItem(String name, String uuid, long size) {
        JsonObject item = new JsonObject();

        item.addProperty("filename", name);
        item.addProperty("sha256", uuid);
        item.addProperty("size", size);

        return item;
    }


}
