package com.winsun.fruitmix.refactor.data.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.loadOperationResult.DeviceIDLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.FileSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.FilesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.MediaSharesLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.MediasLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.List;

/**
 * Created by Administrator on 2017/2/7.
 */

public class DBDataSource implements DataSource {

    private DBUtils mDBUtils;
    private SharedPreferences mSharedPreferences;

    public DBDataSource(Context context){

        mDBUtils = DBUtils.getInstance(context);
        mSharedPreferences = context.getSharedPreferences(Util.FRUITMIX_SHAREDPREFERENCE_NAME,Context.MODE_PRIVATE);

        loadLocalMedias();
    }

    @Override
    public OperationResult saveUser(User user) {
        return null;
    }

    @Override
    public OperationResult saveMediaShare(MediaShare mediaShare) {
        return null;
    }

    @Override
    public OperationResult modifyMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare) {
        return null;
    }

    @Override
    public OperationResult modifyMediaInMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare) {
        return null;
    }

    @Override
    public OperationResult deleteMediaShare(MediaShare mediaShare) {
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
    public MediasLoadOperationResult loadMedias() {
        return null;
    }

    @Override
    public MediaSharesLoadOperationResult loadMediaShares() {
        return null;
    }

    @Override
    public FilesLoadOperationResult loadFiles() {
        return null;
    }

    @Override
    public FileSharesLoadOperationResult loadFileShares() {
        return null;
    }

    @Override
    public TokenLoadOperationResult loadToken(LoadTokenParam param) {

        String token = mSharedPreferences.getString(Util.JWT,"");

        TokenLoadOperationResult result = new TokenLoadOperationResult();
        result.setToken(token);

        return result;
    }

    @Override
    public User loadCurrentUser() {
        return null;
    }

    private void loadLocalMedias(){

    }

    public LoadTokenParam getLoadTokenParam(){

        return new LoadTokenParam(mSharedPreferences.getString(Util.GATEWAY, null),mSharedPreferences.getString(Util.USER_UUID,""),mSharedPreferences.getString(Util.PASSWORD,""));

    }

    public void logout(){

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Util.JWT, null);
        editor.apply();

        mDBUtils.updateLocalMediasUploadedFalse();
    }

    public boolean getShowAlbumTipsValue() {

        return mSharedPreferences.getBoolean(Util.SHOW_ALBUM_TIPS, true);
    }

    public void saveShowAlbumTipsValue(boolean value) {

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Util.SHOW_ALBUM_TIPS, value);
        editor.apply();
    }

}
