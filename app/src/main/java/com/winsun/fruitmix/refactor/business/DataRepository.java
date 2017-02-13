package com.winsun.fruitmix.refactor.business;

import android.os.Handler;
import android.os.Looper;

import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.refactor.business.callback.LoadEquipmentAliasCallback;
import com.winsun.fruitmix.refactor.business.callback.LoadTokenOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.MediaOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.MediaShareOperationCallback;
import com.winsun.fruitmix.refactor.business.callback.OperationCallback;
import com.winsun.fruitmix.refactor.business.callback.UserOperationCallback;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.refactor.data.db.DBDataSource;
import com.winsun.fruitmix.refactor.data.loadOperationResult.TokenLoadOperationResult;
import com.winsun.fruitmix.refactor.data.loadOperationResult.UsersLoadOperationResult;
import com.winsun.fruitmix.refactor.data.memory.MemoryDataSource;
import com.winsun.fruitmix.refactor.data.server.ServerDataSource;
import com.winsun.fruitmix.refactor.model.EquipmentAlias;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.List;

/**
 * Created by Administrator on 2017/2/6.
 */

public class DataRepository {

    private static DataRepository INSTANCE;

    private DataSource mMemoryDataSource;
    private DataSource mDBDataSource;
    private DataSource mServerDataSource;

    private Handler mHandler;

    private ExecutorServiceInstance instance;

    private UserOperationCallback.LoadCurrentUserCallback mLoadCurrentUserCallback;

    private DataRepository(DataSource memoryDataSource, DataSource dbDataSource, DataSource serverDataSource) {

        mMemoryDataSource = memoryDataSource;
        mDBDataSource = dbDataSource;
        mServerDataSource = serverDataSource;

        mHandler = new Handler(Looper.getMainLooper());

        instance = ExecutorServiceInstance.SINGLE_INSTANCE;

    }

    public static DataRepository getInstance(DataSource cacheDataSource, DataSource dbDataSource, DataSource serverDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository(cacheDataSource, dbDataSource, serverDataSource);
        }
        return INSTANCE;
    }

    public void shutdownFixedThreadPoolNow() {

        instance.shutdownFixedThreadPoolNow();
        instance = null;
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

    public void loadUsers(UserOperationCallback.LoadUsersCallback callback) {

        UsersLoadOperationResult result = mMemoryDataSource.loadUsers();

        if (result != null) {
            callback.onLoadSucceed(result.getOperationResult(), result.getUsers());
        } else {

            //TODO: need call in thread
            result = mServerDataSource.loadUsers();

            if (result.getOperationResult().getOperationResultType() == OperationResultType.SUCCEED) {

                for (User user : result.getUsers()) {
                    mDBDataSource.saveUser(user);
                    mMemoryDataSource.saveUser(user);
                }

            } else {

                result = mDBDataSource.loadUsers();
                for (User user : result.getUsers()) {
                    mMemoryDataSource.saveUser(user);
                }

            }

            //TODO:need call in main thread
            callback.onLoadSucceed(result.getOperationResult(), result.getUsers());
            if (mLoadCurrentUserCallback != null)
                mLoadCurrentUserCallback.onLoadSucceed(result.getOperationResult(), mMemoryDataSource.loadCurrentUser());

        }


    }

    public void loadLoadMediaInCamera(MediaOperationCallback.LoadMediasCallback callback) {

    }

    public Media loadMediaFromMemory(String mediaKey) {
        return ((MemoryDataSource) mMemoryDataSource).loadMedia(mediaKey);
    }

    public User loadUserFromMemory(String userUUID) {
        return ((MemoryDataSource) mMemoryDataSource).loadUser(userUUID);
    }

    public MediaShare loadMediaShareFromMemory(String mediaShareUUID){
        return ((MemoryDataSource) mMemoryDataSource).loadMediaShare(mediaShareUUID);
    }

    public void loadMedias(MediaOperationCallback.LoadMediasCallback callback) {


    }

    public void loadMediaInMediaShareFromMemory(MediaShare mediaShare, MediaOperationCallback.LoadMediasCallback callback){



    }

    public void handleMediasForMediaFragment(List<Media> medias, MediaOperationCallback.HandleMediaForMediaFragmentCallback callback) {


    }

    public void loadMediaShares(MediaShareOperationCallback.LoadMediaSharesCallback callback) {

    }

    public boolean isMediaSharePublic(MediaShare mediaShare) {
        return ((MemoryDataSource) mMemoryDataSource).isMediaSharePublic(mediaShare);
    }

    public boolean checkPermissionToOperateMediaShare(MediaShare mediaShare){
        return ((MemoryDataSource) mMemoryDataSource).checkPermissionToOperateMediaShare(mediaShare);
    }

    public boolean getShowAlbumTipsValue() {
        return ((DBDataSource) mDBDataSource).getShowAlbumTipsValue();
    }

    public void saveShowAlbumTipsValue(boolean value) {
        ((DBDataSource) mDBDataSource).saveShowAlbumTipsValue(value);
    }

    public LoadTokenParam getLoadTokenParamInDB() {

        return ((DBDataSource) mDBDataSource).getLoadTokenParam();

    }

    public void loadEquipmentAlias(final String url, final LoadEquipmentAliasCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final List<EquipmentAlias> equipmentAliases = ((ServerDataSource) mServerDataSource).loadEquipmentAlias(url);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (equipmentAliases.isEmpty()) {
                            callback.onLoadFail(null);
                        } else {
                            callback.onLoadSucceed(null, equipmentAliases);
                        }
                    }
                });
            }
        });


    }

    public void loadUserByLoginApi(final String url, final UserOperationCallback.LoadUsersCallback callback) {

        instance.doOneTaskInCachedThread(new Runnable() {
            @Override
            public void run() {

                final List<User> users = ((ServerDataSource) mServerDataSource).loadUserByLoginApi(url);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (url.isEmpty()) {
                            callback.onLoadFail(null);
                        } else {
                            callback.onLoadSucceed(null, users);
                        }
                    }
                });

            }
        });

    }

    public void logout(OperationCallback callback) {

        ((MemoryDataSource) mMemoryDataSource).logout();
        ((DBDataSource) mDBDataSource).logout();

        instance.shutdownFixedThreadPoolNow();

        ButlerService.stopTimingRetrieveMediaShare();

        Util.setRemoteMediaLoaded(false);
        Util.setRemoteMediaShareLoaded(false);
    }

    public void loadCurrentUser(UserOperationCallback.LoadCurrentUserCallback callback) {

        User user = mMemoryDataSource.loadCurrentUser();
        if (user != null) {
            callback.onLoadSucceed(new OperationSuccess(), user);
        } else {
            user = mDBDataSource.loadCurrentUser();
            callback.onLoadSucceed(new OperationSuccess(), user);

            addCallbackWhenLoadUsersFinished(callback);

        }

    }

    private void addCallbackWhenLoadUsersFinished(UserOperationCallback.LoadCurrentUserCallback callback) {
        mLoadCurrentUserCallback = callback;
    }

    public void createUser(String userName, String userPassword, UserOperationCallback.OperateUserCallback callback) {

    }

    public void createMediaShare(MediaShare mediaShare, MediaShareOperationCallback.OperateMediaShareCallback callback) {

    }

    public void modifyMediaShare(MediaShare mediaShare, MediaShareOperationCallback.OperateMediaShareCallback callback) {

    }

    public void deleteMediaShare(MediaShare mediaShare, MediaShareOperationCallback.OperateMediaShareCallback callback) {

    }
}
