package com.winsun.fruitmix.torrent.viewmodel;

/**
 * Created by Administrator on 2017/12/14.
 */

public class TorrentDownloadedGroupItemViewModel {

    private int itemCount;

    public String getItemCount() {
        return "(" + itemCount + ")";
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

}
