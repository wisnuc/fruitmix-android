package com.winsun.fruitmix.refactor.data.server;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadState;
import com.winsun.fruitmix.fileModule.interfaces.FileDownloadUploadInterface;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.http.retrofit.RetrofitInstance;
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
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteFileFolderParser;
import com.winsun.fruitmix.parser.RemoteFileShareParser;
import com.winsun.fruitmix.parser.RemoteMediaParser;
import com.winsun.fruitmix.parser.RemoteMediaShareJSONObjectParser;
import com.winsun.fruitmix.parser.RemoteMediaShareParser;
import com.winsun.fruitmix.parser.RemoteUserJSONObjectParser;
import com.winsun.fruitmix.parser.RemoteUserParser;
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
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by Administrator on 2017/2/9.
 */

public class ServerDataSource implements DataSource {

    private static ServerDataSource INSTANCE;

    public static final String TAG = ServerDataSource.class.getSimpleName();

    private ServerDataSource() {

    }

    public static ServerDataSource getInstance() {

        if (INSTANCE == null)
            INSTANCE = new ServerDataSource();

        return INSTANCE;
    }

    private HttpResponse getRemoteCall(String url, String token) throws MalformedURLException, IOException, SocketTimeoutException {

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

            String str = getRemoteCall(url, token).getResponseData();

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

    public List<User> loadUserByLoginApi(String token, String url) {

        List<User> users = new ArrayList<>();

        try {

            String str = getRemoteCall(url, token).getResponseData();

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
    public Collection<String> loadAllUserUUID() {
        return null;
    }

    @Override
    public OperationResult insertLoginUserUUID(String userUUID) {
        return null;
    }

    @Override
    public String loadLoginUserUUID() {
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
    public OperationResult insertLocalMedia(String url, String token, Media media) {

        boolean result = uploadFile(url, token, media);

        if (result)
            return new OperationSuccess();
        else
            return new OperationIOException();

    }

    @Override
    public OperationResult updateLocalMedia(Media media) {
        return null;
    }

    private boolean uploadFile(String url, String token, Media media) {

        String hash, boundary;
        BufferedOutputStream outStream;
        StringBuilder sb;
        InputStream is;
        byte[] buffer;
        int len, resCode;
        HttpURLConnection conn = null;

        try {

            hash = media.getUuid();

            String thumb = media.getThumb();

            Log.d(TAG, "thumb:" + media.getThumb() + "hash:" + hash);

            // head
            Log.d(TAG, "Photo UP: " + url);
            boundary = java.util.UUID.randomUUID().toString();
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);
            conn.setRequestMethod(Util.HTTP_POST_METHOD); // Post方式
            conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + token);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setConnectTimeout(Util.HTTP_CONNECT_TIMEOUT);
            conn.setReadTimeout(Util.HTTP_CONNECT_TIMEOUT);

            outStream = new BufferedOutputStream(conn.getOutputStream());

            sb = new StringBuilder();
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(thumb).append("\"\r\n");
            sb.append("Content-Type: image/jpeg;\r\n");
            sb.append("\r\n");
            outStream.write(sb.toString().getBytes());

            is = new FileInputStream(thumb);
            buffer = new byte[15000];
            len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            is.close();
            outStream.write("\r\n".getBytes());

            sb = new StringBuilder();
            sb.append("--");
            sb.append(boundary);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data; name=\""
                    + "sha256" + "\"");
            sb.append("\r\n");
            sb.append("\r\n");
            sb.append(hash);
            sb.append("\r\n");
            byte[] data = sb.toString().getBytes();
            outStream.write(data);

            outStream.write(("--" + boundary + "--\r\n").getBytes());
            outStream.flush();
            outStream.close();

            resCode = conn.getResponseCode();
            Log.d(TAG, "UP END1: " + resCode);
            if (resCode == 200) {

                String result = ReadFull(conn.getInputStream());

                Log.i(TAG, "UploadFile: result" + result);

                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (conn != null)
                conn.disconnect();
        }

        return false;
    }

    private String ReadFull(InputStream ins) throws IOException {
        String result = "";
        int length;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            while ((length = ins.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            result = new String(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                ins.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return result;
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
    public DeviceIDLoadOperationResult loadDeviceID(String url, String token) {

        DeviceIDLoadOperationResult result = new DeviceIDLoadOperationResult();

        try {
            HttpResponse httpResponse = postRemoteCall(url, token, "");

            if (httpResponse.getResponseCode() == 200) {

                String deviceID = new JSONObject(httpResponse.getResponseData()).getString("uuid");

                result.setDeviceID(deviceID);

                result.setOperationResult(new OperationSuccess());

            } else {

                result.setOperationResult(new OperationNetworkException(httpResponse.getResponseCode()));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationMalformedUrlException());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationSocketTimeoutException());
        } catch (IOException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationJSONException());
        }

        return result;
    }

    @Override
    public UsersLoadOperationResult loadUsers(String loadUserUrl, String loadOtherUserUrl, String token) {

        return handleActionRetrieveRemoteUser(loadUserUrl, loadOtherUserUrl, token);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private UsersLoadOperationResult handleActionRetrieveRemoteUser(String loadUserUrl, String loadOtherUserUrl, String token) {

        UsersLoadOperationResult result = new UsersLoadOperationResult();

        List<User> users;

        try {

            HttpResponse httpResponse = getRemoteCall(loadUserUrl, token);

            RemoteDataParser<User> parser = new RemoteUserParser();
            users = parser.parse(httpResponse.getResponseData());

            List<User> otherUsers = parser.parse(getRemoteCall(loadOtherUserUrl, token).getResponseData());

            result.setUsers(addDifferentUsers(users, otherUsers));
            result.setOperationResult(new OperationSuccess());

            Log.i(TAG, "handleActionRetrieveRemoteUser: retrieve user from network");

        } catch (MalformedURLException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationMalformedUrlException());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationSocketTimeoutException());
        } catch (IOException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationJSONException());
        }

        return result;

    }

    private List<User> addDifferentUsers(List<User> users, List<User> otherUsers) {
        for (User otherUser : otherUsers) {
            int i;
            for (i = 0; i < users.size(); i++) {
                if (otherUser.getUuid().equals(users.get(i).getUuid())) {
                    break;
                }
            }
            if (i >= users.size()) {
                users.add(otherUser);
            }
        }

        return users;
    }


    @Override
    public User loadUser(String userUUID) {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllRemoteMedias(String url, String token) {

        return handleActionRetrieveRemoteMedia(url, token);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private MediasLoadOperationResult handleActionRetrieveRemoteMedia(String url, String token) {

        MediasLoadOperationResult result = new MediasLoadOperationResult();

        List<Media> medias;

        try {

            Log.d(TAG, "handleActionRetrieveRemoteMedia: before load" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            HttpResponse httpResponse = getRemoteCall(url, token);

            Log.d(TAG, "handleActionRetrieveRemoteMedia: load media finish" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())));

            RemoteDataParser<Media> parser = new RemoteMediaParser();
            medias = parser.parse(httpResponse.getResponseData());

            Log.i(TAG, "handleActionRetrieveRemoteMedia: parse json finish");

            result.setMedias(medias);
            result.setOperationResult(new OperationSuccess());


        } catch (MalformedURLException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationMalformedUrlException());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationSocketTimeoutException());
        } catch (IOException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationJSONException());
        }

        return result;
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
    public Collection<String> loadRemoteMediaUUIDs() {
        return null;
    }

    @Override
    public Media loadLocalMediaByThumb(String thumb) {
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
    public MediaSharesLoadOperationResult loadAllRemoteMediaShares(String url, String token) {

        List<MediaShare> mediaShares;

        MediaSharesLoadOperationResult result = new MediaSharesLoadOperationResult();

        try {

            HttpResponse httpResponse = getRemoteCall(url, token);

            Log.d(TAG, "loadRemoteShare:" + httpResponse.getResponseData().equals(""));

            RemoteDataParser<MediaShare> parser = new RemoteMediaShareParser();
            mediaShares = parser.parse(httpResponse.getResponseData());

            Log.d(TAG, "handleActionRetrieveRemoteMediaShare: parse remote media share");

            result.setMediaShares(mediaShares);
            result.setOperationResult(new OperationSuccess());

        } catch (MalformedURLException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationMalformedUrlException());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationSocketTimeoutException());
        } catch (IOException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationJSONException());
        }

