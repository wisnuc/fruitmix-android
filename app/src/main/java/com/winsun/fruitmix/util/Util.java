package com.winsun.fruitmix.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.transition.ArcMotion;
import android.transition.PatternPathMotion;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.LoginType;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Administrator on 2016/4/29.
 */
public class Util {

    public static final String TAG = Util.class.getSimpleName();

    public static final String SHOW_ALBUM_TIPS = "show_album_tips";
    public static final String SHOW_PHOTO_RETURN_TIPS = "show_photo_return_tips";
    public static final String USER_GROUP_NAME = "user_group_name";
    public static final String USER_NAME = "user_child_name";
    public static final String USER_BG_COLOR = "user_bg_color";
    public static final String USER_IS_ADMIN = "user_is_admin";
    public static final String USER_HOME = "user_home";
    public static final String JWT = "jwt";
    public static final String GATEWAY = "gateway";
    public static final String USER_UUID = "user_uuid";
    public static final String MEDIASHARE_UUID = "mediashare_uuid";
    public static final String EDIT_PHOTO = "edit_photo";
    public static final String UPDATED_ALBUM_TITLE = "updated_album_title";
    public static final String IMAGE_KEY = "image_key";

    public static final String CURRENT_UPLOAD_DEVICE_ID = "current_upload_device_id";
    public static final String AUTO_UPLOAD_OR_NOT = "auto_upload_or_not";

    public static final String REMOTE_USER_CREATED = "remote_user_created";

    public static final String LOCAL_MEDIA_SHARE_CREATED = "local_share_created";
    public static final String LOCAL_MEDIA_SHARE_MODIFIED = "local_share_modified";
    public static final String LOCAL_MEDIA_SHARE_DELETED = "local_share_deleted";

    public static final String REMOTE_MEDIA_SHARE_CREATED = "remote_share_created";
    public static final String REMOTE_MEDIA_SHARE_MODIFIED = "remote_share_modified";
    public static final String REMOTE_MEDIA_SHARE_DELETED = "remote_share_deleted";

    public static final String DOWNLOADED_FILE_DELETED = "downloaded_file_deleted";

    public static final String SHARED_PHOTO_THUMB_RETRIEVED = "shared_photo_thumb_retrieved";

    public static final String LOCAL_COMMENT_CREATED = "local_comment_created";
    public static final String REMOTE_COMMENT_CREATED = "remote_comment_created";
    public static final String LOCAL_COMMENT_DELETED = "local_comment_deleted";

    public static final String NEW_LOCAL_MEDIA_IN_CAMERA_RETRIEVED = "new_local_media_in_camera_retrieved";

    public static final String CALC_NEW_LOCAL_MEDIA_DIGEST_FINISHED = "calc_new_local_media_digest_finished";

    public static final String PHOTO_IN_REMOTE_MEDIASHARE_MODIFIED = "photo_in_remote_media_share_modified";
    public static final String PHOTO_IN_LOCAL_MEDIASHARE_MODIFIED = "photo_in_local_media_share_modified";

    public static final String LOCAL_MEDIA_COMMENT_RETRIEVED = "local_media_comment_retrieved";
    public static final String REMOTE_MEDIA_COMMENT_RETRIEVED = "remote_media_comment_retrieved";
    public static final String REMOTE_MEDIA_SHARE_RETRIEVED = "remote_media_share_retrieved";
    public static final String LOCAL_MEDIA_SHARE_RETRIEVED = "local_media_share_retrieved";
    public static final String LOCAL_MEDIA_RETRIEVED = "local_media_retrieved";
    public static final String REMOTE_MEDIA_RETRIEVED = "remote_media_retrieved";
    public static final String REMOTE_USER_RETRIEVED = "remote_user_retrieved";
    public static final String REMOTE_TOKEN_RETRIEVED = "remote_token_retrieved";
    public static final String REMOTE_DEVICEID_RETRIEVED = "remote_deviceid_retrieved";
    public static final String REMOTE_FILE_RETRIEVED = "remote_file_retrieved";
    public static final String REMOTE_FILE_SHARE_RETRIEVED = "remote_file_share_retrieved";

