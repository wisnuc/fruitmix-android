package com.winsun.fruitmix.torrent.data

/**
 * Created by Administrator on 2017/12/13.
 */

enum class DownloadState {
    DOWNLOADING, DOWNLOADED
}

data class TorrentDownloadInfo(val hash: String,val downloaded: Long, val downloadedSpeed: Double,
                               val progress: Double, val fileName: String, val state: DownloadState, val isPause: Boolean,
                               val time:Long) {
    fun getTotalSize(): Double {
        return downloaded / progress
    }
}

data class TorrentRequestParam(val id: String)