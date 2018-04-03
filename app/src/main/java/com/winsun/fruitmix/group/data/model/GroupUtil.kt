package com.winsun.fruitmix.group.data.model

import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.file.data.model.RemoteFile
import com.winsun.fruitmix.mediaModule.model.Media
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.util.Util

/**
 * Created by Administrator on 2018/4/2.
 */

fun checkFilesAllContainsMedias(files: List<AbstractFile>): Boolean {

    val isMedias: Boolean

    val fileSize = files.size

    val mediaSize = files.filter { FileUtil.checkFileIsMedia(it.name) }.size

    isMedias = mediaSize == fileSize

    return isMedias

}

fun convertFilesToMedias(files: List<AbstractFile>): List<Media> {

    val medias: MutableList<Media> = mutableListOf()

    files.map {

        val media = Media()

        val file: RemoteFile = it as RemoteFile

        media.name = file.name
        media.uuid = file.fileHash
        media.size = file.size
        media.width = "200"
        media.height = "200"
        media.isLocal = false
        media.formattedTime = Util.formatDateAndTime(file.time)
        media.type = FileUtil.getMIMEType(media.name)

        medias.add(media)
    }

    return medias
}
