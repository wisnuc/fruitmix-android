package com.winsun.fruitmix.refactor.data.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.refactor.business.LoadTokenParam;
import com.winsun.fruitmix.refactor.data.DataSource;
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
    public boolean saveUser(User user) {
        return false;
    }

    @Override
    public boolean saveMediaShare(MediaShare mediaShare) {
        return false;
    }

    @Override
    public boolean modifyMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare) {
        return false;
    }

    @Override
    public boolean modifyMediaInMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare) {
        return false;
    }

    @Override
    public boolean deleteMediaShare(MediaShare mediaShare) {
        return false;
    }

    @Override
    public String loadDeviceID() {
        return null;
    }

    @Override
    public List<User> loadUsers() {
        return null;
    }

    @Override
    public List<Media> loadMedias() {
        return null;
    }

    @Override
    public List<MediaShare> loadMediaShares() {
        return null;
    }

    @Override
    public List<AbstractRemoteFile> loadFile() {
        return null;
    }

    @Override
    public List<AbstractRemoteFile> loadFileShare() {
        return null;
    }

    @Override
    public String loadToken() {
        return mSharedPreferences.getString(Util.JWT,"");
    }

    private void loadLocalMedias(){

    }

    public LoadTokenParam getLoadTokenParam(){

        return new LoadTokenParam(mSharedPreferences.getString(Util.GATEWAY, null),mSharedPreferences.getString(Util.USER_UUID,""),mSharedPreferences.getString(Util.PASSWORD,""));

    }
}