    public static final String REFRESH_VIEW_AFTER_DATA_RETRIEVED = "refresh_view_after_data_retrieved";

    public static final String DOWNLOADED_FILE_RETRIEVED = "downloaded_file_retrieved";

    public static final String LOCAL_PHOTO_UPLOAD_STATE_CHANGED = "local_photo_upload_state_changed";

    public static final String NEED_SHOW_MENU = "need_show_menu";
    public static final String KEY_SHOW_COMMENT_BTN = "key_show_comment_btn";
    public static final String KEY_AUTHORIZATION = "Authorization";
    public static final String KEY_JWT_HEAD = "JWT ";
    public static final String KEY_BASE_HEAD = "Basic ";

    public static final String ADD = "add";
    public static final String DELETE = "delete";

    public static final int KEY_MODIFY_ALBUM_REQUEST_CODE = 100;
    public static final int KEY_EDIT_PHOTO_REQUEST_CODE = 101;
    public static final int KEY_CHOOSE_PHOTO_REQUEST_CODE = 102;
    public static final int KEY_LOGIN_REQUEST_CODE = 103;
    public static final int KEY_CREATE_ALBUM_REQUEST_CODE = 104;
    public static final int KEY_ALBUM_CONTENT_REQUEST_CODE = 105;
    public static final int KEY_CREATE_SHARE_REQUEST_CODE = 106;
    public static final int KEY_MANUAL_INPUT_IP_REQUEST_CODE = 107;
    public static final int KEY_CREATE_USER_REQUEST_CODE = 108;

    public static final String HTTP = "http://";
    public static final String MEDIASHARE_PARAMETER = "/mediashare";
    public static final String MEDIA_PARAMETER = "/media";
    public static final String USER_PARAMETER = "/users";
    public static final String TOKEN_PARAMETER = "/token";
    public static final String LOGIN_PARAMETER = "/login";
    public static final String DEVICE_ID_PARAMETER = "/libraries";
    public static final String FILE_PARAMETER = "/files";
    public static final String FILE_SHARE_PARAMETER = "/share";

    public static final String FILE_SHARED_WITH_ME_PARAMETER = "/sharedWithMe";
    public static final String FILE_SHARED_WITH_OTHERS_PARAMETER = "/sharedWithOthers";

    public static final String FRUITMIX_SHAREDPREFERENCE_NAME = "fruitMix";

    public static final String DEVICE_ID_MAP_NAME = "deviceID";

    public static final String HTTP_GET_METHOD = "GET";
    public static final String HTTP_POST_METHOD = "POST";
    public static final String HTTP_PATCH_METHOD = "PATCH";
    public static final String HTTP_DELETE_METHOD = "DELETE";
    public static final int HTTP_CONNECT_TIMEOUT = 30 * 1000;

    public static final String INITIAL_PHOTO_POSITION = "initial_photo_position";
    public static final String CURRENT_PHOTO_POSITION = "current_photo_position";
    public static final String CURRENT_MEDIA_KEY = "current_media_key";
    public static final String CURRENT_MEDIASHARE_TIME = "current_mediashare_time";

    public static final String ALBUM_SWITCH_SHARE_STATE_UMENG_EVENT_ID = "album_switch_share_state";
    public static final String ALBUM_SWITCH_UN_SHARE_STATE_UMENG_EVENT_ID = "album_switch_un_share_state";
    public static final String CRETAE_ALUBM_UMENG_EVENT_ID = "create_album";
    public static final String CREATE_MEDIA_SHARE_UMENG_EVENT_ID = "create_media_share";
    public static final String DELETE_ALBUM_UMENG_EVENT_ID = "delete_album";
    public static final String SWITCH_ALBUM_MODULE_UMENG_EVENT_ID = "switch_album_module";
    public static final String SWITCH_MEDIA_SHARE_MODULE_UMENG_EVENT_ID = "switch_media_share_module";
    public static final String SWITCH_MEDIA_MODULE_UMENG_EVENT_ID = "switch_media_module";
    public static final String SWITCH_ORIGINAL_MEDIA_UMENG_EVENT_ID = "switch_original_media";

    public static final String DEFAULT_DATE = "1916-01-01";

