package com.winsun.fruitmix.torrent.data

/**
 * Created by Administrator on 2017/12/13.
 */

enum class DownloadState {
    DOWNLOADING, DOWNLOADED,UNDEFINED
}

data class TorrentDownloadInfo(val hash: String, val downloaded: Long, val downloadedSpeed: Double,
                               val progress: Double, val fileName: String, var state: DownloadState, val isPause: Boolean,
                               val time: Long, val torrentPath: String, val magnetUrl: String) {
    fun getTotalSize(): Double {
        return downloaded / progress
    }

    fun isTorrent(): Boolean {
        return torrentPath.isNotEmpty()
    }

    fun isMagnet(): Boolean {
        return magnetUrl.isNotEmpty()
    }

}

data class TorrentRequestParam(val id: String)