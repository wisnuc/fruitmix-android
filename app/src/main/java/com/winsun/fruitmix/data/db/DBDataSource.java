package com.winsun.fruitmix.data.db;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.winsun.fruitmix.business.LoadTokenParam;
import com.winsun.fruitmix.data.DataSource;
import com.winsun.fruitmix.data.dataOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FileDownloadLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.OperateMediaShareResult;
import com.winsun.fruitmix.data.dataOperationResult.OperateUserResult;
import com.winsun.fruitmix.data.dataOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.data.dataOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.fileModule.download.FileDownloadState;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.mediaModule.model.MediaShareContent;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.model.EquipmentAlias;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/2/7.
 */

public class DBDataSource implements DataSource {

    private static DBDataSource INSTANCE;

    public static final String TAG = DBDataSource.class.getSimpleName();

    private DBUtils mDBUtils;
    private SharedPreferences mSharedPreferences;
    private ContentResolver mContentResolver;

    private DBDataSource(Context context) {

        mDBUtils = DBUtils.getInstance(context);
        mSharedPreferences = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        mContentResolver = context.getContentResolver();

    }

    public static DBDataSource getInstance(Context context) {
        if (INSTANCE == null)
            INSTANCE = new DBDataSource(context);

        return INSTANCE;
    }

    @Override
    public void init() {

    }

    @Override
    public OperateUserResult insertUser(String url, String token, String userName, String userPassword) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public List<User> loadUserByLoginApi(String token, String url) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public List<EquipmentAlias> loadEquipmentAlias(String token, String url) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult insertUsers(List<User> users) {

        mDBUtils.insertRemoteUsers(users);

        return new OperationSuccess();
    }


    @Override
    public OperateMediaShareResult insertRemoteMediaShare(String url, String token, MediaShare mediaShare) {

        mDBUtils.insertRemoteMediaShare(mediaShare);

        OperateMediaShareResult result = new OperateMediaShareResult();
        result.setMediaShare(mediaShare);

        return result;
    }

    @Override
    public OperationResult insertRemoteMediaShares(Collection<MediaShare> mediaShares) {

        mDBUtils.insertRemoteMediaShares(mediaShares);

        return new OperationSuccess();
    }

    @Override
    public OperationResult insertLocalMedias(List<Media> medias) {

        mDBUtils.insertLocalMedias(medias);

        return new OperationSuccess();
    }

    @Override
    public OperationResult insertRemoteMedias(List<Media> medias) {

        mDBUtils.insertRemoteMedias(medias);

        return new OperationSuccess();
    }

    @Override
    public OperationResult modifyRemoteMediaShare(String url, String token, String requestData, MediaShare modifiedMediaShare) {

        mDBUtils.updateRemoteMediaShare(modifiedMediaShare);

        return new OperationSuccess();
    }

    @Override
    public OperationResult modifyMediaInRemoteMediaShare(String url, String token, String requestData, MediaShare diffContentsOriginalMediaShare, MediaShare diffContentsModifiedMediaShare, MediaShare modifiedMediaShare) {

        long dbResult = 0;

        for (MediaShareContent mediaShareContent : diffContentsOriginalMediaShare.getMediaShareContents()) {
            dbResult = mDBUtils.deleteRemoteMediaShareContentByID(mediaShareContent.getId());
        }

        for (MediaShareContent mediaShareContent : diffContentsModifiedMediaShare.getMediaShareContents()) {
            dbResult = mDBUtils.insertRemoteMediaShareContent(mediaShareContent, diffContentsModifiedMediaShare.getUuid());
        }

        Log.i(TAG, "modify media in remote mediashare which source is network result:" + dbResult);

        if (dbResult > 0) {
            return new OperationSuccess();
        } else {
            return new OperationSQLException();
        }

    }

    @Override
    public OperationResult deleteRemoteMediaShare(String url, String token, MediaShare mediaShare) {

        mDBUtils.deleteRemoteMediaShareByUUIDs(new String[]{mediaShare.getUuid()});

        return new OperationSuccess();
    }

