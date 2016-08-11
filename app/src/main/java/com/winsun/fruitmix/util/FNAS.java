package com.winsun.fruitmix.util;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Share;
import com.winsun.fruitmix.model.OfflineTask;
import com.winsun.fruitmix.services.LocalCommentService;
import com.winsun.fruitmix.services.LocalShareService;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/22.
 */
public class FNAS {

    public static final String TAG = FNAS.class.getSimpleName();

    //public static String Gateway="http://192.168.5.132";
//    public static String Gateway="http://192.168.5.108";
    public static String Gateway = "http://192.168.5.132";
    public static String JWT = null;
    public static String userUUID = null;

    public static String ReadFull(InputStream ins) throws IOException {
        BufferedReader br;
        String result, line;

        br = new BufferedReader(new InputStreamReader(ins));
        result = "";
        while ((line = br.readLine()) != null) {
            Log.d("winsun", "aaa " + line);
            result += line;
        }

        Log.d("winsun", result);
        return result;
    }

    public static void Login() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String str;
                    JSONObject json;
                    HttpURLConnection conn;

                    // get addr first

                    Log.d("wisun", Gateway + "/login"); // output:{"username":"admin","uuid":"db3aeeef-75ba-4e66-8f0e-5d71365a04db","avatar":"defaultAvatar.jpg"}
                    conn = (HttpURLConnection) (new URL(Gateway + "/login").openConnection());
                    str = ReadFull(conn.getInputStream());
                    userUUID = "e1ea7108-cfd7-4b4a-bbbd-99feeb1a6ca6"; //new JSONArray(str).getJSONObject(0).getString("uuid");

