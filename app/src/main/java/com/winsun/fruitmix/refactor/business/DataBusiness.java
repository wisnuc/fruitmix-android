package com.winsun.fruitmix.refactor.business;

import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.data.DataSource;
import com.winsun.fruitmix.util.LocalCache;

/**
 * Created by Administrator on 2017/2/6.
 */

public class DataBusiness implements DataSource {

    private static DataBusiness INSTANCE;

    private DataSource mCacheDataSource;
    private DataSource mDBDataSource;
    private DataSource mServerDataSource;

    private DataBusiness(DataSource cacheDataSource, DataSource dbDataSource, DataSource serverDataSource) {

        mCacheDataSource = cacheDataSource;
        mDBDataSource = dbDataSource;
        mServerDataSource = serverDataSource;

    }

    public static DataBusiness getInstance(DataSource cacheDataSource, DataSource dbDataSource, DataSource serverDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new DataBusiness(cacheDataSource, dbDataSource, serverDataSource);
        }
        return INSTANCE;
    }


    @Override
    public void saveUser(User user, DataOperationCallback callback) {

    }

    @Override
    public void saveMedia(Media media, DataOperationCallback callback) {

    }

    @Override
    public void saveMediaShare(MediaShare mediaShare, DataOperationCallback callback) {

    }

    @Override
    public void modifyMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare, DataOperationCallback callback) {

    }

    @Override
    public void modifyMediaInMediaShare(MediaShare originalMediaShare, MediaShare modifiedMediaShare, DataOperationCallback callback) {

    }

    @Override
    public void deleteMediaShare(MediaShare mediaShare, DataOperationCallback callback) {

    }

    @Override
    public void retrieveDeviceID(RetrieveDeviceIDOperationCallback callback) {

    }

    @Override
    public void retrieveUser(RetrieveUserOperationCallback callback) {

    }

    @Override
    public void retrieveLocalMedia(RetrieveMediaOperationCallback callback) {

    }

    @Override
    public void retrieveRemoteMedia(RetrieveMediaOperationCallback callback) {

    }

    @Override
    public void retrieveMediaShare(RetrieveMediaShareOperationCallback callback) {

    }

    @Override
    public void retrieveFile(RetrieveFileOperationCallback callback) {

    }

    @Override
    public void retrieveFileShare(RetrieveFileOperationCallback callback) {

    }

    @Override
    public void retrieveToken(final RetrieveTokenOperationCallback callback) {

        mDBDataSource.retrieveToken(new RetrieveTokenOperationCallback() {
            @Override
            public void onOperationSucceed(OperationResult operationResult, String token) {

                callback.onOperationSucceed(operationResult, token);

                mServerDataSource.retrieveToken(null);

            }

            @Override
            public void onOperationFail(OperationResult operationResult) {
                callback.onOperationFail(operationResult);
            }
        });


    }
}
