package com.winsun.fruitmix.torrent.data

/**
 * Created by Administrator on 2017/12/13.
 */

enum class DownloadState {
    DOWNLOADING, PAUSE,DOWNLOADED
}

data class TorrentDownloadInfo(val hash: String, val timeRemaining: Double, val downloaded: Long, val downloadedSpeed: Long,
                               val peerNum: Int, val fileName: String, val state: DownloadState, val isPause: Boolean)

data class TorrentRequestParam(val id:String)