    public static final String KEY_MEDIA_SHARE_UUID = "key_media_share_uuid";

    public static final String KEY_ALREADY_SELECTED_IMAGE_UUID_ARRAYLIST = "key_already_selected_image_uuid_arraylist";

    public static final String KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB = "key_transition_photo_need_show_thumb";

    public static final String KEY_NEED_TRANSITION = "key_need_transition";

    public static final String KEY_SHOW_SOFT_INPUT_WHEN_ENTER = "key_show_soft_input_when_enter";

    public static final String KEY_MANUAL_INPUT_IP = "key_manual_input_ip";

    public static final String KEY_SHOULD_STOP_SERVICE = "key_should_stop_service";

    public static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    public static final String NEED_SHOW_AUTO_UPLOAD_DIALOG = "need_show_auto_upload_dialog";

    public static boolean startTimingRetrieveMediaShare = false;

    public static final long DEFAULT_REFRESH_MEDIA_SHARE_DELAY_TIME = 20 * 1000;

    public static long refreshMediaShareDelayTime = DEFAULT_REFRESH_MEDIA_SHARE_DELAY_TIME;

    public static final long MAX_REFRESH_MEDIA_SHARE_DELAY_TIME = 60 * 1000;

    public static final int MAX_PHOTO_SIZE = 1000;

    public static LoginType loginType = LoginType.LOGIN;

    private static boolean localMediaInCameraLoaded = false;

    private static boolean localMediaInDBLoaded = false;

    private static boolean remoteMediaLoaded = false;

    private static boolean remoteMediaShareLoaded = false;

    public static boolean isRemoteMediaLoaded() {
        return remoteMediaLoaded;
    }

    public static void setRemoteMediaLoaded(boolean remoteMediaLoaded) {
        Util.remoteMediaLoaded = remoteMediaLoaded;
    }

    public static boolean isLocalMediaInCameraLoaded() {
        return localMediaInCameraLoaded;
    }

    public static void setLocalMediaInCameraLoaded(boolean localMediaInCameraLoaded) {
        Util.localMediaInCameraLoaded = localMediaInCameraLoaded;
    }

    public static boolean isLocalMediaInDBLoaded() {
        return localMediaInDBLoaded;
    }

    public static void setLocalMediaInDBLoaded(boolean localMediaInDBLoaded) {
        Util.localMediaInDBLoaded = localMediaInDBLoaded;
    }

    public static boolean isRemoteMediaShareLoaded() {
        return remoteMediaShareLoaded;
    }

    public static void setRemoteMediaShareLoaded(boolean remoteMediaShareLoaded) {
        Util.remoteMediaShareLoaded = remoteMediaShareLoaded;
    }

    /**
     * 将dp转化为px
     */
    public static int dip2px(Context context, float dip) {
        float v = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
        return (int) (v + 0.5f);
    }

