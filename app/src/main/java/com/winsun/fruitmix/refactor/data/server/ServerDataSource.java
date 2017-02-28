package com.winsun.fruitmix.refactor.data.server;

import android.support.annotation.NonNull;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.MediaShareOperationEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationLocalMediaShareUploading;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteMediaShareJSONObjectParser;
import com.winsun.fruitmix.parser.RemoteUserJSONObjectParser;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.dataOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FileDownloadLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.OperateMediaShareResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.OperateUserResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/2/9.
 */

public class ServerDataSource implements DataSource {

    public static final String TAG = ServerDataSource.class.getSimpleName();

    public ServerDataSource() {

    }

    private HttpResponse remoteCallWithUrl(String token, String url) throws MalformedURLException, IOException, SocketTimeoutException {

        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + token);

        return OkHttpUtil.INSTANCE.remoteCallMethod(httpRequest);

    }

    // create object and store it to the server
    private HttpResponse postRemoteCall(String url, String token, String data) throws MalformedURLException, IOException, SocketTimeoutException {
//        return RemoteCallMethod(Util.HTTP_POST_METHOD, req, data);

        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_POST_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + token);
        httpRequest.setBody(data);

        return OkHttpUtil.INSTANCE.remoteCallMethod(httpRequest);
    }

    private HttpResponse deleteRemoteCall(String url, String token, String data) throws MalformedURLException, IOException, SocketTimeoutException {
//        return RemoteCallMethod(Util.HTTP_DELETE_METHOD, req, data);

        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_DELETE_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + token);
        httpRequest.setBody(data);

        return OkHttpUtil.INSTANCE.remoteCallMethod(httpRequest);
    }

    public List<EquipmentAlias> loadEquipmentAlias(String token, String url) {

        List<EquipmentAlias> equipmentAliases = new ArrayList<>();

        try {

            String str = remoteCallWithUrl(token, url).getResponseData();

            JSONArray json = new JSONArray(str);

            int length = json.length();

            for (int i = 0; i < length; i++) {
                JSONObject itemRaw = json.getJSONObject(i);

                String ip = itemRaw.getString("ipv4");
                EquipmentAlias equipmentAlias = new EquipmentAlias();
                equipmentAlias.setIpv4(ip);
                equipmentAliases.add(equipmentAlias);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return equipmentAliases;
    }

    @Override
    public String loadGateway() {
        return null;
    }

    @Override
    public String loadPort() {
        return null;
    }

    public List<User> loadUserByLoginApi(String token, String url) {

        List<User> users = new ArrayList<>();

        try {

            String str = remoteCallWithUrl(token, url).getResponseData();

            JSONArray json = new JSONArray(str);

            int length = json.length();

            for (int i = 0; i < length; i++) {
                JSONObject itemRaw = json.getJSONObject(i);
                User user = new User();
                user.setUserName(itemRaw.getString("username"));
                user.setUuid(itemRaw.getString("uuid"));
                user.setAvatar(itemRaw.getString("avatar"));
                users.add(user);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return users;
    }

    @Override
    public void deleteToken() {
    }

    @Override
    public OperateUserResult insertUser(String url, String token, String userName, String userPassword) {

        return handleActionCreateRemoteUser(url, token, userName, userPassword);

    }

    private OperateUserResult handleActionCreateRemoteUser(String url, String token, String userName, String userPassword) {

        String body = User.generateCreateRemoteUserBody(userName, userPassword);

        HttpResponse httpResponse;

        OperateUserResult result = new OperateUserResult();

        try {
            httpResponse = postRemoteCall(url, token, body);

            if (httpResponse.getResponseCode() == 200) {

                User user = new RemoteUserJSONObjectParser().getUser(new JSONObject(httpResponse.getResponseData()));

                result.setUser(user);
                result.setOperationResult(new OperationSuccess());

            } else {

                result.setOperationResult(new OperationNetworkException(httpResponse.getResponseCode()));

                Log.i(TAG, "insert remote user fail");

            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();

            result.setOperationResult(new OperationMalformedUrlException());

            Log.i(TAG, "insert remote user fail");

        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();

            result.setOperationResult(new OperationSocketTimeoutException());

            Log.i(TAG, "insert remote user fail");

        } catch (IOException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationIOException());

            Log.i(TAG, "insert remote user fail");

        } catch (JSONException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationJSONException());

            Log.i(TAG, "insert remote user fail");
        }

        return result;

    }

    @Override
    public OperationResult insertUsers(List<User> users) {
        return null;
    }

    @Override
    public OperateMediaShareResult insertRemoteMediaShare(String url, String token, MediaShare mediaShare) {

        return handleActionCreateRemoteMediaShareTask(url, token, mediaShare);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private OperateMediaShareResult handleActionCreateRemoteMediaShareTask(String url, String token, MediaShare mediaShare) {

        String data = generateCreateMediaShareData(mediaShare);

        OperateMediaShareResult result = new OperateMediaShareResult();

        HttpResponse httpResponse;

        try {
            httpResponse = postRemoteCall(url, token, data);

            if (httpResponse.getResponseCode() == 200) {

                Log.i(TAG, "insert remote mediashare which source is network succeed");

                RemoteMediaShareJSONObjectParser parser = new RemoteMediaShareJSONObjectParser();

                MediaShare newMediaShare = parser.getRemoteMediaShare(new JSONObject(httpResponse.getResponseData()));

                result.setMediaShare(newMediaShare);
                result.setOperationResult(new OperationSuccess());

            } else {
                result.setOperationResult(new OperationNetworkException(httpResponse.getResponseCode()));

                Log.i(TAG, "insert remote mediashare fail");
            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();

            result.setOperationResult(new OperationMalformedUrlException());

            Log.i(TAG, "insert remote mediashare fail");

        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();

            result.setOperationResult(new OperationSocketTimeoutException());

            Log.i(TAG, "insert remote mediashare fail");
        } catch (IOException ex) {
            ex.printStackTrace();

            result.setOperationResult(new OperationIOException());

            Log.i(TAG, "insert remote mediashare fail");
        } catch (JSONException ex) {
            ex.printStackTrace();

            result.setOperationResult(new OperationJSONException());

            Log.i(TAG, "insert remote mediashare fail");
        }

        return result;

    }

    @NonNull
    private String generateCreateMediaShareData(MediaShare mediaShare) {
        String data;

        StringBuilder builder = new StringBuilder();
        builder.append("{\"album\":");
        if (mediaShare.isAlbum()) {
            builder.append("{\"title\":\"");
            builder.append(Util.removeWrap(mediaShare.getTitle()));
            builder.append("\",\"text\":\"");
            builder.append(Util.removeWrap(mediaShare.getDesc()));
            builder.append("\"}");
        } else {
            builder.append("null");
        }

        builder.append(",");

        builder.append("\"sticky\":");
        builder.append(mediaShare.isSticky());

        builder.append(",");

        builder.append("\"viewers\":[");
        StringBuilder viewersBuilder = new StringBuilder();
        for (String viewer : mediaShare.getViewers()) {
            viewersBuilder.append(",");
            viewersBuilder.append("\"");
            viewersBuilder.append(viewer);
            viewersBuilder.append("\"");
        }
        viewersBuilder.append("]");
        if (viewersBuilder.length() > 1) {
            builder.append(viewersBuilder.toString().substring(1));
        } else {
            builder.append(viewersBuilder.toString());
        }

        builder.append(",");

        builder.append("\"maintainers\":[");
        StringBuilder maintainersBuilder = new StringBuilder();
        for (String maintainer : mediaShare.getMaintainers()) {
            maintainersBuilder.append(",");
            maintainersBuilder.append("\"");
            maintainersBuilder.append(maintainer);
            maintainersBuilder.append("\"");
        }
        maintainersBuilder.append("]");
        if (maintainersBuilder.length() > 1) {
            builder.append(maintainersBuilder.toString().substring(1));
        } else {
            builder.append(maintainersBuilder.toString());
        }

        builder.append(",");

        builder.append("\"contents\":[");
        StringBuilder contentsBuilder = new StringBuilder();
        for (String content : mediaShare.getMediaKeyInMediaShareContents()) {
            contentsBuilder.append(",");
            contentsBuilder.append("\"");

            if (content.contains("/"))
                content = Util.CalcSHA256OfFile(content);

            contentsBuilder.append(content);
            contentsBuilder.append("\"");
        }
        contentsBuilder.append("]");
        if (contentsBuilder.length() > 1) {
            builder.append(contentsBuilder.toString().substring(1));
        } else {
            builder.append(contentsBuilder.toString());
        }

        builder.append("}");
        data = builder.toString();
        Log.i(TAG, "handleActionCreateRemoteMediaShareTask: request json:" + data);
        return data;
    }

    @Override
    public OperationResult insertRemoteMediaShares(Collection<MediaShare> mediaShares) {
        return null;
    }

    @Override
    public OperationResult insertLocalMedias(List<Media> medias) {
        return null;
    }

    @Override
    public OperationResult insertRemoteMedias(List<Media> medias) {
        return null;
    }

    @Override
    public OperationResult modifyRemoteMediaShare(String url, String token, String requestData, MediaShare modifiedMediaShare) {

        return handleActionModifyRemoteShare(url, token, modifiedMediaShare, requestData);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private OperationResult handleActionModifyRemoteShare(String url, String token, MediaShare mediaShare, String requestData) {

        if (mediaShare.isLocal()) {

            return new OperationLocalMediaShareUploading();

        } else {

            try {
                HttpResponse httpResponse = postRemoteCall(url, token, requestData);

                if (httpResponse.getResponseCode() == 200) {
                    return new OperationSuccess();
                } else {
                    return new OperationNetworkException(httpResponse.getResponseCode());
                }

            } catch (MalformedURLException e) {

                e.printStackTrace();

                return new OperationMalformedUrlException();

            } catch (SocketTimeoutException e) {

                e.printStackTrace();

                return new OperationSocketTimeoutException();

            } catch (IOException e) {

                e.printStackTrace();

                return new OperationIOException();

            }
        }

    }

    @Override
    public OperationResult deleteRemoteMediaShare(String url, String token, MediaShare mediaShare) {

        return handleActionDeleteRemoteShare(url, token, mediaShare);
    }

    private OperationResult handleActionDeleteRemoteShare(String url, String token, MediaShare mediaShare) {

        if (mediaShare.isLocal()) {

            return new OperationLocalMediaShareUploading();

        } else {

//            data = "{\"commands\": \"[{\\\"op\\\":\\\"replace\\\", \\\"path\\\":\\\"" + mediaShare.getUuid() + "\\\", \\\"value\\\":{\\\"archived\\\":\\\"true\\\",\\\"album\\\":\\\"true\\\", \\\"maintainers\\\":[\\\"" + FNAS.userUUID + "\\\"], \\\"tags\\\":[{\\\"albumname\\\":\\\"" + mediaShare.getDate() + "\\\", \\\"desc\\\":\\\"" + mediaShare.getDesc() + "\\\"}], \\\"viewers\\\":[]}}]\"}";

            try {
                deleteRemoteCall(url, token, "");

                Log.i(TAG, "delete remote mediashare which source is network succeed");

                return new OperationSuccess();


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i(TAG, "delete remote mediashare fail");
                return new OperationMalformedUrlException();

            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                Log.i(TAG, "delete remote mediashare fail");
                return new OperationSocketTimeoutException();

            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "delete remote mediashare fail");
                return new OperationIOException();

            }
        }
    }

    @Override
    public DeviceIDLoadOperationResult loadDeviceID() {
        return null;
    }

    @Override
    public UsersLoadOperationResult loadUsers() {
        return null;
    }

    @Override
    public User loadUser(String userUUID) {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllRemoteMedias() {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllLocalMedias() {
        return null;
    }

    @Override
    public Collection<String> loadLocalMediaUUIDs() {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadLocalMediaInCamera(Collection<String> loadedMediaUUIDs) {
        return null;
    }

    @Override
    public Media loadMedia(String mediaKey) {
        return null;
    }

    @Override
    public void updateLocalMediasUploadedFalse() {

    }

    @Override
    public MediaShare loadRemoteMediaShare(String mediaShareUUID) {
        return null;
    }

    @Override
    public MediaSharesLoadOperationResult loadAllRemoteMediaShares() {
        return null;
    }

    @Override
    public FilesLoadOperationResult loadRemoteFiles(String folderUUID) {
        return null;
    }

    @Override
    public FileDownloadLoadOperationResult loadDownloadedFilesRecord() {
        return null;
    }

    @Override
    public OperationResult deleteDownloadedFileRecord(List<String> fileUUIDs) {
        return null;
    }

    @Override
    public FileSharesLoadOperationResult loadRemoteFileRootShares() {
        return null;
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {
        return null;
    }

    @Override
    public User loadCurrentLoginUser() {
        return null;
    }

    @Override
    public LoadTokenParam getLoadTokenParam() {
        return null;
    }

    @Override
    public boolean getShowAlbumTipsValue() {
        return false;
    }

    @Override
    public void saveShowAlbumTipsValue(boolean value) {

    }

    @Override
    public boolean getShowPhotoReturnTipsValue() {
        return false;
    }

    @Override
    public void saveShowPhotoReturnTipsValue(boolean value) {

    }

    @Override
    public OperationResult deleteAllRemoteMediaShare() {
        return null;
    }

    @Override
    public OperationResult deleteAllRemoteMedia() {
        return null;
    }

    @Override
    public OperationResult deleteAllRemoteUsers() {
        return null;
    }

    @Override
    public OperationResult insertDownloadedFileRecord(FileDownloadItem fileDownloadItem) {
        return null;
    }

    @Override
    public OperationResult insertToken(String token) {
        return null;
    }

    @Override
    public OperationResult insertDeviceID(String deviceID) {
        return null;
    }

    @Override
    public void deleteDeviceID() {

    }

    @Override
    public OperationResult insertCurrentLoginUser(User user) {
        return null;
    }
}
