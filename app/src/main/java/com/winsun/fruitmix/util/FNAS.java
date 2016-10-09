package com.winsun.fruitmix.util;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.model.Media;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2016/4/22.
 */
public class FNAS {

    public static final String TAG = FNAS.class.getSimpleName();

    public static String Gateway = "http://192.168.5.132";
    public static String JWT = null;
    public static String userUUID = null;

    public static String ReadFull(InputStream ins) {
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

    public static long deleteAllRemoteCommentIfNetworkConnected() {
        long result = 0;
        if (Util.getNetworkState(Util.APPLICATION_CONTEXT)) {
            result = DBUtils.SINGLE_INSTANCE.deleteAllRemoteComment();
            Log.i("delete result :", result + "");
        }
        return result;
    }

    public static String loadUser() throws Exception {

        return FNAS.RemoteCall(Util.USER_PARAMETER);

    }

    public static void fillUserMapByJsonString(String str) throws JSONException {

        String uuid;
        JSONArray json;
        JSONObject itemRaw;
        User item;

        json = new JSONArray(str);
        for (int i = 0; i < json.length(); i++) {
            itemRaw = json.getJSONObject(i);
            uuid = itemRaw.getString("uuid");
            if (LocalCache.RemoteUserMapKeyIsUUID.containsKey(uuid)) {
                item = LocalCache.RemoteUserMapKeyIsUUID.get(uuid);
            } else {
                item = new User();
            }

            item.setUserName(itemRaw.getString("username"));
            item.setUuid(itemRaw.getString("uuid"));
            item.setAvatar(itemRaw.getString("avatar"));
            if (itemRaw.has("email")) {
                item.setEmail(itemRaw.getString("email"));
            }

            StringBuilder stringBuilder = new StringBuilder();
            String[] splitStrings = item.getUserName().split(" ");
            for (String splitString : splitStrings) {
                stringBuilder.append(splitString.substring(0, 1).toUpperCase());
            }
            if (item.getDefaultAvatar() == null || item.getDefaultAvatar().equals("")) {
                item.setDefaultAvatar(stringBuilder.toString());
            }
            if (item.getDefaultAvatarBgColor() == null || item.getDefaultAvatarBgColor().equals("")) {
                item.setDefaultAvatarBgColor(String.valueOf(new Random().nextInt(3)));
            }

            LocalCache.RemoteUserMapKeyIsUUID.put(item.getUuid(), item); // save all user info

        }

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
        dbUtils.insertRemoteUsers(LocalCache.RemoteUserMapKeyIsUUID);

        Log.d("winsun", "RemoteUserMapKeyIsUUID " + LocalCache.RemoteUserMapKeyIsUUID);
    }

    public static String loadMedia() throws Exception {

        return FNAS.RemoteCall(Util.MEDIA_PARAMETER); // get all pictures;

    }

    public static String loadRemoteShare() throws Exception {
        return FNAS.RemoteCall(Util.MEDIASHARE_PARAMETER);
    }

    public static String loadRemoteMediaComment(Context context, String mediaUUID) throws Exception {
        return FNAS.RemoteCall(String.format(context.getString(R.string.photo_comment_url), Util.MEDIA_PARAMETER + "/" + mediaUUID));
    }

    public static String loadToken(Context context, String gateway, String userUUID, String userPassword) throws Exception {

        HttpURLConnection conn;
        String str = "";
        conn = (HttpURLConnection) (new URL(gateway + Util.TOKEN_PARAMETER).openConnection()); //output:{"type":"JWT","token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1dWlkIjoiZGIzYWVlZWYtNzViYS00ZTY2LThmMGUtNWQ3MTM2NWEwNGRiIn0.LqISPNt6T5M1Ae4GN3iL0d8D1bj6m0tX7YOwqZqlnvg"}
        conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((userUUID + ":" + userPassword).getBytes(), Base64.DEFAULT));
        conn.setConnectTimeout(Util.HTTP_CONNECT_TIMEOUT);
        if (conn.getResponseCode() != 200) {
            throw new NetworkErrorException();
        } else {
            str = FNAS.ReadFull(conn.getInputStream());
        }

        Log.i(TAG, "loadToken: " + str);

        return str;
    }