    public static int calcScreenWidth(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        return metric.widthPixels;
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
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
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
            builder.append(context.getString(R.string.android_yesterday));
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
        InputMethodManager methodManager = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            methodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void showSoftInput(Activity activity, View view) {
        InputMethodManager methodManager = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        methodManager.showSoftInput(view, 0);
    }

    public static int[] formatPhotoWidthHeight(int width, int height) {
//        if (width >= height) {
//            width = width * 200 / height;
//            height = 200;
//        } else {
//            height = height * 200 / width;
//            width = 200;
//        }

        int actualWidth = 200;
        int actualHeight = 200;

        return new int[]{actualWidth, actualHeight};
    }

    public static boolean checkRunningOnLollipopOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static String removeWrap(String str) {
        return str.replaceAll("\r|\n", "");
    }


    public static String getVersionName(Context context) {

        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);

            return pi.versionName;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getUserNameFirstLetter(String userName) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] splitStrings = userName.split(" ");
        for (String splitString : splitStrings) {
            stringBuilder.append(splitString.substring(0, 1).toUpperCase());
        }
        return stringBuilder.toString();
    }

    public static void hideSystemUI(View view) {

        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

    public static void showSystemUI(View view) {

        // This snippet shows the system bars. It does this by removing all the flags
        // except for the ones that make the content appear under the system bars.
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

    }

    public static Pair<View, String>[] createSafeTransitionPairs(Activity activity, boolean includeBottomNavigationView, Pair... otherPairs) {

        // Avoid system UI glitches as described here:
        // https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
        View decor = activity.getWindow().getDecorView();

        List<Pair> pairs = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View statusBar = decor.findViewById(android.R.id.statusBarBackground);
            View navBar = decor.findViewById(android.R.id.navigationBarBackground);

            Pair statusBarPair = new Pair<>(statusBar, ViewCompat.getTransitionName(statusBar));
            Pair navBarPair = new Pair<>(navBar, ViewCompat.getTransitionName(navBar));

            pairs.add(statusBarPair);
            pairs.add(navBarPair);
        }

        Pair toolbarPair = new Pair<>(decor.findViewById(R.id.toolbar), activity.getString(R.string.transition_toolbar));
        pairs.add(toolbarPair);

        if (includeBottomNavigationView) {
            Pair bottomNavigationViewPair = new Pair<>(decor.findViewById(R.id.bottom_navigation_view),
                    activity.getString(R.string.transition_bottom_navigation_view));

            pairs.add(bottomNavigationViewPair);
        }

        // only add transition participants if there's at least one none-null element
        if (otherPairs != null && !(otherPairs.length == 1
                && otherPairs[0] == null)) {
            pairs.addAll(Arrays.asList(otherPairs));
        }

        return pairs.toArray(new Pair[pairs.size()]);
    }

    public static void setMotion(int position, int spanCount) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if ((position) % spanCount == 0) {

                EnterPatternPathMotion.setMotion(true);

                ReturnPatternPathMotion.setMotion(true);

            } else if ((position + 1) % spanCount == 0) {

                EnterPatternPathMotion.setMotion(false);

                ReturnPatternPathMotion.setMotion(false);

            } else {

                EnterPatternPathMotion.setMotion(true);

                ReturnPatternPathMotion.setMotion(true);
            }

        }

    }

    public static MediaShare generateMediaShare(boolean isAlbum, boolean isPublic, boolean otherMaintainer, String title, String desc, List<String> mediaKeys) {

        MediaShare mediaShare = new MediaShare();
        mediaShare.setUuid(Util.createLocalUUid());

        Log.i(TAG, "create album digest:" + mediaKeys);

        List<MediaShareContent> mediaShareContents = new ArrayList<>();

        for (String mediaKey : mediaKeys) {
            MediaShareContent mediaShareContent = new MediaShareContent();
            mediaShareContent.setMediaUUID(mediaKey);
            mediaShareContent.setAuthor(FNAS.userUUID);
            mediaShareContent.setTime(String.valueOf(System.currentTimeMillis()));
            mediaShareContents.add(mediaShareContent);

        }

        mediaShare.initMediaShareContents(mediaShareContents);

        mediaShare.setCoverImageUUID(mediaKeys.get(0));

        mediaShare.setTitle(title);
        mediaShare.setDesc(desc);

        if (isPublic) {
            for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                mediaShare.addViewer(userUUID);
            }
        } else mediaShare.clearViewers();

        if (otherMaintainer) {
            for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                mediaShare.addMaintainer(userUUID);
            }
        } else {
            mediaShare.clearMaintainers();
            mediaShare.addMaintainer(FNAS.userUUID);
        }

        mediaShare.setCreatorUUID(FNAS.userUUID);
        mediaShare.setTime(String.valueOf(System.currentTimeMillis()));
        mediaShare.setAlbum(isAlbum);
        mediaShare.setLocal(true);
        mediaShare.setArchived(false);
        mediaShare.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(Long.parseLong(mediaShare.getTime()))));

        return mediaShare;

    }

    public static void sendShare(Context context,List<String> selectMediaOriginalPhotoPaths) {
        ArrayList<Uri> uris = new ArrayList<>();

        for (String originalPhotoPath : selectMediaOriginalPhotoPaths) {
            Uri uri = Uri.fromFile(new File(originalPhotoPath));
            uris.add(uri);
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.setType("image/*");
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_text)));

    }


}
