package com.winsun.fruitmix.torrent.viewmodel;

/**
 * Created by Administrator on 2017/12/14.
 */

public class TorrentDownloadedGroupItemViewModel {

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

    private int downloadingItemCount;

    public int getDownloadingItemCount() {
        return downloadingItemCount;
    }

    public void setDownloadingItemCount(int downloadingItemCount) {
        this.downloadingItemCount = downloadingItemCount;
    }
}