    public static void loadDeviceId() throws Exception {
        String str;
        if (LocalCache.DeviceID == null || LocalCache.DeviceID.equals("")) {
            str = FNAS.PostRemoteCall("/library/", "");
            LocalCache.DeviceID = str.replace("\"", "");
            LocalCache.SetGlobalData(Util.DEVICE_ID_MAP_NAME, LocalCache.DeviceID);
        }
        Log.d(TAG, "deviceID: " + LocalCache.GetGlobalData(Util.DEVICE_ID_MAP_NAME));
    }

    public static void loadData(Context context) {

        retrieveUserMap(context);

        retrieveMediaMap(context);

        retrieveShareMap(context);

        retrieveLocalMediaCommentMap(context);
    }

    public static void retrieveUserMap(Context context) {

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_USER.name());
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(intent);

    }

    public static void retrieveMediaMap(Context context) {

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIA.name());
        localBroadcastManager.sendBroadcast(intent);

        intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIA.name());
        localBroadcastManager.sendBroadcast(intent);
    }

    public static void retrieveShareMap(Context context) {

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());
        localBroadcastManager.sendBroadcast(intent);

        intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIASHARE.name());
        localBroadcastManager.sendBroadcast(intent);
    }

    public static void retrieveLocalMediaCommentMap(Context context) {

        Intent intent = new Intent(Util.OPERATION);
        intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.GET.name());
        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.LOCAL_MEDIA_COMMENT.name());
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.sendBroadcast(intent);

    }

    public static void startUploadAllLocalPhoto(Context context) {

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);

        for (Media media : LocalCache.LocalMediaMapKeyIsThumb.values()) {

            if (!media.isUploaded()) {
                Intent intent = new Intent(Util.OPERATION);
                intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.CREATE.name());
                intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIA.name());
                intent.putExtra(Util.OPERATION_MEDIA, media);
                localBroadcastManager.sendBroadcast(intent);
            }

        }

    }


    public static String RemoteCall(String req) throws Exception {
        HttpURLConnection conn;
        String str = "";

        conn = (HttpURLConnection) (new URL(Gateway + req).openConnection());
        conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
        conn.setUseCaches(false);
        conn.setConnectTimeout(Util.HTTP_CONNECT_TIMEOUT);
        Log.d(TAG, "NAS GET: " + (Gateway + req));
        str = FNAS.ReadFull(conn.getInputStream());


        return str;
    }

    // create object and store it to the server
    public static String PostRemoteCall(String req, String data) throws Exception {
        HttpURLConnection conn;
        OutputStream outStream;
        String str;

        conn = (HttpURLConnection) (new URL(Gateway + req).openConnection());
        conn.setRequestMethod(Util.HTTP_POST_METHOD);
        conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(Util.HTTP_CONNECT_TIMEOUT);
        outStream = new BufferedOutputStream(conn.getOutputStream());
        if (data == null) str = "";
        else str = data;
        outStream.write(str.getBytes());
        outStream.flush();

        Log.d(TAG, "NAS POST: " + (Gateway + req) + " " + conn.getResponseCode() + " " + str);
        str = FNAS.ReadFull(conn.getInputStream());
        Log.d(TAG, "NAS POST END: " + (Gateway + req) + " " + str);

        outStream.close();
        conn.disconnect();
        return str;
    }

    // modify data and save it
    public static String PatchRemoteCall(String req, String data) throws Exception {
        HttpURLConnection conn;
        OutputStream outStream;
        String str;

        conn = (HttpURLConnection) (new URL(Gateway + req).openConnection());
        conn.setRequestMethod(Util.HTTP_PATCH_METHOD);
        conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(Util.HTTP_CONNECT_TIMEOUT);
        outStream = new BufferedOutputStream(conn.getOutputStream());
        if (data == null) str = "";
        else str = data;
        outStream.write(str.getBytes());
        outStream.flush();


        Log.d("winsun", "NAS PATCH: " + (Gateway + req) + " " + conn.getResponseCode() + " " + str);
        str = FNAS.ReadFull(conn.getInputStream());
        Log.d("winsun", "NAS PATCH END: " + (Gateway + req) + " " + str);

        outStream.close();
        conn.disconnect();
        return str;
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
            conn = (HttpURLConnection) (new URL(Gateway + req).openConnection());
            conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
            conn.setConnectTimeout(15 * 1000);
            Log.d("winsun", Gateway + req + "  " + JWT);
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
            Log.d("winsun", Gateway + req + "  Success!");
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

    public static void restoreLocalPhotoUploadState() {

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        for (Media media : LocalCache.LocalMediaMapKeyIsThumb.values()) {
            media.restoreUploadState();

            dbUtils.updateLocalMedia(media);
        }

    }


    public static boolean UploadFile(String fname) {
        Media localHashMap;

        String hash, url, boundary;
        BufferedOutputStream outStream;
        StringBuilder sb;
        InputStream is;
        byte[] buffer;
        int len, resCode;

        try {
            while (JWT == null) Thread.sleep(500);
            // calc SHA256
            localHashMap = LocalCache.LocalMediaMapKeyIsThumb.get(fname);
            hash = localHashMap.getUuid();

            Log.i(TAG, "thumb:" + fname + "hash:" + hash);

            // head
            url = Gateway + "/library/" + LocalCache.DeviceID + "?hash=" + hash;
            Log.d(TAG, "Photo UP: " + url);
            boundary = java.util.UUID.randomUUID().toString();
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(60 * 1000);
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);
            conn.setRequestMethod(Util.HTTP_POST_METHOD); // Post方式
            conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setConnectTimeout(Util.HTTP_CONNECT_TIMEOUT);

            outStream = new BufferedOutputStream(conn.getOutputStream());

            sb = new StringBuilder();
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fname).append("\"\r\n");
            sb.append("Content-Type: image/jpeg;\r\n");
            sb.append("\r\n");
            outStream.write(sb.toString().getBytes());

            is = new FileInputStream(fname);
            buffer = new byte[15000];
            len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            is.close();
            outStream.write("\r\n".getBytes());

            outStream.write(("--" + boundary + "--\r\n").getBytes());
            outStream.flush();

            resCode = conn.getResponseCode();
            Log.d(TAG, "UP END1: " + resCode);
            if (resCode == 200) return true;
            if (resCode == 404) {
                LocalCache.DropGlobalData(Util.DEVICE_ID_MAP_NAME);
                System.exit(0);
            }
            InputStream in = conn.getInputStream();
            InputStreamReader isReader = new InputStreamReader(in);
            BufferedReader bufReader = new BufferedReader(isReader);
            String line = null;
            String data = "";

            while ((line = bufReader.readLine()) != null)
                data += line;

            outStream.close();
            conn.disconnect();

            Log.d(TAG, "UP END: " + resCode + " " + data);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isPhotoInMediaMap(String imageUUID) {
        return LocalCache.RemoteMediaMapKeyIsUUID.containsKey(imageUUID);
    }

    public static void delShareInDocumentsMapById(String uuid) {
        LocalCache.RemoteMediaShareMapKeyIsUUID.remove(uuid);
    }

    public static void loadLocalShare() {

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        List<MediaShare> mediaShareList = dbUtils.getAllLocalShare();

        for (MediaShare mediaShare : mediaShareList) {

            LocalCache.RemoteMediaShareMapKeyIsUUID.put(mediaShare.getUuid(), mediaShare);

        }

    }

    public static void checkLocalShareAndComment(final Context context) {
        if (Util.getNetworkState(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                    List<MediaShare> mediaShares = dbUtils.getAllLocalShare();
                    Map<String, List<Comment>> comments = dbUtils.getAllLocalImageCommentKeyIsImageUUID();

                    Intent intent = new Intent(Util.OPERATION);
                    intent.putExtra(Util.OPERATION_TYPE_NAME, OperationType.CREATE.name());
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);

                    if (!mediaShares.isEmpty()) {

                        Log.i(TAG, "start local share task");

                        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIASHARE.name());

                        for (MediaShare mediaShare : mediaShares) {

                            intent.putExtra(Util.OPERATION_MEDIASHARE, mediaShare);
                            localBroadcastManager.sendBroadcast(intent);

                        }

                    }

                    if (!comments.isEmpty()) {

                        Log.i(TAG, "start local comment task");

                        intent.removeExtra(Util.OPERATION_TARGET_TYPE_NAME);
                        intent.putExtra(Util.OPERATION_TARGET_TYPE_NAME, OperationTargetType.REMOTE_MEDIA_COMMENT.name());

                        for (Map.Entry<String, List<Comment>> entry : comments.entrySet()) {
                            for (Comment comment : entry.getValue()) {

                                intent.putExtra(Util.OPERATION_COMMENT, comment);
                                intent.putExtra(Util.OPERATION_IMAGE_UUID, entry.getKey());
                                localBroadcastManager.sendBroadcast(intent);

                            }

                        }

                    }

                }
            }).start();
        }
    }

}
