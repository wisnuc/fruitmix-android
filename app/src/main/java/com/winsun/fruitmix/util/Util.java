package com.winsun.fruitmix.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.github.druk.rxdnssd.BonjourService;
import com.szysky.customize.siv.SImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.GravityArcMotion;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.user.DefaultCommentUser;
import com.winsun.fruitmix.user.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/4/29.
 */
public class Util {

    public static final String TAG = Util.class.getSimpleName();

    public static final String CUSTOM_ERROR_CODE_HEAD = "8";

    public static final String CUSTOM_ERROR_CODE_UPLOAD_MEDIA = "01";
    public static final String CUSTOM_ERROR_CODE_GET_UPLOADED_MEDIA = "02";
    public static final String CUSTOM_ERROR_CODE_GET_ALL_MEDIA_I_CAN_VIEW = "03";
    public static final String CUSTOM_ERROR_CODE_CREATE_FOLDER = "04";
    public static final String CUSTOM_ERROR_CODE_GET_FOLDER = "05";

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

    public static final String SHA_256_STRING = "sha256";
    public static final String SIZE_STRING = "size";
    public static final String MANIFEST_STRING = "manifest";

    public static final String AUTO_UPLOAD_OR_NOT = "auto_upload_or_not";

    public static final String REMOTE_USER_CREATED = "remote_user_created";

    public static final String DOWNLOADED_FILE_DELETED = "downloaded_file_deleted";

    public static final String SHARED_PHOTO_THUMB_RETRIEVED = "shared_photo_thumb_retrieved";

    public static final String NEW_LOCAL_MEDIA_IN_CAMERA_RETRIEVED = "new_local_media_in_camera_retrieved";

    public static final String CALC_NEW_LOCAL_MEDIA_DIGEST_FINISHED = "calc_new_local_media_digest_finished";

    public static final String GET_NEW_COMMENT_FINISHED = "get_new_comment_finished";

    public static final String LOCAL_MEDIA_RETRIEVED = "local_media_retrieved";
    public static final String REMOTE_MEDIA_RETRIEVED = "remote_media_retrieved";
    public static final String REMOTE_USER_RETRIEVED = "remote_user_retrieved";
    public static final String REMOTE_TOKEN_RETRIEVED = "remote_token_retrieved";
    public static final String REMOTE_DEVICE_ID_RETRIEVED = "remote_device_id_retrieved";
    public static final String REMOTE_FILE_RETRIEVED = "remote_file_retrieved";
    public static final String REMOTE_FILE_SHARE_RETRIEVED = "remote_file_share_retrieved";

    public static final String REMOTE_CONFIRM_INVITE_USER_RETRIEVED = "remote_confirm_invite_user_retrieved";

    public static final String LOCAL_VIDEO_THUMBNAIL_RETRIEVED = "local_video_thumbnail_retrieved";

    public static final String REFRESH_VIEW_AFTER_DATA_RETRIEVED = "refresh_view_after_data_retrieved";

    public static final String DOWNLOADED_FILE_RETRIEVED = "downloaded_file_retrieved";

    public static final String LOGIN_STATE_CHANGED = "login_state_changed";

    public static final String SET_CURRENT_LOGIN_USER_AFTER_LOGIN = "set_current_login_user_after_login";

    public static final String RECOMMEND_ALBUM_CREATED = "recommend_album_created";

    public static final String TOKEN_INVALID = "token_invalid";

    public static final String NETWORK_CHANGED = "network_changed";

    public static final String ONLY_UPLOAD_OR_DOWNLOAD_WITH_WIFI_SETTING_CHANGED = "only_upload_or_download_with_wifi_setting_changed";

    public static final String KEY_STOP_CURRENT_ACTIVITY = "key_stop_current_activity";

    public static final String KEY_SHOW_COMMENT_BTN = "key_show_comment_btn";
    public static final String KEY_AUTHORIZATION = "Authorization";
    public static final String KEY_JWT_HEAD = "JWT ";
    public static final String KEY_BASE_HEAD = "Basic ";

    public static final String KEY_GROUP_UUID = "key_group_uuid";

    public static final String ADD = "add";
    public static final String DELETE = "delete";

    public static final int KEY_CHOOSE_PHOTO_REQUEST_CODE = 102;
    public static final int KEY_LOGIN_REQUEST_CODE = 103;

    public static final int KEY_MANUAL_INPUT_IP_REQUEST_CODE = 107;
    public static final int KEY_CREATE_USER_REQUEST_CODE = 108;