    @Override
    public DeviceIDLoadOperationResult loadDeviceID(String url, String token) {

        DeviceIDLoadOperationResult result = new DeviceIDLoadOperationResult();

        String deviceID = getGlobalData(Util.DEVICE_ID_MAP_NAME);

        result.setDeviceID(deviceID);

        if (deviceID != null)
            result.setOperationResult(new OperationSuccess());
        else
            result.setOperationResult(new OperationSQLException());

        return result;
    }

    @Override
    public OperationResult insertDeviceID(String deviceID) {

        saveGlobalData(Util.DEVICE_ID_MAP_NAME, deviceID);

        return new OperationSuccess();
    }

    @Override
    public UsersLoadOperationResult loadUsers(String loadUserUrl, String loadOtherUserUrl, String token) {

        List<User> users = mDBUtils.getAllRemoteUser();

        UsersLoadOperationResult result = new UsersLoadOperationResult();

        result.setUsers(users);
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public User loadUser(String userUUID) {

        return mDBUtils.getRemoteUser(userUUID);
    }

    @Override
    public Collection<String> loadAllUserUUID() {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllRemoteMedias(String url, String token) {

        MediasLoadOperationResult result = new MediasLoadOperationResult();

        result.setMedias(mDBUtils.getAllRemoteMedia());
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public MediasLoadOperationResult loadAllLocalMedias() {

        MediasLoadOperationResult result = new MediasLoadOperationResult();

        result.setMedias(mDBUtils.getAllLocalMedia());
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public Collection<String> loadLocalMediaThumbs() {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult insertLocalMedia(String url, String token, Media media) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult updateLocalMedia(Media media) {

        if (mDBUtils != null)
            mDBUtils.updateLocalMedia(media);

        return null;
    }

    @Override
    public OperationResult updateLocalMedias(Collection<Media> medias) {

        mDBUtils.updateLocalMedias(medias);

        return new OperationSuccess();
    }

    @Override
    public Collection<String> loadRemoteMediaUUIDs() {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public Media loadLocalMediaByThumb(String thumb) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public MediaSharesLoadOperationResult loadAllRemoteMediaShares(String url, String token) {

        MediaSharesLoadOperationResult result = new MediaSharesLoadOperationResult();

        result.setMediaShares(mDBUtils.getAllRemoteMediaShare());
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public FilesLoadOperationResult loadRemoteFolder(String url, String token) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult insertRemoteFiles(AbstractRemoteFile folder) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult loadRemoteFile(String baseUrl, String token, FileDownloadState fileDownloadState) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult deleteAllRemoteFiles() {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public FileSharesLoadOperationResult loadRemoteFileRootShares(String loadFileSharedWithMeUrl, String loadFileShareWithOthersUrl, String token) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult insertRemoteFileShare(List<AbstractRemoteFile> files) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public OperationResult deleteAllRemoteFileShare() {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public FileDownloadItem loadDownloadFileRecord(String fileUUID) {
        for (FileDownloadItem fileDownloadItem : FileDownloadManager.INSTANCE.getFileDownloadItems()) {

            if (fileDownloadItem.getFileUUID().equals(fileUUID)) {
                return fileDownloadItem;
            }
        }

        return null;
    }

    @Override
    public FileDownloadLoadOperationResult loadDownloadedFilesRecord(String userUUID) {

        FileDownloadLoadOperationResult result = new FileDownloadLoadOperationResult();

        FileDownloadManager fileDownloadManager = FileDownloadManager.INSTANCE;

        List<FileDownloadItem> fileDownloadItems = mDBUtils.getAllCurrentLoginUserDownloadedFile(userUUID);

        String[] fileNames = new File(FileUtil.getDownloadFileStoreFolderPath()).list();

        if (fileNames != null && fileNames.length != 0) {

            List<String> fileNameList = Arrays.asList(fileNames);

            Iterator<FileDownloadItem> itemIterator = fileDownloadItems.iterator();
            while (itemIterator.hasNext()) {
                FileDownloadItem fileDownloadItem = itemIterator.next();

                if (!fileNameList.contains(fileDownloadItem.getFileName())) {
                    itemIterator.remove();
                    mDBUtils.deleteDownloadedFileByUUID(fileDownloadItem.getFileUUID());
                }
            }

        }

        for (FileDownloadItem fileDownloadItem : fileDownloadItems) {
            fileDownloadManager.addDownloadedFile(fileDownloadItem);
        }

        result.setFileDownloadItems(fileDownloadManager.getFileDownloadItems());
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public OperationResult deleteDownloadedFileRecord(List<String> fileUUIDs, String userUUID) {

        for (String fileUUID : fileUUIDs) {
            mDBUtils.deleteDownloadedFileByUUIDAndCreatorUUID(fileUUID, userUUID);
        }

        FileDownloadManager.INSTANCE.deleteFileDownloadItem(fileUUIDs);

        return new OperationSuccess();
    }

    @Override
    public OperationResult insertDownloadedFileRecord(FileDownloadItem fileDownloadItem) {

        mDBUtils.insertDownloadedFile(fileDownloadItem);

        return new OperationSuccess();
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {

        TokenLoadOperationResult result = new TokenLoadOperationResult();
        result.setToken(getGlobalData(Util.JWT));

        return result;
    }

    @Override
    public String loadToken() {
        return getGlobalData(Util.JWT);
    }

    @Override
    public MediasLoadOperationResult loadLocalMediaInCamera(Collection<String> loadedMediaUUIDs) {

        MediasLoadOperationResult result = new MediasLoadOperationResult();

        String[] fields = {MediaStore.Images.Media.HEIGHT, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
        Cursor cursor;
        List<Media> mediaList;
        Media media;
        File f;
        SimpleDateFormat df;
        Calendar date;

//        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "='" + bucketName + "'", null, null);

        cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, null, null, null);

        if (cursor == null || !cursor.moveToFirst() || loadedMediaUUIDs == null) {
            result.setMedias(Collections.<Media>emptyList());
            return result;
        }

        mediaList = new ArrayList<>();
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = Calendar.getInstance();

        do {

            String thumb = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

            if (loadedMediaUUIDs.contains(thumb)) {
                continue;
            }

            if (thumb.contains(FileUtil.getLocalPhotoThumbnailFolderPath())) {
                continue;
            }

            media = new Media();
            media.setThumb(thumb);
            media.setWidth(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)));
            media.setHeight(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)));

            f = new File(thumb);
            date.setTimeInMillis(f.lastModified());
            media.setTime(df.format(date.getTime()));

            media.setSelected(false);
            media.setLoaded(false);

            int orientation = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));

            switch (orientation) {
                case 0:
                    media.setOrientationNumber(1);
                    break;
                case 90:
                    media.setOrientationNumber(6);
                    break;
                case 180:
                    media.setOrientationNumber(4);
                    break;
                case 270:
                    media.setOrientationNumber(3);
                    break;
                default:
                    media.setOrientationNumber(1);
            }

            media.setLocal(true);
            media.setSharing(true);
            media.setUuid("");

            mediaList.add(media);

        }
        while (cursor.moveToNext());

        cursor.close();

        result.setMedias(mediaList);

        return result;

    }

    @Override
    public Media loadMedia(String mediaKey) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public MediaShare loadRemoteMediaShare(String mediaShareUUID) {
        throw new UnsupportedOperationException(Util.UNSUPPORT_OPERATION);
    }

    @Override
    public void deleteToken() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Util.JWT, null);
        editor.apply();

        clearGlobalData(Util.JWT);
    }

    @Override
    public OperationResult insertToken(String token) {

        saveGlobalData(Util.JWT, token);

        return new OperationSuccess();
    }

    @Override
    public String loadGateway() {

        return getGlobalData(Util.GATEWAY);
    }

    @Override
    public OperationResult insertGateway(String gateway) {

        saveGlobalData(Util.GATEWAY, gateway);

        return new OperationSuccess();
    }

    @Override
    public OperationResult insertLoginUserUUID(String userUUID) {

        saveGlobalData(Util.USER_UUID, userUUID);

        return null;
    }

    @Override
    public String loadLoginUserUUID() {
        return getGlobalData(Util.USER_UUID);
    }

    @Override
    public void deleteLoggedInUser(LoggedInUser loggedInUser) {
        mDBUtils.deleteLoggerUserByUserUUID(loggedInUser.getUser().getUuid());
    }

    @Override
    public boolean getShowAlbumTipsValue() {

        return mSharedPreferences.getBoolean(Util.SHOW_ALBUM_TIPS, true);
    }

    @Override
    public void saveShowAlbumTipsValue(boolean value) {

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Util.SHOW_ALBUM_TIPS, value);
        editor.apply();
    }

    @Override
    public boolean getShowPhotoReturnTipsValue() {

        return mSharedPreferences.getBoolean(Util.SHOW_PHOTO_RETURN_TIPS, true);
    }

    @Override
    public void saveShowPhotoReturnTipsValue(boolean value) {

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Util.SHOW_PHOTO_RETURN_TIPS, value);
        editor.apply();
    }

    @Override
    public void deleteDeviceID() {

        clearGlobalData(Util.DEVICE_ID_MAP_NAME);

    }

    @Override
    public boolean deleteAllRemoteMediaShare() {

        mDBUtils.deleteAllRemoteMediaShare();

        return true;
    }

    @Override
    public boolean deleteAllRemoteMedia() {

        mDBUtils.deleteAllRemoteMedia();

        return true;
    }

    @Override
    public boolean deleteAllRemoteUsers() {

        mDBUtils.deleteAllRemoteUser();

        return true;
    }

    private User getUser() {

        User user = new User();
        user.setUserName(mSharedPreferences.getString(Util.USER_NAME, ""));
        user.setDefaultAvatar(Util.getUserNameFirstLetter(user.getUserName()));
        user.setDefaultAvatarBgColor(mSharedPreferences.getInt(Util.USER_BG_COLOR, 0));
        user.setAdmin(mSharedPreferences.getBoolean(Util.USER_IS_ADMIN, false));
        user.setHome(mSharedPreferences.getString(Util.USER_HOME, ""));
        user.setUuid(mSharedPreferences.getString(Util.USER_UUID, ""));

        return user;
    }

    private void saveUser(String userName, int userDefaultAvatarBgColor, boolean userIsAdmin, String userHome, String userUUID) {

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Util.USER_NAME, userName);
        editor.putInt(Util.USER_BG_COLOR, userDefaultAvatarBgColor);
        editor.putBoolean(Util.USER_IS_ADMIN, userIsAdmin);
        editor.putString(Util.USER_HOME, userHome);
        editor.putString(Util.USER_UUID, userUUID);
        editor.apply();
    }