        return result;
    }

    @Override
    public FilesLoadOperationResult loadRemoteFolder(String url, String token) {

        FilesLoadOperationResult result = new FilesLoadOperationResult();

        try {
            HttpResponse httpResponse = getRemoteCall(url, token);

            if (httpResponse.getResponseCode() == 200) {

                RemoteFileFolderParser parser = new RemoteFileFolderParser();
                List<AbstractRemoteFile> abstractRemoteFiles = parser.parse(httpResponse.getResponseData());

                result.setFiles(abstractRemoteFiles);
                result.setOperationResult(new OperationSuccess());

            } else {
                result.setOperationResult(new OperationNetworkException(httpResponse.getResponseCode()));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationMalformedUrlException());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationSocketTimeoutException());
        } catch (IOException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationJSONException());
        }

        return result;
    }

    @Override
    public OperationResult loadRemoteFile(String baseUrl, String token, FileDownloadState fileDownloadState) {

        FileDownloadUploadInterface fileDownloadUploadInterface = RetrofitInstance.INSTANCE.getRetrofitInstance(baseUrl, token).create(FileDownloadUploadInterface.class);

        Call<ResponseBody> call = fileDownloadUploadInterface.downloadFile(baseUrl + Util.FILE_PARAMETER + "/" + fileDownloadState.getFileUUID());

        boolean result = false;

        try {
            result = FileUtil.writeResponseBodyToFolder(call.execute().body(), fileDownloadState);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (result)
            return new OperationSuccess();
        else
            return new OperationIOException();
    }

    @Override
    public OperationResult insertRemoteFiles(AbstractRemoteFile folder) {
        return null;
    }

    @Override
    public OperationResult deleteAllRemoteFiles() {
        return null;
    }

    @Override
    public FileDownloadItem loadDownloadFileRecord(String fileUUID) {
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
    public FileSharesLoadOperationResult loadRemoteFileRootShares(String loadFileSharedWithMeUrl, String loadFileShareWithOthersUrl, String token) {

        FileSharesLoadOperationResult result = new FileSharesLoadOperationResult();

        List<AbstractRemoteFile> files;

        try {

            HttpResponse remoteFileShareWithMeJSON = getRemoteCall(loadFileSharedWithMeUrl, token);

            if (remoteFileShareWithMeJSON.getResponseCode() == 200) {
                RemoteDataParser<AbstractRemoteFile> parser = new RemoteFileShareParser();

                files = new ArrayList<>();

                files.addAll(parser.parse(remoteFileShareWithMeJSON.getResponseData()));

                HttpResponse remoteFileShareWithOthersJSON = getRemoteCall(loadFileShareWithOthersUrl, token);

                if (remoteFileShareWithOthersJSON.getResponseCode() == 200) {

                    files.addAll(parser.parse(remoteFileShareWithOthersJSON.getResponseData()));

                    result.setFiles(files);
                    result.setOperationResult(new OperationSuccess());

                } else {
                    result.setOperationResult(new OperationNetworkException(remoteFileShareWithOthersJSON.getResponseCode()));
                }

            } else {
                result.setOperationResult(new OperationNetworkException(remoteFileShareWithMeJSON.getResponseCode()));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationMalformedUrlException());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationSocketTimeoutException());
        } catch (IOException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationJSONException());
        }


        return result;

    }

    @Override
    public OperationResult insertRemoteFileShare(List<AbstractRemoteFile> files) {
        return null;
    }

    @Override
    public OperationResult deleteAllRemoteFileShare() {
        return null;
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {

        TokenLoadOperationResult result = new TokenLoadOperationResult();

        try {
            String url = param.getGateway() + ":" + Util.PORT + Util.TOKEN_PARAMETER;

            HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);
            httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((param.getUserUUID() + ":" + param.getUserPassword()).getBytes(), Base64.NO_WRAP));

            HttpResponse httpResponse = OkHttpUtil.INSTANCE.remoteCallMethod(httpRequest);

            int responseCode = httpResponse.getResponseCode();

            if (responseCode == 200) {

                String token = new JSONObject(httpResponse.getResponseData()).getString("token");

                result.setToken(token);
                result.setOperationResult(new OperationSuccess());

            } else {
                result.setOperationResult(new OperationNetworkException(responseCode));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationMalformedUrlException());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationSocketTimeoutException());
        } catch (IOException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            result.setOperationResult(new OperationJSONException());
        }


        return result;
    }

    @Override
    public String loadToken() {
        return null;
    }

    @Override
    public OperationResult insertGateway(String gateway) {
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

}
