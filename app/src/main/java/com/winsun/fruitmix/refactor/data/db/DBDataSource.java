package com.winsun.fruitmix.refactor.data.db;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.dataOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FileDownloadLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.OperateMediaShareResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.OperateUserResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.dataOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;
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

    private DBUtils mDBUtils;
    private SharedPreferences mSharedPreferences;
    private ContentResolver mContentResolver;

    public DBDataSource(Context context) {

        mDBUtils = DBUtils.getInstance(context);
        mSharedPreferences = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME, Context.MODE_PRIVATE);
        mContentResolver = context.getContentResolver();

    }

    @Override
    public OperateUserResult insertUser(String url, String token, String userName, String userPassword) {
        return null;
    }

    @Override
    public List<User> loadUserByLoginApi(String token, String url) {
        return null;
    }

    @Override
    public List<EquipmentAlias> loadEquipmentAlias(String token, String url) {
        return null;
    }

    @Override
    public String loadGateway() {
        return null;
    }

    @Override
    public String loadPort() {
        return null;
    }

    @Override
    public OperationResult insertUsers(List<User> users) {

        mDBUtils.insertRemoteUsers(users);

        return new OperationSuccess();
    }


    @Override
    public OperateMediaShareResult insertRemoteMediaShare(String url,String token,MediaShare mediaShare) {

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
    public OperationResult modifyRemoteMediaShare(String url,String token,String requestData, MediaShare modifiedMediaShare) {

        mDBUtils.updateRemoteMediaShare(modifiedMediaShare);

        return new OperationSuccess();
    }

    @Override
    public OperationResult deleteRemoteMediaShare(String url,String token,MediaShare mediaShare) {

        mDBUtils.deleteRemoteMediaShareByUUIDs(new String[]{mediaShare.getUuid()});

        return new OperationSuccess();
    }

    @Override
    public DeviceIDLoadOperationResult loadDeviceID() {

        DeviceIDLoadOperationResult result = new DeviceIDLoadOperationResult();

        result.setDeviceID(getGlobalData(Util.DEVICE_ID_MAP_NAME));
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public OperationResult insertDeviceID(String deviceID) {

        saveGlobalData(Util.DEVICE_ID_MAP_NAME, deviceID);

        return new OperationSuccess();
    }

    @Override
    public UsersLoadOperationResult loadUsers() {

        List<User> users = mDBUtils.getAllRemoteUser();

        UsersLoadOperationResult result = new UsersLoadOperationResult();

        result.setUsers(users);
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public User loadUser(String userUUID) {

        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllRemoteMedias() {

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
    public Collection<String> loadLocalMediaUUIDs() {
        return null;
    }

    @Override
    public MediaSharesLoadOperationResult loadAllRemoteMediaShares() {

        MediaSharesLoadOperationResult result = new MediaSharesLoadOperationResult();

        result.setMediaShares(mDBUtils.getAllRemoteMediaShare());
        result.setOperationResult(new OperationSuccess());

        return result;
    }

    @Override
    public FilesLoadOperationResult loadRemoteFiles(String folderUUID) {
        return null;
    }

    @Override
    public FileDownloadLoadOperationResult loadDownloadedFilesRecord() {

        FileDownloadLoadOperationResult result = new FileDownloadLoadOperationResult();

        FileDownloadManager fileDownloadManager = FileDownloadManager.INSTANCE;

        List<FileDownloadItem> fileDownloadItems = mDBUtils.getAllDownloadedFile();

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
    public OperationResult deleteDownloadedFileRecord(List<String> fileUUIDs) {

        for (String fileUUID : fileUUIDs) {
            mDBUtils.deleteDownloadedFileByUUID(fileUUID);
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
    public FileSharesLoadOperationResult loadRemoteFileRootShares() {
        return null;
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {

        TokenLoadOperationResult result = new TokenLoadOperationResult();
        result.setToken(getGlobalData(Util.JWT));

        return result;
    }

    @Override
    public User loadCurrentLoginUser() {
        return getUser();
    }

    @Override
    public OperationResult insertCurrentLoginUser(User user) {
        saveUser(user.getUserName(), user.getDefaultAvatarBgColor(), user.isAdmin(), user.getHome(), user.getUuid());
        return new OperationSuccess();
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

            media = new Media();
            media.setThumb(thumb);
            media.setWidth(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)));
            media.setHeight(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)));

            f = new File(thumb);
            date.setTimeInMillis(f.lastModified());
            media.setTime(df.format(date.getTime()));

            media.setUploaded(false);
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
        return null;
    }

    @Override
    public void updateLocalMediasUploadedFalse() {
        mDBUtils.updateLocalMediasUploadedFalse();
    }

    @Override
    public MediaShare loadRemoteMediaShare(String mediaShareUUID) {
        return null;
    }

    @Override
    public LoadTokenParam getLoadTokenParam() {

        return new LoadTokenParam(mSharedPreferences.getString(Util.GATEWAY, null), mSharedPreferences.getString(Util.USER_UUID, ""), mSharedPreferences.getString(Util.PASSWORD, ""));

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
    public OperationResult deleteAllRemoteMediaShare() {

        mDBUtils.deleteAllRemoteMediaShare();

        return new OperationSuccess();
    }

    @Override
    public OperationResult deleteAllRemoteMedia() {

        mDBUtils.deleteAllRemoteMedia();

        return new OperationSuccess();
    }

    @Override
    public OperationResult deleteAllRemoteUsers() {

        mDBUtils.deleteAllRemoteUser();

        return new OperationSuccess();
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
}
