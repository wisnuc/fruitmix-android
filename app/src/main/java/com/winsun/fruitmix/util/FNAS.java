package com.winsun.fruitmix.util;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.MediaShare;
import com.winsun.fruitmix.services.CreateRemoteCommentService;
import com.winsun.fruitmix.services.CreateRemoteMediaService;
import com.winsun.fruitmix.services.CreateRemoteMediaShareService;

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
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
        ConcurrentMap<String, String> item;

        json = new JSONArray(str);
        for (int i = 0; i < json.length(); i++) {
            itemRaw = json.getJSONObject(i);
            uuid = itemRaw.getString("uuid");
            if (LocalCache.UsersMap.containsKey(uuid)) {
                item = LocalCache.UsersMap.get(uuid);
            } else {
                item = new ConcurrentHashMap<>();
            }

            item.put("name", itemRaw.getString("username"));
            item.put("uuid", itemRaw.getString("uuid"));
            item.put("avatar", itemRaw.getString("avatar"));
            if (itemRaw.has("email")) {
                item.put("email", itemRaw.getString("email"));
            }

            StringBuilder stringBuilder = new StringBuilder();
            String[] splitStrings = item.get("name").split(" ");
            for (String splitString : splitStrings) {
                stringBuilder.append(splitString.substring(0, 1).toUpperCase());
            }
            if (!item.containsKey("avatar_default")) {
                item.put("avatar_default", stringBuilder.toString());
            }
            if (!item.containsKey("avatar_default_color")) {
                item.put("avatar_default_color", String.valueOf(new Random().nextInt(3)));
            }

            LocalCache.UsersMap.put(item.get("uuid"), item); // save all user info

        }

        LocalCache.SetGlobalHashMap(Util.USER_MAP_NAME, LocalCache.UsersMap);
        Log.d("winsun", "UsersMap " + LocalCache.UsersMap);
    }

    public static String loadMedia() throws Exception {

        return FNAS.RemoteCall(Util.MEDIA_PARAMETER); // get all pictures;

    }

    public static void fillMediaMapByJsonString(String str) throws JSONException {

        String mtime;
        JSONArray json;
        JSONObject itemRaw;
        ConcurrentMap<String, String> item;

        json = new JSONArray(str);

        if (json.length() != 0) {
            LocalCache.MediasMap.clear();
        }

        for (int i = 0; i < json.length(); i++) {
            itemRaw = json.getJSONObject(i);
            if (itemRaw.getString("kind").equals("image")) {
                item = new ConcurrentHashMap<>();
                item.put("uuid", "" + itemRaw.getString("hash"));
                item.put("mtime", "1916-01-01 00:00:00");
                if (itemRaw.has("width")) {

                    item.put("width", itemRaw.getString("width"));
                    item.put("height", itemRaw.getString("height"));

                } else if (itemRaw.getJSONObject("detail").has("width")) {
                    item.put("width", itemRaw.getJSONObject("detail").getString("width"));
                    item.put("height", itemRaw.getJSONObject("detail").getString("height"));
                } else {
                    item.put("width", itemRaw.getJSONObject("detail").getJSONObject("exif").getString("ExifImageWidth"));
                    item.put("height", itemRaw.getJSONObject("detail").getJSONObject("exif").getString("ExifImageHeight"));
                    if (itemRaw.getJSONObject("detail").has("exif") && itemRaw.getJSONObject("detail").getJSONObject("exif").has("CreateDate")) {
                        mtime = itemRaw.getJSONObject("detail").getJSONObject("exif").getString("CreateDate");
                        item.put("mtime", mtime.substring(0, 4) + "-" + mtime.substring(5, 7) + "-" + mtime.substring(8));
                    } else item.put("mtime", "1916-01-01 00:00:00");
                }
                LocalCache.MediasMap.put(item.get("uuid"), item);

            }
        }

        LocalCache.SetGlobalHashMap(Util.MEDIA_MAP_NAME, LocalCache.MediasMap);
        Log.d("winsun", "MediasMap " + LocalCache.MediasMap);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(Util.APPLICATION_CONTEXT);
        manager.sendBroadcast(new Intent(Util.REMOTE_PHOTO_LOADED));
    }

    public static String loadRemoteShare() throws Exception {
        return FNAS.RemoteCall(Util.MEDIASHARE_PARAMETER);
    }

    public static void fillShareMapByJsonString(String str) throws JSONException {

        String uuid, imgStr;
        JSONArray json, jsonArr;
        JSONObject itemRaw;
        ConcurrentMap<String, String> item;

        json = new JSONArray(str);

        if (json.length() != 0) {
            LocalCache.SharesMap.clear();
        }

        for (int i = 0; i < json.length(); i++) {
            itemRaw = json.getJSONObject(i);
            Log.d("winsun", "" + itemRaw);
            uuid = itemRaw.getString("uuid");
            if (LocalCache.SharesMap.containsKey(uuid)) {
                item = LocalCache.SharesMap.get(uuid);
            } else item = new ConcurrentHashMap<>();
            item.put("_id", itemRaw.getJSONObject("latest").getString("_id"));
            if (itemRaw.getJSONObject("latest").has("creator"))
                item.put("creator", itemRaw.getJSONObject("latest").getString("creator"));
            else
                item.put("creator", itemRaw.getJSONObject("latest").getJSONArray("maintainers").getString(0));
            item.put("album", itemRaw.getJSONObject("latest").getString("album"));
            item.put("mtime", itemRaw.getJSONObject("latest").getString("mtime"));
            item.put("uuid", uuid);
            item.put("del", itemRaw.getJSONObject("latest").getString("archived").equals("true") ? "1" : "0"); // 1 means deleted,0 means not deleted,（archived是服务端是否已删标志）
            item.put("date", new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(itemRaw.getJSONObject("latest").getString("mtime")))));
            if (itemRaw.getJSONObject("latest").getString("album").equals("true")) {
                item.put("type", "album");
                item.put("title", itemRaw.getJSONObject("latest").getJSONArray("tags").getJSONObject(0).getString("albumname"));
                item.put("desc", itemRaw.getJSONObject("latest").getJSONArray("tags").getJSONObject(0).getString("desc"));
            } else item.put("type", "set");
            imgStr = "";
            jsonArr = itemRaw.getJSONObject("latest").getJSONArray("contents");
            if (jsonArr.length() > 0) {
                for (int j = 0; j < jsonArr.length(); j++)
                    imgStr += "," + jsonArr.getJSONObject(j).getString("digest").toLowerCase();
                if (imgStr.length() > 1) imgStr = imgStr.substring(1);
                item.put("images", imgStr);
                item.put("coverImg", jsonArr.getJSONObject(0).getString("digest").toLowerCase());
            } else {
                item.put("images", "");
                item.put("coverImg", "");
            }
            if (itemRaw.getJSONObject("latest").getJSONArray("viewers").length() <= 1 && itemRaw.getJSONObject("latest").getJSONArray("maintainers").length() <= 1)
                item.put("private", "true");
            else item.put("private", "false"); // 1 means private,0 means public

            boolean isMaintainer = false;
            JSONArray jsonArray = itemRaw.getJSONObject("latest").getJSONArray("maintainers");
            for (int k = 0; k < jsonArray.length(); k++) {
                if (jsonArray.getString(k).equals(userUUID)) {
                    isMaintainer = true;
                }
            }
            item.put("maintained", isMaintainer ? "true" : "false");

            item.put("locked", "false");

            LocalCache.SharesMap.put(uuid, item);

        }

    }

    public static void retrieveUserMap() {
        try {
            fillUserMapByJsonString(loadUser());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void retrieveMediaMap() {
        try {
            fillMediaMapByJsonString(loadMedia());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void retrieveShareMap() {
        try {
            fillShareMapByJsonString(loadRemoteShare());
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadLocalShare();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(Util.APPLICATION_CONTEXT);
        manager.sendBroadcast(new Intent(Util.SHARE_LOADED));

        LocalCache.SetGlobalHashMap(Util.SHARE_MAP_NAME, LocalCache.SharesMap);
        Log.d("winsun", "SharesMap " + LocalCache.SharesMap);
    }

    public static void loadData() {

        deleteAllRemoteCommentIfNetworkConnected();

        retrieveUserMap();

        retrieveMediaMap();

        CreateRemoteMediaService.startActionCreateRemoteMedia(Util.APPLICATION_CONTEXT);

        retrieveShareMap();

    }


    public static String RemoteCall(String req) throws Exception {
        HttpURLConnection conn;
        String str = "";

        while (JWT == null) Thread.sleep(500);

        conn = (HttpURLConnection) (new URL(Gateway + req).openConnection());
        conn.setRequestProperty(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
        conn.setUseCaches(false);
        conn.setConnectTimeout(Util.HTTP_CONNECT_TIMEOUT);
        Log.d(TAG, "NAS GET: " + (Gateway + req));
        if (conn.getResponseCode() == 200) {
            str = FNAS.ReadFull(conn.getInputStream());
        }

        return str;
    }

    // create object and store it to the server
    public static String PostRemoteCall(String req, String data) throws Exception {
        HttpURLConnection conn;
        OutputStream outStream;
        String str;

        while (JWT == null) Thread.sleep(500);

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

        while (JWT == null) Thread.sleep(500);

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
        for (ConcurrentMap<String, String> map : LocalCache.LocalImagesMapKeyIsThumb.values()) {
            map.put(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS, "false");
        }
        LocalCache.SetGlobalHashMap(Util.LOCAL_IMAGE_MAP_NAME, LocalCache.LocalImagesMapKeyIsThumb);
    }

    public static boolean UploadFile(String fname) {
        ConcurrentMap<String, String> localHashMap;

        String hash, url, boundary;
        BufferedOutputStream outStream;
        StringBuilder sb;
        InputStream is;
        byte[] buffer;
        int len, resCode;

        try {
            while (JWT == null) Thread.sleep(500);
            // calc SHA256
            localHashMap = LocalCache.LocalImagesMapKeyIsThumb.get(fname);
            hash = localHashMap.get("uuid");

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
        return LocalCache.MediasMap.containsKey(imageUUID);
    }

    public static void delShareInDocumentsMapById(String uuid) {
        LocalCache.SharesMap.remove(uuid);
    }

    public static void loadLocalShare() {

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        List<MediaShare> mediaShareList = dbUtils.getAllLocalShare();
        ConcurrentMap<String, String> item;

        for (MediaShare mediaShare : mediaShareList) {

            if (LocalCache.SharesMap.containsKey(mediaShare.getUuid())) {
                item = LocalCache.SharesMap.get(mediaShare.getUuid());
            } else item = new ConcurrentHashMap<>();

            item.put("_id", "");
            item.put("creator", FNAS.userUUID);
            if (mediaShare.isAlbum()) {
                item.put("type", "album");
            } else {
                item.put("type", "set");
            }
            item.put("mtime", mediaShare.getTime());
            item.put("uuid", mediaShare.getUuid());
            item.put("del", "0"); // 本地存在，判断是否需要显示（archived是服务端是否已删标志）
            item.put("date", new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(System.currentTimeMillis())));
            item.put("title", mediaShare.getTitle());
            item.put("desc", mediaShare.getDesc());

            StringBuilder builder = new StringBuilder();
            for (String digest : mediaShare.getImageDigests()) {
                builder.append(digest);
                builder.append(",");
            }

            item.put("images", builder.toString());

            item.put("coverImg", mediaShare.getCoverImageDigest().toLowerCase());

            item.put("private", String.valueOf(mediaShare.isPrivate()));

            item.put("maintained", String.valueOf(mediaShare.isMaintained()));
            item.put("local", "true");

            Log.i(TAG, "local share:" + item.toString());

            LocalCache.SharesMap.put(mediaShare.getUuid(), item);

        }

    }

    public static void checkLocalShareAndComment(final Context context) {
        if (Util.getNetworkState(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                    if (!dbUtils.getAllLocalShare().isEmpty()) {

                        Log.i(TAG, "start local share task");

                        CreateRemoteMediaShareService.startActionCreateRemoteMediaShareTask(context);
                    }

                    if (!dbUtils.getAllLocalImageComment().isEmpty()) {

                        Log.i(TAG, "start local comment task");

                        CreateRemoteCommentService.startActionCreateRemoteCommentTask(context);
                    }

                }
            }).start();
        }
    }

}
