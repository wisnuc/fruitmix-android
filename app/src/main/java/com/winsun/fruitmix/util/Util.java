package com.winsun.fruitmix.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.winsun.fruitmix.R;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/4/29.
 */
public class Util {

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
    public static final String UPDATED_ALBUM_TITLE = "updated_album_title";
    public static final String NEW_ALBUM_CONTENT = "new_album_content";
    public static final String LOCAL_COMMENT_MAP = "local_comment_map";
    public static final String LOCAL_SHARE_CHANGED = "local_share_changed";
    public static final String LOCAL_COMMENT_CHANGED = "local_comment_changed";
    public static final String LOCAL_PHOTO_UPLOAD_STATE_CHANGED = "local_photo_upload_state_changed";
    public static final String REMOTE_PHOTO_LOADED = "remote_photo_loaded";
    public static final String SHARE_LOADED = "share_loaded";
    public static final String NEED_SHOW_MENU = "need_show_menu";
    public static final String KEY_LOCAL_PHOTO_UPLOAD_SUCCESS = "key_local_photo_upload_success";
    public static final String KEY_SHOW_COMMENT_BTN = "key_show_comment_btn";
    public static final String KEY_AUTHORIZATION = "Authorization";
    public static final String KEY_JWT_HEAD = "JWT ";
    public static final String KEY_BASE_HEAD = "Basic ";

    public static final String CREATE_LOCAL_SHARE_FINISH = "create_local_share_finish";

    public static final int KEY_MODIFY_ALBUM_REQUEST_CODE = 100;
    public static final int KEY_EDIT_PHOTO_REQUEST_CODE = 101;
    public static final int KEY_CHOOSE_PHOTO_REQUEST_CODE = 102;
    public static final int KEY_LOGIN_REQUEST_CODE = 103;
    public static final int KEY_CREATE_ALBUM_REQUEST_CODE = 104;
    public static final int KEY_ALBUM_CONTENT_REQUEST_CODE = 105;
    public static final int KEY_CREATE_SHARE_REQUEST_CODE = 106;

    public static final String HTTP = "http://";
    public static final String MEDIASHARE_PARAMETER = "/mediashare";
    public static final String MEDIA_PARAMETER = "/media";
    public static final String USER_PARAMETER = "/users";
    public static final String TOKEN_PARAMETER = "/token";
    public static final String LOGIN_PARAMETER = "/login";

    public static final String FRUITMIX_SHAREDPREFERENCE_NAME = "fruitMix";

    public static final String SHARE_MAP_NAME = "sharesMap";
    public static final String MEDIA_MAP_NAME = "mediasMap";
    public static final String USER_MAP_NAME = "usersMap";
    public static final String LOCAL_IMAGE_MAP_NAME = "localImagesMap";
    public static final String DEVICE_ID_MAP_NAME = "deviceID";

    public static final String HTTP_POST_METHOD = "POST";
    public static final String HTTP_PATCH_METHOD = "PATCH";

    public static final int HTTP_CONNECT_TIMEOUT = 15 * 1000;

    public static final String INITIAL_PHOTO_POSITION = "initial_photo_position";
    public static final String CURRENT_PHOTO_POSITION = "current_photo_position";
    public static final String CURRENT_PHOTO_DATE = "current_photo_date";

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
                if (info.getState() == NetworkInfo.State.CONNECTED && loginState) {
                    return true;
                }
            }
        }
        return false;
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

        Date date = new Date();
        long l = 24 * 60 * 60 & 1000;
        long timeDifference = date.getTime() - date.getTime() % l - 8 * 60 * 60 * 1000 - createTime;

        if (timeDifference < 0) {
            builder.append(new SimpleDateFormat("HH:mm:ss", Locale.SIMPLIFIED_CHINESE).format(new Date(createTime)));

        } else if (timeDifference < 24 * 3600 * 1000) {
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

    public static void hideSoftInput(Activity activity) {
        InputMethodManager methodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            methodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static int[] formatPhotoWidthHeight(int width, int height) {
        if (width >= height) {
            width = width * 200 / height;
            height = 200;
        } else {
            height = height * 200 / width;
            width = 200;
        }

        if (width / height > 2)
            width = 200;
        else if (height / width > 2)
            height = 200;

        return new int[]{width, height};
    }

    public static boolean checkRunningOnLollipopOrHigher(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
