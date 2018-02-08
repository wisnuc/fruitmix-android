package com.winsun.fruitmix.group.data.source;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractLocalFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.group.data.model.FileComment;
import com.winsun.fruitmix.group.data.model.MediaComment;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.FileFormData;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.http.TextFormData;
import com.winsun.fruitmix.http.request.factory.CloudHttpRequestFactory;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteGroupParser;
import com.winsun.fruitmix.parser.RemoteUserCommentParser;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2018/1/19.
 */

public class GroupRemoteDataSource extends BaseRemoteDataSourceImpl implements GroupDataSource {

    public static final String TAG = GroupRemoteDataSource.class.getSimpleName();

    private static final String BOXES = "/boxes";
    public static final String TWEETS = "/tweets";
    public static final String METADATA_TRUE = "?metadata=true";
    public static final String INDRIVE = "/indrive";

    private static GroupDataSource instance;

    private String cloudToken;

    private SystemSettingDataSource mSystemSettingDataSource;

    private GroupRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory, SystemSettingDataSource systemSettingDataSource) {
        super(iHttpUtil, httpRequestFactory);

        mSystemSettingDataSource = systemSettingDataSource;
    }

    public static GroupDataSource getInstance(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory,
                                              SystemSettingDataSource systemSettingDataSource) {
        if (instance == null)
            instance = new GroupRemoteDataSource(iHttpUtil, httpRequestFactory, systemSettingDataSource);
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    public void setCloudToken(String cloudToken) {
        this.cloudToken = cloudToken;
    }

    @Override
    public void addGroup(PrivateGroup group, BaseOperateCallback callback) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", group.getName());

        List<User> users = group.getUsers();

        JsonArray jsonArray = new JsonArray(users.size());

        for (User user : users) {
            jsonArray.add(user.getAssociatedWeChatGUID());
        }

        jsonObject.add("users", jsonArray);

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(BOXES, jsonObject.toString(), getAuthorizationValue());

        wrapper.operateCall(httpRequest, callback);

    }

    @Override
    public void getAllGroups(BaseLoadDataCallback<PrivateGroup> callback) {

        String authorizationValue = getAuthorizationValue();

        HttpRequest httpRequest;

        if (mSystemSettingDataSource.getCurrentWAToken().length() != 0) {
            httpRequest = httpRequestFactory.createHttpGetRequestByCloudAPIWithoutWrap(CloudHttpRequestFactory.CLOUD_API_LEVEL + BOXES);
        } else
            httpRequest = httpRequestFactory.createHttpGetRequest(BOXES, authorizationValue);

        wrapper.loadCall(httpRequest, callback, new RemoteGroupParser());

    }

    @NonNull
    private String getAuthorizationValue() {
        String token = httpRequestFactory.getTokenForHeaderValue();

        if (token.startsWith(Util.KEY_JWT_HEAD)) {
            token = token.substring(4, token.length());
        }

        return Util.KEY_JWT_HEAD + cloudToken + " " + token;
    }

    @Override
    public void clearGroups() {

    }

    @Override
    public void deleteGroup(GroupRequestParam groupRequestParam, BaseOperateCallback callback) {

        String authorizationValue = getAuthorizationValue();

        HttpRequest httpRequest;

        httpRequest = httpRequestFactory.createHttpDeleteRequestByCloudAPIWithWrap(BOXES + "/" + groupRequestParam.getGroupUUID(), "",
                groupRequestParam.getStationID());

        wrapper.operateCall(httpRequest, callback);

    }

    @Override
    public void quitGroup(GroupRequestParam groupRequestParam, String currentUserGUID, BaseOperateCallback callback) {

        JsonObject root = getAddDeleteUserBody("delete", Collections.singletonList(currentUserGUID));

        String httpPath = BOXES + "/" + groupRequestParam.getGroupUUID();

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequestByCloudAPIWithWrap(httpPath, root.toString(), groupRequestParam.getStationID());

        wrapper.operateCall(httpRequest, callback);

    }

    @Override
    public void getAllUserCommentByGroupUUID(GroupRequestParam groupRequestParam, BaseLoadDataCallback<UserComment> callback) {

        String httpPath = BOXES + "/" + groupRequestParam.getGroupUUID() + TWEETS + METADATA_TRUE;

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequestByCloudAPIWithWrap(httpPath, groupRequestParam.getStationID());

        wrapper.loadCall(httpRequest, callback, new RemoteUserCommentParser());

    }

    @Override
    public void insertUserComment(final GroupRequestParam groupRequestParam, final UserComment userComment, final BaseOperateCallback callback) {

        if (userComment instanceof MediaComment) {

            insertMediaCommentWhenWechatLogin(groupRequestParam, (MediaComment) userComment, callback);
//            insertMediaCommentWhenWechatLogin(groupUUID, (MediaComment) userComment, callback);

            /*List<Media> medias = ((MediaComment) userComment).getMedias();

            List<Media> localMedias = new ArrayList<>();
            final List<Media> remoteMedias = new ArrayList<>();

            for (Media media : medias) {

                if (media.isLocal())
                    localMedias.add(media);
                else
                    remoteMedias.add(media);

            }

            if (localMedias.size() != 0) {

                UserComment localMediaComment = new MediaComment(userComment.getUuid(), userComment.getCreator(), userComment.getTime(),
                        userComment.getGroupUUID(), localMedias);

                insertUserCommentSrcFromPhone(groupUUID, localMediaComment, new BaseOperateCallback() {
                    @Override
                    public void onSucceed() {

                        handleLocalMediaCommentCreated(groupUUID, userComment, callback, remoteMedias);

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {
                        callback.onFail(operationResult);
                    }
                });

            } else {

                handleLocalMediaCommentCreated(groupUUID, userComment, callback, remoteMedias);

            }*/


        } else if (userComment instanceof FileComment) {

//            insertFileCommentSrcFromNas(groupRequestParam, userComment, callback);

            insertFileCommentSrcFromNasWhenWechatLogin(groupRequestParam, userComment, callback);

        }


    }

    private void insertMediaCommentWhenWechatLogin(GroupRequestParam groupRequestParam, MediaComment mediaComment, final BaseOperateCallback callback) {

        List<Media> medias = mediaComment.getMedias();

        List<Media> localMedias = new ArrayList<>();
        final List<Media> remoteMedias = new ArrayList<>();

        for (Media media : medias) {

            if (media.isLocal())
                localMedias.add(media);
            else
                remoteMedias.add(media);

        }

        String path = BOXES + "/" + groupRequestParam.getGroupUUID() + TWEETS;

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequestByCloudAPIWithWrap(path, "", groupRequestParam.getStationID());

        JsonObject jsonObject = new JsonObject();
        try {
            JSONObject body = new JSONObject(httpRequest.getBody());

            Iterator<String> keys = body.keys();
            while (keys.hasNext()) {

                String key = keys.next();

                jsonObject.addProperty(key, body.getString(key));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonObject.addProperty("comment", "");
        jsonObject.addProperty("type", "list");

        JsonArray localMediaJsonArray = new JsonArray();

        List<FileFormData> fileFormDatas = new ArrayList<>();

        for (Media media : localMedias) {

            JsonObject item = new JsonObject();

            File file = new File(media.getOriginalPhotoPath());

            item.addProperty("size", file.length());
            item.addProperty("sha256", media.getUuid());

            FileFormData fileFormData = new FileFormData(file.getName(), item.toString(), file);

            fileFormDatas.add(fileFormData);

            item.addProperty("filename", file.getName());

            localMediaJsonArray.add(item);

        }

        if (localMediaJsonArray.size() != 0)
            jsonObject.add("list", localMediaJsonArray);

        JsonArray remoteMediaJsonArray = new JsonArray();

        for (Media media : remoteMedias) {

            JsonObject item = new JsonObject();

            item.addProperty("type", "media");
            item.addProperty("sha256", media.getUuid());

            remoteMediaJsonArray.add(item);

        }

        if (remoteMediaJsonArray.size() != 0)
            jsonObject.add("indrive", remoteMediaJsonArray);


        String jsonObj = jsonObject.toString();

        TextFormData textFormData = new TextFormData(Util.MANIFEST_STRING, jsonObj);

        Log.d(TAG, "insertMediaComment: manifest jsonObject:" + jsonObj);

        wrapper.operateCall(httpRequest, Collections.singletonList(textFormData), fileFormDatas,
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                        callback.onSucceed();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        callback.onFail(operationResult);

                    }
                }, new RemoteDataParser<Void>() {
                    @Override
                    public Void parse(String json) throws JSONException {
                        return null;
                    }
                });

    }

    private void insertMediaComment(GroupRequestParam groupRequestParam, MediaComment mediaComment, final BaseOperateCallback callback) {

        List<Media> medias = mediaComment.getMedias();

        List<Media> localMedias = new ArrayList<>();
        final List<Media> remoteMedias = new ArrayList<>();

        for (Media media : medias) {

            if (media.isLocal())
                localMedias.add(media);
            else
                remoteMedias.add(media);

        }

        String path = BOXES + "/" + groupRequestParam.getGroupUUID() + TWEETS;

        HttpRequest httpRequest = httpRequestFactory.createHttpPostFileRequest(path, "", getAuthorizationValue());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("comment", "");
        jsonObject.addProperty("type", "list");

        JsonArray localMediaJsonArray = new JsonArray();

        List<FileFormData> fileFormDatas = new ArrayList<>();

        for (Media media : localMedias) {

            JsonObject item = new JsonObject();

            File file = new File(media.getOriginalPhotoPath());

            item.addProperty("size", file.length());
            item.addProperty("sha256", media.getUuid());

            FileFormData fileFormData = new FileFormData(file.getName(), item.toString(), file);

            fileFormDatas.add(fileFormData);

            item.addProperty("filename", file.getName());

            localMediaJsonArray.add(item);

        }

        if (localMediaJsonArray.size() != 0)
            jsonObject.add("list", localMediaJsonArray);

        JsonArray remoteMediaJsonArray = new JsonArray();

        for (Media media : remoteMedias) {

            JsonObject item = new JsonObject();

            item.addProperty("type", "media");
            item.addProperty("sha256", media.getUuid());

            remoteMediaJsonArray.add(item);

        }

        if (remoteMediaJsonArray.size() != 0)
            jsonObject.add("indrive", remoteMediaJsonArray);

        String jsonObj = jsonObject.toString();

        TextFormData textFormData = new TextFormData("list", jsonObj);

        Log.d(TAG, "insertMediaComment: list jsonObject:" + jsonObj);

        wrapper.operateCall(httpRequest, Collections.singletonList(textFormData), fileFormDatas,
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                        callback.onSucceed();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        callback.onFail(operationResult);

                    }
                }, new RemoteDataParser<Void>() {
                    @Override
                    public Void parse(String json) throws JSONException {
                        return null;
                    }
                });

    }

    private void insertUserCommentSrcFromPhone(String groupUUID, UserComment userComment, final BaseOperateCallback callback) {
        String path = BOXES + "/" + groupUUID + TWEETS;

        HttpRequest httpRequest = httpRequestFactory.createHttpPostFileRequest(path, "", getAuthorizationValue());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("comment", "");
        jsonObject.addProperty("type", "list");

        JsonArray jsonArray = new JsonArray();

        List<FileFormData> fileFormDatas = new ArrayList<>();

        if (userComment instanceof MediaComment) {

            List<Media> medias = ((MediaComment) userComment).getMedias();

            for (Media media : medias) {

                JsonObject item = new JsonObject();

                File file = new File(media.getOriginalPhotoPath());

                item.addProperty("size", file.length());
                item.addProperty("sha256", media.getUuid());

                FileFormData fileFormData = new FileFormData(file.getName(), item.toString(), file);

                fileFormDatas.add(fileFormData);

                item.addProperty("filename", file.getName());

                jsonArray.add(item);


            }

        } else if (userComment instanceof FileComment) {

            List<AbstractFile> files = ((FileComment) userComment).getFiles();

            for (AbstractFile file : files) {

                AbstractLocalFile abstractLocalFile = (AbstractLocalFile) file;

                File fileItem = new File(abstractLocalFile.getPath());

                JsonObject item = new JsonObject();

                item.addProperty("size", fileItem.length());
                item.addProperty("sha256", Util.calcSHA256OfFile(abstractLocalFile.getPath()));

                FileFormData fileFormData = new FileFormData(fileItem.getName(), item.toString(), fileItem);

                fileFormDatas.add(fileFormData);

                item.addProperty("filename", file.getName());

                jsonArray.add(item);

            }

        }

        jsonObject.add("list", jsonArray);

        TextFormData textFormData = new TextFormData("list", jsonObject.toString());

        wrapper.operateCall(httpRequest, Collections.singletonList(textFormData), fileFormDatas,
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                        callback.onSucceed();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        callback.onFail(operationResult);

                    }
                }, new RemoteDataParser<Void>() {
                    @Override
                    public Void parse(String json) throws JSONException {
                        return null;
                    }
                });
    }

    private void insertFileCommentSrcFromNasWhenWechatLogin(GroupRequestParam groupRequestParam, UserComment userComment, final BaseOperateCallback callback) {

        String path = BOXES + "/" + groupRequestParam.getGroupUUID() + TWEETS;

        HttpRequest httpRequest = httpRequestFactory.createHttpPostFileRequestByCloudAPIWithWrap(path, "", groupRequestParam.getStationID());

        JsonObject jsonObject = new JsonObject();

        try {
            JSONObject body = new JSONObject(httpRequest.getBody());

            Iterator<String> keys = body.keys();
            while (keys.hasNext()) {

                String key = keys.next();

                jsonObject.addProperty(key, body.getString(key));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonObject.addProperty("comment", "");
        jsonObject.addProperty("type", "list");

        JsonArray jsonArray = new JsonArray();

        if (userComment instanceof MediaComment) {

            List<Media> medias = ((MediaComment) userComment).getMedias();

            for (Media media : medias) {

                JsonObject item = new JsonObject();

                item.addProperty("type", "media");
                item.addProperty("sha256", media.getUuid());

                jsonArray.add(item);


            }

        } else if (userComment instanceof FileComment) {

            List<AbstractFile> files = ((FileComment) userComment).getFiles();

            for (AbstractFile file : files) {

                AbstractRemoteFile abstractRemoteFile = (AbstractRemoteFile) file;

                JsonObject item = new JsonObject();

                item.addProperty("type", "file");

                item.addProperty("filename", abstractRemoteFile.getName());

                item.addProperty("driveUUID", abstractRemoteFile.getRootFolderUUID());

                item.addProperty("dirUUID", abstractRemoteFile.getParentFolderUUID());

                jsonArray.add(item);

            }

        }

        jsonObject.add("indrive", jsonArray);

        String jsonObj = jsonObject.toString();

        TextFormData textFormData = new TextFormData(Util.MANIFEST_STRING, jsonObj);

        Log.d(TAG, "insertFileComment: manifest jsonObject:" + jsonObj);

        wrapper.operateCall(httpRequest, Collections.singletonList(textFormData), Collections.<FileFormData>emptyList(),
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                        callback.onSucceed();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        callback.onFail(operationResult);

                    }
                }, new RemoteDataParser<Void>() {
                    @Override
                    public Void parse(String json) throws JSONException {
                        return null;
                    }
                });


    }

    private void insertFileCommentSrcFromNas(GroupRequestParam groupRequestParam, UserComment userComment, final BaseOperateCallback callback) {

        String path = BOXES + "/" + groupRequestParam.getGroupUUID() + TWEETS;

        HttpRequest httpRequest = httpRequestFactory.createHttpPostFileRequest(path, "", getAuthorizationValue());

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("comment", "");
        jsonObject.addProperty("type", "list");

        JsonArray jsonArray = new JsonArray();

        if (userComment instanceof MediaComment) {

            List<Media> medias = ((MediaComment) userComment).getMedias();

            for (Media media : medias) {

                JsonObject item = new JsonObject();

                item.addProperty("type", "media");
                item.addProperty("sha256", media.getUuid());

                jsonArray.add(item);


            }

        } else if (userComment instanceof FileComment) {

            List<AbstractFile> files = ((FileComment) userComment).getFiles();

            for (AbstractFile file : files) {

                AbstractRemoteFile abstractRemoteFile = (AbstractRemoteFile) file;

                JsonObject item = new JsonObject();

                item.addProperty("type", "file");

                item.addProperty("filename", abstractRemoteFile.getName());

                item.addProperty("driveUUID", abstractRemoteFile.getRootFolderUUID());

                item.addProperty("dirUUID", abstractRemoteFile.getParentFolderUUID());

                jsonArray.add(item);

            }

        }

        jsonObject.add("indrive", jsonArray);

        String jsonObj = jsonObject.toString();

        TextFormData textFormData = new TextFormData("list", jsonObj);

        Log.d(TAG, "insertFileComment: list jsonObject:" + jsonObj);

        wrapper.operateCall(httpRequest, Collections.singletonList(textFormData), Collections.<FileFormData>emptyList(),
                new BaseOperateDataCallback<Void>() {
                    @Override
                    public void onSucceed(Void data, OperationResult result) {

                        callback.onSucceed();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        callback.onFail(operationResult);

                    }
                }, new RemoteDataParser<Void>() {
                    @Override
                    public Void parse(String json) throws JSONException {
                        return null;
                    }
                });


    }

    @Override
    public void updateGroupProperty(GroupRequestParam groupRequestParam, String property, String newValue, BaseOperateCallback callback) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(property, newValue);

        String httpPath = BOXES + "/" + groupRequestParam.getGroupUUID();

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequestByCloudAPIWithWrap(httpPath, jsonObject.toString(), groupRequestParam.getStationID());

        wrapper.operateCall(httpRequest, callback);

    }

    @Override
    public void addUsersInGroup(GroupRequestParam groupRequestParam, List<String> userGUIDs, BaseOperateCallback callback) {

        JsonObject root = getAddDeleteUserBody("add", userGUIDs);

        String httpPath = BOXES + "/" + groupRequestParam.getGroupUUID();

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequestByCloudAPIWithWrap(httpPath, root.toString(), groupRequestParam.getStationID());

        wrapper.operateCall(httpRequest, callback);

    }


    @NonNull
    private JsonObject getAddDeleteUserBody(String op, List<String> userGUIDs) {
        JsonObject users = new JsonObject();
        users.addProperty("op", op);

        JsonArray userGUIDsArray = new JsonArray();

        for (String userGUID : userGUIDs) {
            userGUIDsArray.add(userGUID);
        }

        users.add("value", userGUIDsArray);

        JsonObject root = new JsonObject();

        root.add("users", users);
        return root;
    }


    @Override
    public void deleteUsersInGroup(GroupRequestParam groupRequestParam, List<String> userGUIDs, BaseOperateCallback callback) {

        JsonObject root = getAddDeleteUserBody("delete", userGUIDs);

        String httpPath = BOXES + "/" + groupRequestParam.getGroupUUID();

        HttpRequest httpRequest = httpRequestFactory.createHttpPatchRequestByCloudAPIWithWrap(httpPath, root.toString(), groupRequestParam.getStationID());

        wrapper.operateCall(httpRequest, callback);

    }

    @Override
    public Pin insertPin(String groupUUID, Pin pin) {
        return null;
    }

    @Override
    public boolean modifyPin(String groupUUID, String pinName, String pinUUID) {
        return false;
    }

    @Override
    public boolean deletePin(String groupUUID, String pinUUID) {
        return false;
    }

    @Override
    public boolean insertMediaToPin(Collection<Media> medias, String groupUUID, String pinUUID) {
        return false;
    }

    @Override
    public boolean insertFileToPin(Collection<AbstractFile> files, String groupUUID, String pinUUID) {
        return false;
    }

    @Override
    public boolean updatePinInGroup(Pin pin, String groupUUID) {
        return false;
    }

    @Override
    public Pin getPinInGroup(String pinUUID, String groupUUID) {
        return null;
    }
}
