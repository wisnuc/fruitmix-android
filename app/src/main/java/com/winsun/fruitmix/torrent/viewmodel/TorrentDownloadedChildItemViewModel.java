package com.winsun.fruitmix.torrent.viewmodel;

import android.text.format.DateFormat;

import com.winsun.fruitmix.torrent.data.TorrentDownloadInfo;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/12/14.
 */

public class TorrentDownloadedChildItemViewModel {

    private TorrentDownloadInfo mTorrentDownloadInfo;

    public TorrentDownloadedChildItemViewModel(TorrentDownloadInfo torrentDownloadInfo) {
        mTorrentDownloadInfo = torrentDownloadInfo;
    }

    public String getName() {
        return mTorrentDownloadInfo.getFileName();
    }

    public String getTotalSize() {
        return FileUtil.formatFileSize(mTorrentDownloadInfo.getTotalSize());
    }

    public String getTime() {
        return DateFormat.format("yyyy.MM.dd", mTorrentDownloadInfo.getTime()).toString();
    }

}
