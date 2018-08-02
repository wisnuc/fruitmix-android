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

import com.szysky.customize.siv.SImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.GravityArcMotion;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.user.DefaultCommentUser;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.digestInputStream.DigestFileInputStream;
import com.winsun.fruitmix.util.digestInputStream.DigestInputStream;
import com.winsun.fruitmix.util.digestInputStream.DigestRandomAccessFileInputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

    public static final String SHA_256_STRING = "sha256";
    public static final String SIZE_STRING = "size";
    public static final String MANIFEST_STRING = "manifest";

    public static final String GET_NEW_COMMENT_FINISHED = "get_new_comment_finished";

    public static final String LOCAL_VIDEO_THUMBNAIL_RETRIEVED = "local_video_thumbnail_retrieved";

    public static final String LOGIN_STATE_CHANGED = "login_state_changed";

    public static final String KEY_AUTHORIZATION = "Authorization";
    public static final String KEY_JWT_HEAD = "JWT ";
    public static final String KEY_BASE_HEAD = "Basic ";

    public static final String HTTP = "http://";

    public static final String TOKEN_PARAMETER = "/token";
    public static final String USERS_PARAMETER = "/users";

    public static final String FRUITMIX_SHAREDPREFERENCE_NAME = "fruitMix";

    public static final String HTTP_GET_METHOD = "GET";
    public static final String HTTP_POST_METHOD = "POST";
    public static final String HTTP_PATCH_METHOD = "PATCH";
    public static final String HTTP_DELETE_METHOD = "DELETE";
    public static final String HTTP_PUT_METHOD = "PUT";

    public static final int HTTP_WRITE_TIMEOUT = 60 * 1000;

    public static final String INITIAL_PHOTO_POSITION = "initial_photo_position";
    public static final String CURRENT_PHOTO_POSITION = "current_photo_position";
    public static final String CURRENT_MEDIA_KEY = "current_media_key";
    public static final String CURRENT_MEDIASHARE_TIME = "current_mediashare_time";

    public static final String SWITCH_ORIGINAL_MEDIA_UMENG_EVENT_ID = "switch_original_media";

    public static final String DEFAULT_DATE = "1916-01-01";

    public static final String KEY_TRANSITION_PHOTO_NEED_SHOW_THUMB = "key_transition_photo_need_show_thumb";

    public static final String KEY_NEED_TRANSITION = "key_need_transition";

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

    public static String covertByteToString(byte[] digest) {

        String digits = "0123456789abcdef";
        int i;
        StringBuilder st;

        st = new StringBuilder();
        for (i = 0; i < digest.length; i++) {
            st.append(digits.charAt((digest[i] >> 4) & 0xf));
            st.append(digits.charAt(digest[i] & 0xf));
        }
        return st.toString();
    }

    public static byte[] calcSHA256OfFileReturnByte(String filePath, long startPosition, long offset) {

        byte[] digest = new byte[0];
        MessageDigest md;
        byte[] buffer;
        int len = 0;

        int currentReadCount = 0;

        int bufferSize = 1024 * 1024;

        try {

            DigestInputStream digestInputStream;

            if (startPosition > 0) {

                RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");

                randomAccessFile.seek(startPosition);
                digestInputStream = new DigestRandomAccessFileInputStream(randomAccessFile);
            } else {
                digestInputStream = new DigestFileInputStream(new FileInputStream(filePath));
            }

            buffer = new byte[bufferSize];
            md = MessageDigest.getInstance("SHA-256");
            while (true) {

                if (offset - currentReadCount < bufferSize) {

                    int count = (int) offset - currentReadCount;

                    buffer = new byte[count];

                    len = digestInputStream.read(buffer, 0, count);

                } else
                    len = digestInputStream.read(buffer, 0, bufferSize);

                currentReadCount += len;

                if (len == -1)
                    break;
                else if (currentReadCount < offset) {
                    md.update(buffer, 0, len);
                } else if (currentReadCount == offset) {
                    md.update(buffer, 0, len);
                    break;
                } else
                    break;

            }

            Log.d(TAG, "calcSHA256OfFile: currentReadCount: " + currentReadCount);

            digestInputStream.close();
            digest = md.digest();

            return digest;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return digest;

    }

    public static String calcSHA256OfFile(String filePath) {

        FileInputStream fin;

        try {

            fin = new FileInputStream(filePath);

            return calcSHA256OfFile(new DigestFileInputStream(fin));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return "";

    }

    public static byte[] calcSHA256OfFileReturnByte(byte[] bytes) {

        MessageDigest md;
        byte[] digest = new byte[0];

        try {
            md = MessageDigest.getInstance("SHA-256");

            md.update(bytes);

            digest = md.digest();

            return digest;

        } catch (Exception e) {
            return digest;
        }

    }

    private static String calcSHA256OfFile(DigestInputStream digestInputStream) {
        MessageDigest md;
        byte[] buffer;
        byte[] digest;
        String digits = "0123456789abcdef";
        int len, i;
        StringBuilder st;

        int currentReadCount = 0;

        try {
            buffer = new byte[15000];
            md = MessageDigest.getInstance("SHA-256");

            while ((len = digestInputStream.read(buffer)) != -1) {

                currentReadCount += len;
                md.update(buffer, 0, len);
            }

            Log.d(TAG, "calcSHA256OfFile: currentReadCount: " + currentReadCount);

            digestInputStream.close();
            digest = md.digest();
            st = new StringBuilder();
            for (i = 0; i < digest.length; i++) {
                st.append(digits.charAt((digest[i] >> 4) & 0xf));
                st.append(digits.charAt(digest[i] & 0xf));
            }
            return st.toString();
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


    public static boolean checkRunningOnLollipopOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
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

    public static Pair<View, String>[] createSafeTransitionPairs(View toolbar, Activity activity, Pair... otherPairs) {

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

    }

    public static void startActivity(Context context, Class target) {
        context.startActivity(new Intent(context, target));
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

    public static String formatDateAndTime(long time) {

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

    public static void fillGroupUserAvatar(PrivateGroup group, SImageView sImageView) {

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

    public static boolean checkNameFirstWordIsIllegal(String name) {
        Pattern pattern = Pattern.compile("^[-.]");

        Matcher matcher = pattern.matcher(name);

        return matcher.lookingAt();

    }

    public static boolean checkNameIsIllegal(String name) {

        Pattern pattern = Pattern.compile("[a-zA-Z0-9]+|[!()\\-.?[\\\\]_`~@#\"']+|[\\u4E00-\\u9FFF\\u3400-\\u4dbf\\uf900-\\ufaff\\u3040-\\u309f\\uac00-\\ud7af]");

        Matcher matcher = pattern.matcher(name);

        return matcher.replaceAll("").length() != 0;

    }

}
