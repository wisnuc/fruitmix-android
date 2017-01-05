package com.winsun.fruitmix.util;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.eventbus.AbstractFileRequestEvent;
import com.winsun.fruitmix.eventbus.EditPhotoInMediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.MediaCommentRequestEvent;
import com.winsun.fruitmix.eventbus.MediaRequestEvent;
import com.winsun.fruitmix.eventbus.MediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.ModifyMediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.eventbus.RetrieveMediaShareRequestEvent;
import com.winsun.fruitmix.eventbus.TokenRequestEvent;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.mediaModule.model.Comment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collection;

/**
 * Created by Administrator on 2016/4/22.
 */
public class FNAS {

    public static final String TAG = FNAS.class.getSimpleName();

    public static String Gateway = "http://192.168.5.98";
    public static String JWT = null;
    public static String userUUID = null;

    public static String PORT = "3721";

    static String ReadFull(InputStream ins) throws IOException {
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

    public static HttpResponse loadFileInFolder(String folderUUID) throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(Util.FILE_PARAMETER + "/" + folderUUID);
    }

    public static HttpResponse loadFileSharedWithMe() throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(Util.FILE_SHARE_PARAMETER + Util.FILE_SHARED_WITH_ME_PARAMETER);
    }

    public static HttpResponse loadFileShareWithOthers() throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(Util.FILE_SHARE_PARAMETER + Util.FILE_SHARED_WITH_OTHERS_PARAMETER);
    }

    public static HttpResponse loadUser() throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(Util.USER_PARAMETER);

    }

    public static HttpResponse loadOtherUsers() throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(Util.LOGIN_PARAMETER);

    }

    public static HttpResponse loadMedia() throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(Util.MEDIA_PARAMETER); // get all pictures;

    }

    public static HttpResponse loadRemoteShare() throws MalformedURLException, IOException, SocketTimeoutException {
        return FNAS.RemoteCall(Util.MEDIASHARE_PARAMETER);
    }

    public static HttpResponse loadRemoteMediaComment(Context context, String mediaUUID) throws MalformedURLException, IOException, SocketTimeoutException {
        return FNAS.RemoteCall(String.format(context.getString(R.string.android_photo_comment_url), Util.MEDIA_PARAMETER + "/" + mediaUUID));
    }

    public static HttpResponse loadToken(Context context, String gateway, String userUUID, String userPassword) throws MalformedURLException, SocketTimeoutException, IOException {

        HttpURLConnection conn;
        String str = "";
        conn = (HttpURLConnection) (new URL(gateway + ":" + FNAS.PORT + Util.TOKEN_PARAMETER).openConnection()); //output:{"type":"JWT","token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1dWlkIjoiZGIzYWVlZWYtNzViYS00ZTY2LThmMGUtNWQ3MTM2NWEwNGRiIn0.LqISPNt6T5M1Ae4GN3iL0d8D1bj6m0tX7YOwqZqlnvg"}
        conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((userUUID + ":" + userPassword).getBytes(), Base64.DEFAULT));
        conn.setConnectTimeout(Util.HTTP_CONNECT_TIMEOUT);
        if (conn.getResponseCode() == 200) {
            str = FNAS.ReadFull(conn.getInputStream());
        }

        HttpResponse httpResponse = new HttpResponse(conn.getResponseCode(), str);

        Log.i(TAG, "loadToken: " + str);

        return httpResponse;
    }

    public static HttpResponse loadDeviceId() throws MalformedURLException, IOException, SocketTimeoutException {
        HttpResponse httpResponse = new HttpResponse();
        if (LocalCache.DeviceID == null || LocalCache.DeviceID.equals("")) {
            httpResponse = FNAS.PostRemoteCall(Util.DEVICE_ID_PARAMETER, "");
        }

        return httpResponse;
    }

    public static void retrieveRemoteDeviceID(Context context) {

        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.REMOTE_DEVICE_ID));
    }

    public static void retrieveRemoteToken(Context context, String gateway, String userUUID, String pwd) {

        EventBus.getDefault().post(new TokenRequestEvent(OperationType.GET, OperationTargetType.REMOTE_TOKEN, gateway, userUUID,pwd));
    }

    public static void retrieveUser(Context context) {

        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.REMOTE_USER));
    }

    public static void retrieveLocalMediaInCamera() {
        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.LOCAL_MEDIA_IN_CAMERA));
    }

    public static void retrieveLocalMedia(Context context) {

        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.LOCAL_MEDIA));
    }

    public static void retrieveRemoteMedia(Context context) {

        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.REMOTE_MEDIA));
    }


    public static void retrieveLocalMediaShare(Context context) {

        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.LOCAL_MEDIA_SHARE));
    }

    public static void retrieveRemoteMediaShare(Context context, boolean loadMediaShareInDBWhenExceptionOccur) {

        EventBus.getDefault().post(new RetrieveMediaShareRequestEvent(OperationType.GET, OperationTargetType.REMOTE_MEDIA_SHARE, loadMediaShareInDBWhenExceptionOccur));
    }

    public static void retrieveLocalMediaCommentMap(Context context) {

        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.LOCAL_MEDIA_COMMENT));

    }

    public static void retrieveRemoteMediaCommentMap(Context context, String imageUUID) {

        EventBus.getDefault().post(new MediaCommentRequestEvent(OperationType.GET, OperationTargetType.REMOTE_MEDIA_COMMENT, imageUUID, null));
    }

    public static void retrieveRemoteFile(Context context, String folderUUID) {

        EventBus.getDefault().post(new AbstractFileRequestEvent(OperationType.GET, OperationTargetType.REMOTE_FILE, folderUUID));
    }

    public static void retrieveRemoteFileShare() {
        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.REMOTE_FILE_SHARE));
    }

    public static void retrieveDownloadedFile() {
        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.DOWNLOADED_FILE));
    }

    public static void createRemoteMedia(Context context, Media media) {

        EventBus.getDefault().post(new MediaRequestEvent(OperationType.CREATE, OperationTargetType.REMOTE_MEDIA, media));
    }

    public static void createRemoteMediaShare(Context context, MediaShare mediaShare) {

        EventBus.getDefault().post(new MediaShareRequestEvent(OperationType.CREATE, OperationTargetType.REMOTE_MEDIA_SHARE, mediaShare));
    }

    public static void createLocalMediaShare(Context context, MediaShare mediaShare) {

        EventBus.getDefault().post(new MediaShareRequestEvent(OperationType.CREATE, OperationTargetType.LOCAL_MEDIA_SHARE, mediaShare));
    }

    public static void createRemoteMediaComment(Context context, String imageUUID, Comment comment) {

        EventBus.getDefault().post(new MediaCommentRequestEvent(OperationType.CREATE, OperationTargetType.REMOTE_MEDIA_COMMENT, imageUUID, comment));
    }

    public static void createLocalMediaComment(Context context, String imageUUID, Comment comment) {

        EventBus.getDefault().post(new MediaCommentRequestEvent(OperationType.CREATE, OperationTargetType.LOCAL_MEDIA_COMMENT, imageUUID, comment));
    }

    public static void modifyRemoteMediaShare(Context context, MediaShare mediaShare, String requestData) {

        EventBus.getDefault().post(new ModifyMediaShareRequestEvent(OperationType.MODIFY, OperationTargetType.REMOTE_MEDIA_SHARE, mediaShare, requestData));
    }

    public static void modifyLocalMediaShare(Context context, MediaShare mediaShare, String requestData) {

        EventBus.getDefault().post(new ModifyMediaShareRequestEvent(OperationType.MODIFY, OperationTargetType.LOCAL_MEDIA_SHARE, mediaShare, requestData));
    }

    public static void deleteLocalMediaShare(Context context, MediaShare mediaShare) {

        EventBus.getDefault().post(new MediaShareRequestEvent(OperationType.DELETE, OperationTargetType.LOCAL_MEDIA_SHARE, mediaShare));
    }

    public static void deleteRemoteMediaShare(Context context, MediaShare mediaShare) {

        EventBus.getDefault().post(new MediaShareRequestEvent(OperationType.DELETE, OperationTargetType.REMOTE_MEDIA_SHARE, mediaShare));
    }

    public static void deleteLocalMediaComment(Context context, String imageUUID, Comment comment) {

        EventBus.getDefault().post(new MediaCommentRequestEvent(OperationType.DELETE, OperationTargetType.LOCAL_MEDIA_COMMENT, imageUUID, comment));
    }

    public static void editPhotoInRemoteMediaShare(Context context, MediaShare diffContentsInOriginalMediaShare, MediaShare diffContentsInModifiedMediaShare, MediaShare modifiedMediaShare) {

        EventBus.getDefault().post(new EditPhotoInMediaShareRequestEvent(OperationType.EDIT_PHOTO_IN_MEDIASHARE, OperationTargetType.REMOTE_MEDIA_SHARE, diffContentsInOriginalMediaShare, diffContentsInModifiedMediaShare, modifiedMediaShare));
    }

    public static void editPhotoInLocalMediaShare(Context context, MediaShare diffContentsInOriginalMediaShare, MediaShare diffContentsInModifiedMediaShare, MediaShare modifiedMediaShare) {

        EventBus.getDefault().post(new EditPhotoInMediaShareRequestEvent(OperationType.EDIT_PHOTO_IN_MEDIASHARE, OperationTargetType.LOCAL_MEDIA_SHARE, diffContentsInOriginalMediaShare, diffContentsInModifiedMediaShare, modifiedMediaShare));

    }

    public static HttpResponse RemoteCall(String req) throws MalformedURLException, IOException, SocketTimeoutException {

//        return GetRemoteCall(Gateway + ":" + FNAS.PORT + req);
        return OkHttpUtil.INSTANCE.remoteCallMethod(Util.HTTP_GET_METHOD, req, null);
    }

    public static HttpResponse GetRemoteCall(String url) throws MalformedURLException, IOException, SocketTimeoutException {

        HttpURLConnection conn;
        String str = "";

        conn = (HttpURLConnection) (new URL(url).openConnection());
        conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
        conn.setUseCaches(false);
        conn.setConnectTimeout(Util.HTTP_CONNECT_TIMEOUT);
        Log.d(TAG, "NAS GET: " + (url));

        if (conn.getResponseCode() == 200) {
            str = FNAS.ReadFull(conn.getInputStream());
        }

        return new HttpResponse(conn.getResponseCode(), str);


    }


    // create object and store it to the server
    public static HttpResponse PostRemoteCall(String req, String data) throws MalformedURLException, IOException, SocketTimeoutException {
//        return RemoteCallMethod(Util.HTTP_POST_METHOD, req, data);
        return OkHttpUtil.INSTANCE.remoteCallMethod(Util.HTTP_POST_METHOD, req, data);
    }

    // modify data and save it
    public static HttpResponse PatchRemoteCall(String req, String data) throws MalformedURLException, IOException, SocketTimeoutException {
        return RemoteCallMethod(Util.HTTP_PATCH_METHOD, req, data);
    }

    public static HttpResponse DeleteRemoteCall(String req, String data) throws MalformedURLException, IOException, SocketTimeoutException {
//        return RemoteCallMethod(Util.HTTP_DELETE_METHOD, req, data);
        return OkHttpUtil.INSTANCE.remoteCallMethod(Util.HTTP_DELETE_METHOD, req, data);
    }

    private static HttpResponse RemoteCallMethod(String httpMethod, String req, String data) throws MalformedURLException, IOException, SocketTimeoutException {
        HttpURLConnection conn;
        OutputStream outStream;
        String str;

        conn = (HttpURLConnection) (new URL(Gateway + ":" + FNAS.PORT + req).openConnection());
        conn.setRequestMethod(httpMethod);
        conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(Util.HTTP_CONNECT_TIMEOUT);
        conn.setReadTimeout(Util.HTTP_CONNECT_TIMEOUT);
        outStream = new BufferedOutputStream(conn.getOutputStream());
        if (data == null) str = "";
        else str = data;
        outStream.write(str.getBytes());
        outStream.flush();

        int responseCode = conn.getResponseCode();

        Log.d("winsun", "NAS " + httpMethod + " : " + (Gateway + ":" + FNAS.PORT + req) + " " + responseCode + " " + str);

        if (responseCode == 200) {
            str = FNAS.ReadFull(conn.getInputStream());
        }

        Log.d("winsun", "NAS " + httpMethod + " END: " + (Gateway + ":" + FNAS.PORT + req) + " " + str);

        outStream.close();
        conn.disconnect();

        return new HttpResponse(responseCode, str);

    }

    // get media files and cache it locally
    public static int RetrieveFNASFile(final String req, final String key) {
        BufferedInputStream bin;
        BufferedOutputStream bout;
        HttpURLConnection conn = null;
        byte[] buffer;
        String tempFile;
        int r;

        try {
            buffer = new byte[4096];
            tempFile = LocalCache.GetInnerTempFile();
            conn = (HttpURLConnection) (new URL(Gateway + ":" + FNAS.PORT + req).openConnection());
            conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
            conn.setConnectTimeout(15 * 1000);
            Log.d("winsun", Gateway + ":" + FNAS.PORT + req + "  " + JWT);
            bin = new BufferedInputStream(conn.getInputStream());
            bout = new BufferedOutputStream(new FileOutputStream(tempFile));
            while (true) {
                r = bin.read(buffer);
                if (r == -1) break;
                bout.write(buffer, 0, r);
            }
            bin.close();
            bout.close();
            LocalCache.MoveTempFileToThumbCache(tempFile, key);
            Log.d("winsun", Gateway + ":" + FNAS.PORT + req + "  Success!");
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void RetrieveFile(final String url, final String key) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedInputStream bin;
                BufferedOutputStream bout;
                byte[] buffer;
                String tempFile;
                int r;

                try {
                    buffer = new byte[4096];
                    tempFile = LocalCache.GetInnerTempFile();
                    bin = new BufferedInputStream(new URL(url).openConnection().getInputStream());
                    bout = new BufferedOutputStream(new FileOutputStream(tempFile));
                    while (true) {
                        r = bin.read(buffer);
                        if (r == -1) break;
                        bout.write(buffer, 0, r);
                    }
                    bin.close();
                    bout.close();
                    LocalCache.MoveTempFileToThumbCache(tempFile, key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static void restoreLocalPhotoUploadState(Context context) {

        DBUtils dbUtils = DBUtils.getInstance(context);

        Collection<Media> medias = LocalCache.LocalMediaMapKeyIsThumb.values();

        for (Media media : medias) {
            media.restoreUploadState();
        }

        dbUtils.updateLocalMediasUploadedFalse();

    }


    public static boolean UploadFile(String fileName) {
        Media localHashMap;

        String hash, url, boundary;
        BufferedOutputStream outStream;
        StringBuilder sb;
        InputStream is;
        byte[] buffer;
        int len, resCode;
        HttpURLConnection conn = null;

        try {
            while (JWT == null) Thread.sleep(500);
            // calc SHA256
            localHashMap = LocalCache.LocalMediaMapKeyIsThumb.get(fileName);
            hash = localHashMap.getUuid();

            Log.i(TAG, "thumb:" + fileName + "hash:" + hash);

            // head
            url = Gateway + ":" + FNAS.PORT + Util.DEVICE_ID_PARAMETER + "/" + LocalCache.DeviceID;
            Log.d(TAG, "Photo UP: " + url);
            boundary = java.util.UUID.randomUUID().toString();
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(60 * 1000);
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);
            conn.setRequestMethod(Util.HTTP_POST_METHOD); // Post方式
            conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setConnectTimeout(60 * 1000);

            outStream = new BufferedOutputStream(conn.getOutputStream());

            sb = new StringBuilder();
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
            sb.append("Content-Type: image/jpeg;\r\n");
            sb.append("\r\n");
            outStream.write(sb.toString().getBytes());

            is = new FileInputStream(fileName);
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

}
