package com.winsun.fruitmix.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64OutputStream;
import android.util.Log;

import com.winsun.fruitmix.R;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/4/29.
 */
public class Util {

    //add by liang.wu
    public static final String SHOW_ALBUM_TIPS = "show_album_tips";
    public static final String SHOW_PHOTO_RETURN_TIPS = "show_photo_return_tips";
    public static final String EQUIPMENT_GROUP_NAME = "equipment_group_name";
    public static final String EQUIPMENT_CHILD_NAME = "equipment_child_name";
    public static final String JWT = "jwt";
    public static final String GATEWAY = "gateway";
    public static final String USER_UUID = "user_uuid";
    public static final String MEDIASHARE_UUID = "mediashare_uuid";
    public static final String PASSWORD = "password";
    public static final String EDIT_PHOTO = "edit_photo";
    public static final int ADD_ALBUM = 101;
    public static final String UPDATED_ALBUM_TITLE = "updated_album_title";
    public static final String NEW_ALBUM_CONTENT = "new_album_content";
    public static final String LOCAL_COMMENT_MAP = "local_comment_map";
    public static final String LOCAL_SHARE_CHANGED = "local_share_changed";
    public static final String LOCAL_COMMENT_CHANGED = "local_comment_changed";
    public static final String NEED_SHOW_MENU = "need_show_menu";
    public static final String KEY_LOCAL_PHOTO_UPLOAD_SUCCESS = "key_local_photo_upload_success";

    public static Context APPLICATION_CONTEXT = null;

    public static boolean loginState = false;

    public static int Dp2Px(float dpValue) {
        final float scale = LocalCache.CurrentApp.getBaseContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int Px2Dp(float pxValue) {
        final float scale = LocalCache.CurrentApp.getBaseContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static String CalcSHA256OfFile(String fname) {
        MessageDigest md;
        FileInputStream fin;
        byte[] buffer;
        byte[] digest;
        String digits = "0123456789abcdef";
        int len, i;
        String st;

        try {
            buffer = new byte[15000];
            md = MessageDigest.getInstance("SHA-256");
            fin = new FileInputStream(fname);
            len = 0;
            while ((len = fin.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fin.close();
            digest = md.digest();
            st = "";
            for (i = 0; i < digest.length; i++) {
                st += digits.charAt((digest[i] >> 4) & 0xf);
                st += digits.charAt(digest[i] & 0xf);
            }
            return st;
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean getNetworkState(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED && loginState) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    public static String FormatDateDiff(String date) {
        /*
        long diff;
        diff=new Date().getTime()-new DateFormat.getTime();
        Log.e("winsun", "ww "+diff);
        try {
            Thread.sleep(1000000);
        }catch (Exception e) {}
        */
        return "";
    }

    public static String createLocalUUid() {
        return UUID.randomUUID().toString();
    }

    /**
     * format time
     *
     * @param createTime milliseconds
     * @return formated time
     */
    public static String formatTime(Context context, long createTime) {

        StringBuilder builder = new StringBuilder();

        long currentTime = System.currentTimeMillis();

        long timeDifference = currentTime - createTime;
        if (timeDifference < 0) {
            builder.append(String.format(context.getString(R.string.seconds_ago), 0));
        } else if (timeDifference < 60 * 1000) {
            builder.append(String.format(context.getString(R.string.seconds_ago), timeDifference / 1000));
        } else if (timeDifference < 3600 * 1000) {
            builder.append(String.format(context.getString(R.string.minutes_ago), timeDifference / 60 / 1000));
        } else if (timeDifference < 24 * 3600 * 1000) {
            builder.append(String.format(context.getString(R.string.hours_ago), timeDifference / 3600 / 1000));
        } else if (timeDifference < 48 * 3600 * 1000) {
            builder.append(context.getString(R.string.yesterday));
            builder.append(new SimpleDateFormat("HH:mm", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));
        } else if (timeDifference < 4 * 24 * 3600 * 1000) {

            builder.append(new SimpleDateFormat("E", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));


        } else if (4 * 24 * 3600 * 1000 < timeDifference) {

            String createYear = new SimpleDateFormat("yyyy", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime));
            String currentYear = new SimpleDateFormat("yyyy", Locale.SIMPLIFIED_CHINESE).format(new Date(currentTime));

            String currentMonth = new SimpleDateFormat("MM", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime));
            if (createYear.equals(currentYear)) {

                if (currentMonth.startsWith("0")) {
                    builder.append(new SimpleDateFormat("M月dd日", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));
                } else {
                    builder.append(new SimpleDateFormat("MM月dd日", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));
                }

            } else {

                if (currentMonth.startsWith("0")) {
                    builder.append(new SimpleDateFormat("yyyy年M月dd日", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));
                } else {
                    builder.append(new SimpleDateFormat("yyyy年MM月dd日", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));
                }

            }

        }

        return builder.toString();
    }

}
