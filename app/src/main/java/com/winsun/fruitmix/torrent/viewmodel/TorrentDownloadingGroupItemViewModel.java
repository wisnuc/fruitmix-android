package com.winsun.fruitmix.torrent.viewmodel;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.text.format.Formatter;

import com.winsun.fruitmix.util.FileUtil;

/**
 * Created by Administrator on 2017/12/14.
 */

public class TorrentDownloadingGroupItemViewModel {

    private int itemCount;

    public String getFormatItemCount() {
        return "(" + itemCount + ")";
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    private double totalSpeed;

    public String getTotalSpeed() {

        if (totalSpeed == 0)
            return "";
        else
            return FileUtil.formatFileSize(totalSpeed) + "/s";
    }

    public void setTotalSpeed(double totalSpeed) {
        this.totalSpeed = totalSpeed;
    }

    public final ObservableBoolean allPause = new ObservableBoolean();

}
