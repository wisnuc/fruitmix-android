package com.winsun.fruitmix.torrent.data;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteMkDirParser;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.user.User;

import org.json.JSONException;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2017/12/13.
 */

public class TorrentDataRepositoryImpl extends BaseDataRepository implements TorrentDataRepository {

    private TorrentDataSource mTorrentDataSource;

    private User currentUser;

    private StationFileRepository mStationFileRepository;

    public static final String DOWNLOAD_FOLDER_NAME = "Download";

    public TorrentDataRepositoryImpl(ThreadManager threadManager, TorrentDataSource torrentDataSource, User currentUser, StationFileRepository stationFileRepository) {
        super(threadManager);
        mTorrentDataSource = torrentDataSource;
        this.currentUser = currentUser;
        mStationFileRepository = stationFileRepository;
    }

    private void createDownloadFolderIfNotExist(final BaseOperateDataCallback<String> callback) {

        mStationFileRepository.getFile(currentUser.getHome(), currentUser.getHome(), "",new BaseLoadDataCallback<AbstractRemoteFile>() {
            @Override
            public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {

                String downloadFolderUUID = getDownloadFolderUUID(data);

                if (downloadFolderUUID.isEmpty())
                    createDownloadFolder(callback);
                else
                    callback.onSucceed(downloadFolderUUID, operationResult);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onFail(operationResult);

            }
        });

    }

    private String getDownloadFolderUUID(List<AbstractRemoteFile> files) {

        for (AbstractRemoteFile file : files) {
            if (file.getName().equals(DOWNLOAD_FOLDER_NAME))
                return file.getUuid();
        }

        return "";
    }


    private void createDownloadFolder(final BaseOperateDataCallback<String> callback) {

        mStationFileRepository.createFolder(DOWNLOAD_FOLDER_NAME, currentUser.getHome(), currentUser.getHome(), new BaseOperateDataCallback<HttpResponse>() {
            @Override
            public void onSucceed(HttpResponse data, OperationResult result) {

                RemoteDataParser<AbstractRemoteFile> parser = new RemoteMkDirParser();

                try {
                    AbstractRemoteFile file = parser.parse(data.getResponseData());

                    callback.onSucceed(file.getUuid(), result);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFail(OperationResult operationResult) {
                callback.onFail(operationResult);
            }
        });

    }


    @Override
    public void getAllTorrentDownloadInfo(final BaseLoadDataCallback<TorrentDownloadInfo> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mTorrentDataSource.getAllTorrentDownloadInfo(createLoadCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void postTorrentDownloadTask(final File torrent, final BaseOperateDataCallback<TorrentRequestParam> callback) {

        createDownloadFolderIfNotExist(new BaseOperateDataCallback<String>() {
            @Override
            public void onSucceed(final String data, OperationResult result) {

                mThreadManager.runOnCacheThread(new Runnable() {
                    @Override
                    public void run() {
                        mTorrentDataSource.postTorrentDownloadTask(data, torrent, createOperateCallbackRunOnMainThread(callback));
                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {
                callback.onFail(operationResult);
            }
        });


    }

    @Override
    public void postTorrentDownloadTask( final String magnetUrl, final BaseOperateDataCallback<TorrentRequestParam> callback) {

        createDownloadFolderIfNotExist(new BaseOperateDataCallback<String>() {
            @Override
            public void onSucceed(final String data, OperationResult result) {

                mThreadManager.runOnCacheThread(new Runnable() {
                    @Override
                    public void run() {
                        mTorrentDataSource.postTorrentDownloadTask(data, magnetUrl, createOperateCallbackRunOnMainThread(callback));
                    }
                });

            }

            @Override
            public void onFail(OperationResult operationResult) {
                callback.onFail(operationResult);
            }
        });

    }

    @Override
    public void pauseTorrentDownloadTask(final TorrentRequestParam torrentRequestParam, final BaseOperateDataCallback<Void> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mTorrentDataSource.pauseTorrentDownloadTask(torrentRequestParam, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void resumeTorrentDownloadTask(final TorrentRequestParam torrentRequestParam, final BaseOperateDataCallback<Void> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mTorrentDataSource.resumeTorrentDownloadTask(torrentRequestParam, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void deleteTorrentDownloadTask(final TorrentRequestParam torrentRequestParam, final BaseOperateDataCallback<Void> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mTorrentDataSource.deleteTorrentDownloadTask(torrentRequestParam, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }


}
