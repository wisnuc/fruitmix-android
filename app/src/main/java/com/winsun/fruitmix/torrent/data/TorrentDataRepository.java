package com.winsun.fruitmix.torrent.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;

import java.io.File;

/**
 * Created by Administrator on 2017/12/13.
 */

public interface TorrentDataRepository {

    void getAllTorrentDownloadInfo(BaseLoadDataCallback<TorrentDownloadInfo> callback);

    void postTorrentDownloadTask(File torrent, BaseOperateDataCallback<TorrentRequestParam> callback);

    void postTorrentDownloadTask(String magnetUrl, BaseOperateDataCallback<TorrentRequestParam> callback);

    void pauseTorrentDownloadTask(TorrentRequestParam torrentRequestParam, BaseOperateDataCallback<Void> callback);

    void resumeTorrentDownloadTask(TorrentRequestParam torrentRequestParam, BaseOperateDataCallback<Void> callback);

    void deleteTorrentDownloadTask(TorrentRequestParam torrentRequestParam, BaseOperateDataCallback<Void> callback);


}