    public static final String HTTP = "http://";
    public static final String MEDIA_PARAMETER = "/media";
    public static final String ADMIN_USER_PARAMETER = "/admin/users";
    public static final String ACCOUNT_PARAMETER = "/account";
    public static final String TOKEN_PARAMETER = "/token";
    public static final String USERS_PARAMETER = "/users";
    public static final String DEVICE_ID_PARAMETER = "/libraries";
    public static final String LIST_FILE_PARAMETER = "/files/fruitmix/list";
    public static final String DOWNLOAD_FILE_PARAMETER = "/files/fruitmix/download";
    public static final String FILE_SHARE_PARAMETER = "/share";
    public static final String TICKETS_PARAMETER = "/station/tickets";

    public static final String FILE_SHARED_WITH_ME_PARAMETER = "/sharedWithMe";
    public static final String FILE_SHARED_WITH_OTHERS_PARAMETER = "/sharedWithOthers";

    public static final String FRUITMIX_SHAREDPREFERENCE_NAME = "fruitMix";

    public static final String DEVICE_ID_MAP_NAME = "deviceID";

    public static final String HTTP_GET_METHOD = "GET";
    public static final String HTTP_POST_METHOD = "POST";
    public static final String HTTP_PATCH_METHOD = "PATCH";
    public static final String HTTP_DELETE_METHOD = "DELETE";
    public static final String HTTP_PUT_METHOD = "PUT";
    public static final int HTTP_CONNECT_TIMEOUT = 30 * 1000;

    public static final int HTTP_READ_TIMEOUT = 30 * 1000;
    public static final int HTTP_WRITE_TIMEOUT = 60 * 1000;

    public static final String INITIAL_PHOTO_POSITION = "initial_photo_position";
    public static final String CURRENT_PHOTO_POSITION = "current_photo_position";
    public static final String CURRENT_MEDIA_KEY = "current_media_key";
    public static final String CURRENT_MEDIASHARE_TIME = "current_mediashare_time";

    public static final String SWITCH_INBOX_MODULE_UMENG_EVENT_ID = "switch_inbox_module";
    public static final String SWITCH_FILE_MODULE_UMENG_EVENT_ID = "switch_file_module";
    public static final String SWITCH_GROUP_MODULE_UMENG_EVENT_ID = "switch_group_module";
    public static final String SWITCH_MEDIA_MODULE_UMENG_EVENT_ID = "switch_media_module";
    public static final String SWITCH_ORIGINAL_MEDIA_UMENG_EVENT_ID = "switch_original_media";

    public static final String DEFAULT_DATE = "1916-01-01";

    public static final String KEY_ALREADY_SELECTED_IMAGE_KEY_ARRAYLIST = "key_already_selected_image_key_arraylist";

    public static final String KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB = "key_transition_photo_need_show_thumb";

    public static final String KEY_NEED_TRANSITION = "key_need_transition";

    public static final String KEY_SHOW_SOFT_INPUT_WHEN_ENTER = "key_show_soft_input_when_enter";

    public static final String KEY_MANUAL_INPUT_IP = "key_manual_input_ip";

    public static final String KEY_SHOULD_STOP_SERVICE = "key_should_stop_service";

    public static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    public static final String NEED_SHOW_AUTO_UPLOAD_DIALOG = "need_show_auto_upload_dialog";

    public static final long DEFAULT_REFRESH_TICKET_DELAY_TIME = 20 * 1000;

    public static long refreshTicketsDelayTime = DEFAULT_REFRESH_TICKET_DELAY_TIME;

    public static final long MAX_REFRESH_MEDIA_SHARE_DELAY_TIME = 60 * 1000;

    public static final int MAX_PHOTO_SIZE = 1000;

    public static LoginType loginType = LoginType.LOGIN;

    private static boolean localMediaInCameraLoaded = false;

    private static boolean localMediaInDBLoaded = false;

    private static boolean remoteMediaLoaded = false;

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

    public static boolean clearFileDownloadItem = false;


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

    public static int calcScreenHeight(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        return metric.heightPixels;
    }

    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