                    conn = (HttpURLConnection) (new URL(Gateway + "/token").openConnection()); //output:{"type":"JWT","token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1dWlkIjoiZGIzYWVlZWYtNzViYS00ZTY2LThmMGUtNWQ3MTM2NWEwNGRiIn0.LqISPNt6T5M1Ae4GN3iL0d8D1bj6m0tX7YOwqZqlnvg"}
                    conn.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((userUUID + ":123456").getBytes(), Base64.DEFAULT));
                    str = ReadFull(conn.getInputStream());
                    JWT = new JSONObject(str).getString("token"); // get token

                    // UUID
                    // LocalCache.DeviceID="6830ebc0-03cf-477c-b62f-e95ac863058a";
                    if (LocalCache.DeviceID == null) {
                        //SetGlobalData("deviceID", UUID.randomUUID().toString());
                        str = PostRemoteCall("/library/", "");
                        LocalCache.DeviceID = str.replace("\"", "");
                        LocalCache.SetGlobalData("deviceID", LocalCache.DeviceID);
                    } // get deviceID
                    Log.d("uuid", LocalCache.GetGlobalData("deviceID"));


                    //PostRemoteCall("/mediashare", "{\"uuid\":\"11\" }") {}


                    // documents
                    LoadDocuments(); // get all available data

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void LoadDocuments() throws Exception {

        String str, date, uuid, lhash;
        JSONArray json, jsonArr;
        int i, j;
        JSONObject itemRaw, jsonObject;
        Map<String, String> item;
        List<String> newList;
        String imgStr, mtime;

        if (Util.getNetworkState(Util.APPLICATION_CONTEXT)) {
            long result = DBUtils.SINGLE_INSTANCE.deleteAllRemoteComment();
            Log.i("delete result :", result + "");
        }

        loadLocalShare();

        str = FNAS.RemoteCall("/users"); // get all user;
        json = new JSONArray(str);
        for (i = 0; i < json.length(); i++) {
            itemRaw = json.getJSONObject(i);
            uuid = itemRaw.getString("uuid");
            item = new HashMap<String, String>();
            item.put("name", itemRaw.getString("username"));
            item.put("uuid", itemRaw.getString("uuid"));
            item.put("avatar", itemRaw.getString("avatar"));
            if (itemRaw.has("email")) {
                item.put("email", itemRaw.getString("email"));
            }
            StringBuilder stringBuilder = new StringBuilder();
            String[] splitStrings = itemRaw.getString("username").split(" ");
            for (String splitString : splitStrings) {
                stringBuilder.append(splitString.substring(0, 1).toUpperCase());
            }
            item.put("avatar_default",stringBuilder.toString());
            int color = (int)(Math.random() * 3);
            item.put("avatar_default_color",color+"");

            LocalCache.UsersMap.put(item.get("uuid"), item); // save all user info
        }

        str = FNAS.RemoteCall("/media"); // get all pictures;
        json = new JSONArray(str);

        LocalCache.MediasMap.clear();

        for (i = 0; i < json.length(); i++) {
            try {
                itemRaw = json.getJSONObject(i);
                if (itemRaw.getString("kind").equals("image")) {
                    item = new HashMap<String, String>();
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
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

        // upload
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UploadAll();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                LocalCache.SetGlobalHashMap("localImagesMap", LocalCache.LocalImagesMap);

            }
        }).start();


        str = FNAS.RemoteCall("/mediashare"); // get all album share and normal share(immutable)
        json = new JSONArray(str);
        for (i = 0; i < json.length(); i++) {
            itemRaw = json.getJSONObject(i);
            Log.d("winsun", "" + itemRaw);
            uuid = itemRaw.getString("uuid");
            if (LocalCache.DocumentsMap.containsKey(uuid)) {
                item = LocalCache.DocumentsMap.get(uuid);
                //if(itemRaw.getJSONObject("latest").get("_id").equals()) continue;
            } else item = new HashMap<String, String>();
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
                for (j = 0; j < jsonArr.length(); j++)
                    imgStr += "," + jsonArr.getJSONObject(j).getString("digest").toLowerCase();
                if (imgStr.length() > 1) imgStr = imgStr.substring(1);
                item.put("images", imgStr);
                item.put("coverImg", jsonArr.getJSONObject(0).getString("digest").toLowerCase());
                if (itemRaw.getJSONObject("latest").getJSONArray("viewers").length() <= 1 && itemRaw.getJSONObject("latest").getJSONArray("maintainers").length() <= 1)
                    item.put("private", "1");
                else item.put("private", "0"); // 1 means private,0 means public

                //begin add by liang.wu
                boolean isMaintainer = false;
                JSONArray jsonArray = itemRaw.getJSONObject("latest").getJSONArray("maintainers");
                for (int k = 0; k < jsonArray.length(); k++) {

                    if (jsonArray.getString(k).equals(userUUID)) {
                        isMaintainer = true;
                    }
                }
                item.put("maintained", isMaintainer ? "true" : "false");

                item.put("locked", "false");
                //finish add by liang.wu

                LocalCache.DocumentsMap.put(uuid, item);
            }
        }

        //LocalCache.SetGlobalHashMap("documentsMap", LocalCache.DocumentsMap);
        //Log.d("winsun", "List: " + newList);

        LocalCache.SetGlobalHashMap("usersMap", LocalCache.UsersMap);
        Log.d("winsun", "UsersMap " + LocalCache.UsersMap);

        LocalCache.SetGlobalHashMap("documentsMap", LocalCache.DocumentsMap);
        Log.d("winsun", "DocumentsMap " + LocalCache.DocumentsMap);

        LocalCache.SetGlobalHashMap("mediasMap", LocalCache.MediasMap);
        Log.d("winsun", "MediasMap " + LocalCache.MediasMap);

    }


    public static String RemoteCall(String req) throws Exception {
        HttpURLConnection conn;
        String str;

        while (JWT == null) Thread.sleep(500);

        conn = (HttpURLConnection) (new URL(Gateway + req).openConnection());
        conn.setRequestProperty("Authorization", "JWT " + JWT);
        conn.setConnectTimeout(15 * 1000);
        Log.d("winsun", "NAS GET: " + (Gateway + req));
        str = FNAS.ReadFull(conn.getInputStream());

        return str;
    }

    // create object and store it to the server
    public static String PostRemoteCall(String req, String data) throws Exception {
        HttpURLConnection conn;
        OutputStream outStream;
        String str;

        while (JWT == null) Thread.sleep(500);

        conn = (HttpURLConnection) (new URL(Gateway + req).openConnection());
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "JWT " + JWT);
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false);
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(15 * 1000);
        outStream = new BufferedOutputStream(conn.getOutputStream());
        //str="{\"version\":\"0.1.G513b\",\"permission\":\"public\",\"doctype\":\"album\",\"content\":{\"title\":\"test1\",\"desc\":\"test1desc\",\"items\":[{\"type\":\"media1\",\"uuid\":\"75922aa33a961c3769966e70bec6430e4d5a6c8028e5d6616d5a5a599f337483\"}]}}";
        if (data == null) str = "";
        else str = data;
        outStream.write(str.toString().getBytes());
        outStream.flush();

        Log.d("winsun", "NAS POST: " + (Gateway + req) + " " + conn.getResponseCode() + " " + str);
        str = FNAS.ReadFull(conn.getInputStream());
        Log.d("winsun", "NAS POST END: " + (Gateway + req) + " " + str);

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
        //conn=(HttpURLConnection) (new URL("http://192.168.1.102:9220" + req).openConnection());
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Authorization", "JWT " + JWT);
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false);
        conn.setRequestProperty("Connection", "keep-alive");
        //conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(15 * 1000);
        outStream = new BufferedOutputStream(conn.getOutputStream());
        //str="{\"version\":\"0.1.G513b\",\"permission\":\"public\",\"doctype\":\"album\",\"content\":{\"title\":\"test1\",\"desc\":\"test1desc\",\"items\":[{\"type\":\"media1\",\"uuid\":\"75922aa33a961c3769966e70bec6430e4d5a6c8028e5d6616d5a5a599f337483\"}]}}";
        if (data == null) str = "";
        else str = data;
        outStream.write(str.toString().getBytes());
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
            conn.setRequestProperty("Authorization", "JWT " + JWT);
            conn.setConnectTimeout(15 * 1000);
            Log.d("winsun", Gateway + req + "  " + JWT);
            bin = new BufferedInputStream(conn.getInputStream());
            bout = new BufferedOutputStream(new FileOutputStream(tempFile));
            while (true) {
                r = bin.read(buffer);
                if (r == -1) break;
                bout.write(buffer, 0, r);
                // Log.d("winsun", new String(buffer, 0, r)); "createing thumb"
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

    public static void UploadAll() {
        Map<String, String> itemRaw;

        for (String key : LocalCache.LocalImagesMap.keySet()) {
            itemRaw = LocalCache.LocalImagesMap.get(key);
            //Log.d("winsun", "XX "+itemRaw+"");
            if (itemRaw != null) {

                if (!itemRaw.containsKey(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS) || itemRaw.get(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS).equals("false")) {
                    boolean result = UploadFile(itemRaw.get("thumb"));
                    itemRaw.put(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS, result + "");
                }

            }
        }

    }


    public static boolean UploadFile(String fname) {
        HashMap<String, Map<String, String>> localHashMap;
        Map<String, String> localHashObj;
        String hash, url, boundary;
        BufferedOutputStream outStream;
        StringBuilder sb;
        InputStream is;
        byte[] buffer;
        int len, resCode;

        try {
            while (JWT == null) Thread.sleep(500);
            // calc SHA256
            localHashMap = LocalCache.GetGlobalHashMap("localHashMap");
            if (!localHashMap.containsKey(fname)) {
                localHashObj = new HashMap<String, String>();
                localHashObj.put("digest", Util.CalcSHA256OfFile(fname));
                localHashMap.put(fname, localHashObj);
            }
            hash = localHashMap.get(fname).get("digest");

            // head
            url = Gateway + "/library/" + LocalCache.DeviceID + "?hash=" + hash;
            Log.d("winsun", "UP: " + url);
            boundary = java.util.UUID.randomUUID().toString();
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(60 * 1000);
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);
            conn.setRequestMethod("POST"); // Post方式
            conn.setRequestProperty("Authorization", "JWT " + JWT);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setConnectTimeout(15 * 1000);

            outStream = new BufferedOutputStream(conn.getOutputStream());

            sb = new StringBuilder();
            sb.append("--" + boundary + "\r\n");
            sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fname + "\"\r\n");
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
            Log.d("winsun", "UP END1: " + resCode);
            if (resCode == 200) return true;
            if (resCode == 404) {
                LocalCache.DropGlobalData("deviceID");
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

            Log.d("winsun", "UP END: " + resCode + " " + data);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isPhotoInMediaMap(String imageUUID) {
        return LocalCache.MediasMap.containsKey(imageUUID);
    }

    public static void delShareInDocumentsMapById(String uuid) {
        LocalCache.DocumentsMap.remove(uuid);
    }

    public static void loadLocalShare() {

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

        List<Share> shareList = dbUtils.getAllLocalShare();
        Map<String, String> item;

        for (Share share : shareList) {

            if (LocalCache.DocumentsMap.containsKey(share.getUuid())) {
                item = LocalCache.DocumentsMap.get(share.getUuid());
                //if(itemRaw.getJSONObject("latest").get("_id").equals()) continue;
            } else item = new HashMap<String, String>();

            item.put("_id", "");
            item.put("creator", FNAS.userUUID);
            if (share.isAlbum()) {
                item.put("type", "album");
            } else {
                item.put("type", "set");
            }
            item.put("mtime", String.valueOf(System.currentTimeMillis()));
            item.put("uuid", share.getUuid());
            item.put("del", "0"); // 本地存在，判断是否需要显示（archived是服务端是否已删标志）
            item.put("date", new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(System.currentTimeMillis())));
            item.put("title", share.getTitle());
            item.put("desc", share.getDesc());
            item.put("images", share.getDigest());

            String[] digests = share.getDigest().split(",");
            item.put("coverImg", digests[0].toLowerCase());

            String[] maintainers = share.getMaintainer().split(",");
            if (share.getViewer().split(",").length <= 1 && maintainers.length <= 1) {
                item.put("private", "1");
            } else {
                item.put("private", "0");
            }

            boolean isMaintainer = false;
            for (int i = 0; i < maintainers.length; i++) {
                if (maintainers[i].equals(FNAS.userUUID)) {
                    isMaintainer = true;
                }
            }
            item.put("maintained", isMaintainer ? "true" : "false");
            item.put("local", "true");

            Log.i(TAG, "local share:" + item.toString());

            LocalCache.DocumentsMap.put(share.getUuid(), item);

        }

    }

    public static void checkLocalShareAndComment(final Context context) {
        if (Util.getNetworkState(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                    if (!dbUtils.getAllLocalShare().isEmpty()) {

                        Log.i(TAG, "start local share task");

                        LocalShareService.startActionLocalShareTask(context);
                    }

                    if (!dbUtils.getAllLocalImageComment().isEmpty()) {

                        Log.i(TAG, "start local comment task");

                        LocalCommentService.startActionLocalCommentTask(context);
                    }

                }
            }).start();
        }
    }

    public static void checkOfflineTask(Context context) {
        if (Util.getNetworkState(context)) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;

                    List<OfflineTask> taskList = dbUtils.getAllOfflineTask();
                    for (final OfflineTask task : taskList) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {

                                DBUtils myDBUtils = DBUtils.SINGLE_INSTANCE;

                                if (task.getOperationCount() > 5) {
                                    myDBUtils.deleteTask(task.getId());
                                } else {
                                    myDBUtils.modifyOperationCount(task.getOperationCount() + 1, task.getId());
                                }
                                try {
                                    switch (task.getHttpType()) {
                                        case POST:
                                            FNAS.PostRemoteCall(task.getRequest(), task.getData());
                                            break;
                                        case PATCH:
                                            FNAS.PatchRemoteCall(task.getRequest(), task.getData());
                                            break;
                                        default:
                                    }

                                    myDBUtils.deleteTask(task.getId());

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                            }
                        };
                        dbUtils.doOneTaskInCachedThread(runnable);
                    }
                }
            }).start();
        }
    }

    // 发现设备
   /* public static void FindBonjour() {
        try {

            // Activate these lines to see log messages of JmDNS
            final JmDNS jmdns = JmDNS.create();
            String type = "_http._tcp.local.";
            jmdns.addServiceTypeListener(new ServiceTypeListener() {
                @Override
                public void subTypeForServiceTypeAdded(ServiceEvent event) {
                    Log.d("bonjour", " subTypeForServiceTypeAdded: " + event.getInfo());
                }

                @Override
                public void serviceTypeAdded(ServiceEvent event) {
                    Log.d("bonjour", " serviceTypeAdded: " + event + " ");

                }

            });//事实证明，执行到这个方法时会去回调（该监听的相关方法）serviceAdded、serviceRemove、serviceresolved
            jmdns.addServiceListener(type, new ServiceListener() {
                @Override
                public void serviceAdded(ServiceEvent event) {
                    Log.d("bonjour", "Service added   : " + event.getName() + "." + event.getType());
                    // Log.d("winsun", "Service added   : " + event.getInfo().getServer() + ":"+event);
                    *//*
                    try {
                        final HostInfo hostInfo = HostInfo.newHostInfo(InetAddress.getByName(event.getName()+"."+event.getType()), new JmDNSImpl(null, null), null);
                        Log.d("winsun", "MDNS hostname (Bonjour): " + hostInfo.getName());
                        Log.d("winsun", "DNS hostname: " + hostInfo.getInetAddress().getHostName());
                        Log.d("winsun", "IP address: " + hostInfo.getInetAddress().getHostAddress());
                    } catch (Exception e) { e.printStackTrace();}*//*
                }

                @Override
                public void serviceResolved(ServiceEvent event) {//什么时候监听接口回调该方法
                    Log.d("bonjour", "Service resolved: " + event.getInfo());
                }

                //移除某一个服�?
                @Override
                public void serviceRemoved(ServiceEvent event) {
                    Log.d("bonjour", "Service removed : " + event.getName() + "." + event.getType());
                }
            });//执行到这个方法的时候，会去回调（该监听的相关方法）serviceTypeAdded、subTypeForServiceTypeAdded

            Log.i("bonjour", "end find bonjour");
            jmdns.close();
//            Thread.sleep(100000);
            *//*
            int b;
            while ((b = System.in.read()) != -1 && (char) b != 'q') {

            }
            jmdns.close();
            System.out.println("Done");
            *//*
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

}
