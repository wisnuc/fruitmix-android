package com.winsun.fruitmix.refactor.data.db;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
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
import com.winsun.fruitmix.util.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
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
    public OperateUserResult insertUser(String userName, String userPassword) {
        return null;
    }

    @Override
    public OperationResult insertUsers(List<User> users) {
        return null;
    }


    @Override
    public OperateMediaShareResult insertRemoteMediaShare(MediaShare mediaShare) {
        return null;
    }

    @Override
    public OperationResult insertRemoteMediaShares(Collection<MediaShare> mediaShares) {
        return null;
    }

    @Override
    public OperationResult insertLocalMedias(List<Media> medias) {
        return null;
    }

    @Override
    public OperationResult insertRemoteMedias(List<Media> medias) {
        return null;
    }

    @Override
    public OperationResult modifyRemoteMediaShare(String requestData, MediaShare modifiedMediaShare) {
        return null;
    }

    @Override
    public OperationResult deleteRemoteMediaShare(MediaShare mediaShare) {
        return null;
    }

    @Override
    public DeviceIDLoadOperationResult loadDeviceID() {
        return null;
    }

    @Override
    public UsersLoadOperationResult loadUsers() {
        return null;
    }

    @Override
    public User loadUser(String userUUID) {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllRemoteMedias() {
        return null;
    }

    @Override
    public MediasLoadOperationResult loadAllLocalMedias() {
        return null;
    }

    @Override
    public Collection<String> loadLocalMediaUUIDs() {
        return null;
    }

    @Override
    public MediaSharesLoadOperationResult loadAllMediaShares() {
        return null;
    }

    @Override
    public FilesLoadOperationResult loadRemoteFiles(String folderUUID) {
        return null;
    }

    @Override
    public FileDownloadLoadOperationResult loadDownloadedFiles() {
        return null;
    }

    @Override
    public OperationResult deleteDownloadedFile(List<String> fileUUIDs) {
        return null;
    }

    @Override
    public FileSharesLoadOperationResult loadRemoteFileRootShares() {
        return null;
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {

        String token = mSharedPreferences.getString(Util.JWT, "");

        TokenLoadOperationResult result = new TokenLoadOperationResult();
        result.setToken(token);

        return result;
    }

    @Override
    public User loadCurrentLoginUser() {
        return null;
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
    public List<EquipmentAlias> loadEquipmentAlias(String url) {
        return null;
    }

    @Override
    public List<User> loadUserByLoginApi(String url) {
        return null;
    }

}