        return metric;
    }


    public static String calcSHA256OfFile(String filePath) {
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
            fin = new FileInputStream(filePath);
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

    public static boolean isNetworkConnected(Context context) {
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

    public static String formatShareTime(Context context, long shareTimeMillis, long currentTimeMillis) {

        Calendar shareCalendar = Calendar.getInstance();

        shareCalendar.setTimeInMillis(shareTimeMillis);

        Calendar currentCalendar = Calendar.getInstance();

        currentCalendar.setTimeInMillis(currentTimeMillis);

        int currentYear = currentCalendar.get(Calendar.YEAR);
        int shareYear = shareCalendar.get(Calendar.YEAR);

        int shareMonth = shareCalendar.get(Calendar.MONTH) + 1;
        int shareDay = shareCalendar.get(Calendar.DAY_OF_MONTH);

        if (currentYear != shareYear) {
            return context.getString(R.string.year_month_day, shareYear, shareMonth, shareDay);
        }

        int currentMonth = currentCalendar.get(Calendar.MONTH) + 1;
        int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

        if (currentMonth != shareMonth || currentDay - shareDay > 1) {
            return context.getString(R.string.month_day, shareMonth, shareDay);
        }

        int shareHour = shareCalendar.get(Calendar.HOUR_OF_DAY);
        int shareMin = shareCalendar.get(Calendar.MINUTE);

        if (currentDay - shareDay == 1) {
            return context.getString(R.string.yesterday_hour_min, String.valueOf(shareHour), Util.formatHourMinSec(shareMin));
        }

        int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);

        int diffHour = currentHour - shareHour;

        if (diffHour >= 5) {
            return context.getString(R.string.hour_min, String.valueOf(shareHour), Util.formatHourMinSec(shareMin));
        } else if (diffHour < 5 && diffHour > 1) {

            String hourAgo = context.getResources().getQuantityString(R.plurals.hour, diffHour, diffHour);

            return context.getString(R.string.time_ago, hourAgo);
        } else {

            int currentMin = currentCalendar.get(Calendar.MINUTE);

            int diffMin = currentMin - shareMin;
            if (diffMin < 2)
                diffMin = 1;

            String minAgo = context.getResources().getQuantityString(R.plurals.minute, diffMin, diffMin);

            return context.getString(R.string.time_ago, minAgo);
        }


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
        if (activity.getCurrentFocus() != null && methodManager != null) {
            methodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void showSoftInput(Activity activity, View view) {
        InputMethodManager methodManager = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (methodManager != null)
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

    public static String getUserNameForAvatar(String userName) {

        if (userName.isEmpty())
            return userName;

        if (userName.contains(" ")) {

            String[] splitStrings = userName.split(" ");

            String firstStr = splitStrings[0].length() > 0 ? splitStrings[0].substring(0, 1).toUpperCase() : "";

            if (isChinese(firstStr))
                return firstStr;
            else {

                int position = splitStrings.length - 1;

                String lastStr = splitStrings[position].length() > 0 ? splitStrings[position].substring(0, 1).toUpperCase() : "";

                if (isChinese(lastStr))
                    return firstStr;
                else
                    return firstStr + lastStr;

            }

        } else {

            String firstStr = userName.substring(0, 1).toUpperCase();

            if (isChinese(firstStr))
                return firstStr;
            else {

                String lastStr = userName.length() > 1 ? userName.substring(1, 2).toUpperCase() : "";

                if (isChinese(lastStr))
                    return firstStr;
                else
                    return firstStr + lastStr;

            }

        }


    }

    private static boolean isChinese(String param) {

        String regEx = "[\\u4e00-\\u9fa5]";

        Pattern pattern = Pattern.compile(regEx);

        Matcher matcher = pattern.matcher(param);

        return matcher.matches();

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

    public static Pair<View, String>[] createSafeTransitionPairs(View toolbar, Activity activity, boolean includeBottomNavigationView, Pair... otherPairs) {

        // Avoid system UI glitches as described here:
        // https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
        View decor = activity.getWindow().getDecorView();

        List<Pair> pairs = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View statusBar = decor.findViewById(android.R.id.statusBarBackground);
            View navBar = decor.findViewById(android.R.id.navigationBarBackground);

            if (statusBar != null) {
                Pair statusBarPair = new Pair<>(statusBar, ViewCompat.getTransitionName(statusBar));
                pairs.add(statusBarPair);
            }

            if (navBar != null) {
                Pair navBarPair = new Pair<>(navBar, ViewCompat.getTransitionName(navBar));
                pairs.add(navBarPair);
            }

        }

        if (toolbar != null) {
            Pair toolbarPair = new Pair<>(toolbar, activity.getString(R.string.transition_toolbar));
            pairs.add(toolbarPair);
        }

        if (includeBottomNavigationView) {

            View bottomNavigationView = decor.findViewById(R.id.bottom_navigation_view);

            if (bottomNavigationView != null) {

                Pair bottomNavigationViewPair = new Pair<>(bottomNavigationView,
                        activity.getString(R.string.transition_bottom_navigation_view));

                pairs.add(bottomNavigationViewPair);

            }

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

            GravityArcMotion.setLinePath(true);

/*            int halfSpanCount = spanCount / 2;

            if (spanCount % 2 != 0) {
                GravityArcMotion.setLinePath(position == halfSpanCount);
            }*/

/*            if (spanCount % 2 == 0) {

                if (position < halfSpanCount) {

                    EnterPatternPathMotion.setMotion(true, false);

                    ReturnPatternPathMotion.setMotion(true, false);

                } else if (position >= halfSpanCount) {
                    EnterPatternPathMotion.setMotion(false, true);

                    ReturnPatternPathMotion.setMotion(false, true);
                }

            } else {

                if (position < halfSpanCount) {
                    EnterPatternPathMotion.setMotion(true, false);

                    ReturnPatternPathMotion.setMotion(true, false);
                } else if (position > halfSpanCount) {
                    EnterPatternPathMotion.setMotion(false, true);

                    ReturnPatternPathMotion.setMotion(false, true);

                } else if (position == halfSpanCount) {
                    EnterPatternPathMotion.setMotion(false, false);

                    ReturnPatternPathMotion.setMotion(false, false);
                }


            }*/

        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void dismissViewWithReveal(final View view) {

        // get the center for the clipping circle
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = view.getBottom();

        // get the initial radius for the clipping circle
        int initialRadius = Math.max(view.getWidth(), view.getHeight());

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });

        // start the animation
        anim.setDuration(200);
        anim.start();

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void showViewWithReveal(View view) {

        // get the center for the clipping circle
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = view.getBottom();

        // get the final radius for the clipping circle
        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        view.setVisibility(View.VISIBLE);

        anim.setDuration(200);
        anim.start();

    }

    public static boolean checkBonjourService(BonjourService bonjourService) {

        if (bonjourService.isLost()) return false;

        String hostName = bonjourService.getHostname();

        if (hostName == null || !hostName.contains("wisnuc")) return false;

        Log.d(TAG, "call: hostName: " + hostName);

        return bonjourService.getInet4Address() != null;

    }

    public static void setStatusBarColor(Activity activity, int colorResID) {

        if (checkRunningOnLollipopOrHigher()) {
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, colorResID));
        }

    }

    public static boolean checkIP(String ip) {

        if (ip.contains(HTTP)) {
            ip = ip.substring(7, ip.length());
        }

        Process process = null;

        try {

            process = new ProcessBuilder()
                    .command("/system/bin/ping", "-c 3", "-w 3", ip)
                    .redirectErrorStream(true)
                    .start();

            int status = process.waitFor();

            return status == 0;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (process != null)
                process.destroy();
        }

        return false;
    }

    public static boolean checkIpLegal(String ip) {

        return Patterns.IP_ADDRESS.matcher(ip).matches();

/*        String ipRegularExpression = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";

        Pattern pattern = Pattern.compile(ipRegularExpression);

        Matcher matcher = pattern.matcher(ip);

        boolean result =  matcher.matches();

        if(!result)
            return false;

        String[] ips = ip.split("\\.");

        for (String item:ips){

            long num = Long.valueOf(item);

            if(num > 255)
                return false;

        }

        return true;*/

    }


    public static void startActivity(Context context, Class target) {
        context.startActivity(new Intent(context, target));
    }

    public static void setTopMargin(View view, int top) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            params.topMargin = top;

            view.requestLayout();
        }
    }

    public static void setLeftMargin(View view, int left) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            params.leftMargin = left;

            view.requestLayout();
        }
    }

    public static void setRightMargin(View view, int right) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            params.rightMargin = right;

            view.requestLayout();
        }
    }

    public static void setBottomMargin(View view, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            params.bottomMargin = bottom;

            view.requestLayout();
        }
    }

    public static void setMargin(View view, int left, int top, int right, int bottom) {

        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            params.setMargins(left, top, right, bottom);

            view.requestLayout();
        }

    }

    public static void setHeight(View view, int height) {

        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            params.height = height;

            view.requestLayout();
        }
    }

    public static void setWidthAndHeight(View view, int width, int height) {

        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            params.width = width;
            params.height = height;

            view.requestLayout();
        }
    }

    public static void setMarginAndHeight(View view, int height, int left, int top, int right, int bottom) {

        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            params.height = height;

            params.setMargins(left, top, right, bottom);

            view.requestLayout();
        }

    }

    /**
     * check str is number
     *
     * @param str may be -19162431.1254，if not use BigDecimal,it will become -1.91624311254E7
     * @return true is number,false is not number
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;
        }

        Matcher isNum = pattern.matcher(bigStr);
        return isNum.matches();
    }

    public static String formatDate(long time) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(time));
    }

    public static String formatTime(long time) {
        return new SimpleDateFormat("hh:mm:ss", Locale.CHINA).format(new Date(time));
    }

    public static String getCurrentFormatTime() {

        return formatDateAndTime(System.currentTimeMillis());

    }

    private static String formatDateAndTime(long time) {

        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA).format(new Date(time));

    }

    public static long getFirstNumInStr(String param) {

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(param);

        if (matcher.find())
            return Long.parseLong(matcher.group());
        else
            return 0;
    }

    public static int getBitmapSize(Bitmap bitmap) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {//API 16
            return bitmap.getByteCount();
        }

        return bitmap.getRowBytes() * bitmap.getHeight();

    }

    public static String formatDuration(long durationInMills) {

        long durationInSec = durationInMills / 1000;
        long durationHour = durationInSec / 3600;
        long durationMinute = durationInSec % 3600 / 60;
        long durationSecond = durationInSec % 3600 % 60;

        String hourStr;

        if (durationHour == 0) {

            hourStr = "";

        } else {

            hourStr = String.valueOf(durationHour) + ":";

        }

        return hourStr + durationMinute + ":" + formatHourMinSec(durationSecond);

    }

    private static String formatHourMinSec(long time) {

        return time < 10 ? "0" + time : String.valueOf(time);

    }

    public static int compareVersion(String currentVersion, String newVersion) {

        String[] currentVersionNumbers = currentVersion.split("\\.");
        String[] newVersionNumbers = newVersion.split("\\.");

        int length = currentVersionNumbers.length > newVersionNumbers.length ? newVersionNumbers.length : currentVersionNumbers.length;

        for (int i = 0; i < length; i++) {

            try {

                int number1 = Integer.parseInt(currentVersionNumbers[i]);
                int number2 = Integer.parseInt(newVersionNumbers[i]);

                int result = number1 - number2;

                if (result != 0)
                    return result;

            } catch (NumberFormatException e) {
                e.printStackTrace();

                break;
            }

        }

        return 0;

    }

    public static void fillUserCommentUser(List<User> users, UserComment userComment) {

        User commentUser = userComment.getCreator();

        int size = users.size();

        int position = 0;

        for (; position < size; position++) {

            User user = users.get(position);

            if (commentUser.getAssociatedWeChatGUID().equals(user.getAssociatedWeChatGUID())) {
                commentUser.setUserName(user.getUserName());
                commentUser.setAvatar(user.getAvatar());

                commentUser.setDefaultAvatar(user.getDefaultAvatar());
                commentUser.setDefaultAvatarBgColor(user.getDefaultAvatarBgColor());

                break;
            }

        }

        if (position >= size) {

            User defaultCommentUser = new DefaultCommentUser();
            defaultCommentUser.setAssociatedWeChatGUID(commentUser.getAssociatedWeChatGUID());

            userComment.setCreator(new DefaultCommentUser());

        }

    }

    public static void setUserDefaultAvatar(User user, Random random) {
        if (user.getDefaultAvatar() == null || user.getDefaultAvatar().isEmpty()) {
            user.setDefaultAvatar(Util.getUserNameForAvatar(user.getUserName()));
        }
        if (user.getDefaultAvatarBgColor() == 0) {

            int avatarBgColor = random.nextInt(3) + 1;

            user.setDefaultAvatarBgColor(avatarBgColor);
        }
    }

    public static void fillGroupUserAvatar(PrivateGroup group, SImageView sImageView){

        List<User> users = group.getUsers();

        int size = users.size();

        List<String> avatarUrls = new ArrayList<>(size);

        for (User user : users) {

            avatarUrls.add(user.getAvatar());

        }

        sImageView.setImageUrls(avatarUrls.toArray(new String[]{}));

    }

    public static <T> boolean checkItemIsInTheList(T item, List<T> list, ComparisonRule comparisonRule) {

        int currentCount = 0;
        int totalCount = list.size();

        for (; currentCount < totalCount; currentCount++) {

            T itemInList = list.get(currentCount);

            if (comparisonRule.compare(item, itemInList) == 0)
                break;

        }

        return currentCount < totalCount;

    }

}