    private String getGlobalData(String name) {

        return mSharedPreferences.getString(name, null);
    }

    private void saveGlobalData(String name, String data) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(name, data);
        mEditor.apply();
    }

    private void clearGlobalData(String name) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(name, null);
        mEditor.apply();
    }

    @Override
    public List<LoggedInUser> loadLoggedInUser() {
        return mDBUtils.getAllLoggedInUser();
    }

    @Override
    public void insertLoggedInUser(List<LoggedInUser> loggedInUsers) {
        mDBUtils.insertLoggedInUserInDB(loggedInUsers);
    }

    @Override
    public boolean getAutoUploadOrNot() {
        return mSharedPreferences.getBoolean(Util.AUTO_UPLOAD_OR_NOT, true);
    }

    @Override
    public void saveAutoUploadOrNot(boolean autoUploadOrNot) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(Util.AUTO_UPLOAD_OR_NOT, autoUploadOrNot);
        mEditor.apply();
    }

    @Override
    public void saveCurrentUploadDeviceID(String currentUploadDeviceID) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(Util.CURRENT_UPLOAD_DEVICE_ID, currentUploadDeviceID);
        mEditor.apply();
    }

    @Override
    public String getCurrentUploadDeviceID() {
        return mSharedPreferences.getString(Util.CURRENT_UPLOAD_DEVICE_ID, "");
    }
}
