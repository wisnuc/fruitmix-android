package com.winsun.fruitmix.refactor.business;

import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.refactor.business.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.MediaOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.UserOperationCallback;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.db.DBDataSource;
import com.winsun.fruitmix.refactor.data.loadOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;

import java.util.ArrayList;
import java.util.List;

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

    public String loadTokenInDB() {
        return mDBDataSource.loadToken(null).getToken();
    }

    public void loadRemoteToken(LoadTokenParam param, LoadTokenOperationCallback.LoadTokenCallback callback) {

        TokenLoadOperationResult result = mServerDataSource.loadToken(param);
        if (result.getOperationResult().getOperationResultType().equals(OperationResultType.SUCCEED)) {
            callback.onLoadSucceed(result.getOperationResult(), result.getToken());
        } else {
            callback.onLoadFail(result.getOperationResult());
        }

    }

    public void loadUsers(String token, UserOperationCallback.LoadUsersCallback callback) {

    }

    public void loadMedias(String token, MediaOperationCallback.LoadMediasCallback callback) {


    }

    public void loadMediaShares(String token, MediaShareOperationCallback.LoadMediaSharesCallback callback) {

    }


    public LoadTokenParam getLoadTokenParamInDB() {

        return ((DBDataSource) mDBDataSource).getLoadTokenParam();

    }

    public List<EquipmentAlias> loadEquipmentAlais(String url) {

        List<EquipmentAlias> equipmentAliases = new ArrayList<>();

        return equipmentAliases;
    }

    public List<User> loadUserByLoginApi(String url){
        return new ArrayList<>();
    }

}
