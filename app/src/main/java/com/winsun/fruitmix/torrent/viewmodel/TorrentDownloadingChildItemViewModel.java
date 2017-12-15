package com.winsun.fruitmix.torrent.viewmodel;

import android.content.Context;
import android.databinding.ObservableField;
import android.text.format.Formatter;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.torrent.data.TorrentDownloadInfo;
import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/12/14.
 */

public class TorrentDownloadingChildItemViewModel {

    private TorrentDownloadInfo mTorrentDownloadInfo;

    public TorrentDownloadingChildItemViewModel(TorrentDownloadInfo torrentDownloadInfo) {
        mTorrentDownloadInfo = torrentDownloadInfo;
    }

    public String getName() {
        return mTorrentDownloadInfo.getFileName();
    }

    public String getSpeed(Context context) {

        if (mTorrentDownloadInfo.isPause())
            return context.getString(R.string.paused);
        else
            return FileUtil.formatFileSize(mTorrentDownloadInfo.getDownloadedSpeed()) + "/S";

    }

    public int getProgress() {

        return (int) Math.round(mTorrentDownloadInfo.getProgress() * 100);

    }

    public String getPercent() {

        return getProgress() + "%";

    }

    public boolean isPause(){
        return mTorrentDownloadInfo.isPause();
    }

    public String getHash(){
        return mTorrentDownloadInfo.getHash();
    }

    public String getDownloadedAndTotalSize(Context context) {

        return Formatter.formatFileSize(context, mTorrentDownloadInfo.getDownloaded()) + "/"
                + FileUtil.formatFileSize(mTorrentDownloadInfo.getTotalSize());

    }


}
