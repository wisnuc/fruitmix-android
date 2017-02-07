package com.winsun.fruitmix.refactor.business;

import com.winsun.fruitmix.refactor.common.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.refactor.common.callback.MediaOperationCallback;
import com.winsun.fruitmix.refactor.common.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.refactor.common.callback.UserOperationCallback;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.db.DBDataSource;
import com.winsun.fruitmix.util.LocalCache;

/**
 * Created by Administrator on 2017/2/6.
 */

public class DataRepository {

    private static DataRepository INSTANCE;

    private DataSource mMemoryDataSource;
    private DataSource mDBDataSource;
    private DataSource mServerDataSource;

    private DataRepository(DataSource memoryDataSource, DataSource dbDataSource, DataSource serverDataSource) {

        mMemoryDataSource = memoryDataSource;
        mDBDataSource = dbDataSource;
        mServerDataSource = serverDataSource;

    }

    public static DataRepository getInstance(DataSource cacheDataSource, DataSource dbDataSource, DataSource serverDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository(cacheDataSource, dbDataSource, serverDataSource);
        }
        return INSTANCE;
    }

    public String loadTokenInDB(){
        return mDBDataSource.loadToken();
    }

    public void loadRemoteToken(LoadTokenParam param, LoadTokenOperationCallback.LoadTokenCallback callback){

        mServerDataSource.loadToken();

    }

    public void loadUsers(String token, UserOperationCallback.LoadUsersCallback callback){

    }

    public void loadMedias(String token, MediaOperationCallback.LoadMediasCallback callback){


    }

    public void loadMediaShares(String token, MediaShareOperationCallback.LoadMediaSharesCallback callback){

    }


    public LoadTokenParam getLoadTokenParamInDB(){

        return ((DBDataSource)mDBDataSource).getLoadTokenParam();

    }


}
