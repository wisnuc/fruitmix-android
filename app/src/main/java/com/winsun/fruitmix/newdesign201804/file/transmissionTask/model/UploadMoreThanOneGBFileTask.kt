package com.winsun.fruitmix.newdesign201804.file.transmissionTask.model

import android.util.Log
import com.winsun.fruitmix.file.data.model.AbstractLocalFile
import com.winsun.fruitmix.file.data.station.StationFileRepository
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithFile
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.data.FileUploadParam
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.util.Util

private const val TAG = "UploadMoreThanOneGBFile"

class UploadMoreThanOneGBFileTask(val stationFileRepository: StationFileRepository, uuid: String, createUserUUID: String,
                                  private val abstractLocalFile: AbstractLocalFile, fileDataSource: FileDataSource,
                                  fileUploadParam: FileUploadParam, threadManager: ThreadManager)
    : UploadTask(uuid, createUserUUID, abstractLocalFile, fileDataSource, fileUploadParam, threadManager) {

    private val fingerPrints = mutableListOf<String>()

    override fun executeTask() {

        val uploadMoreThanOneGBFileCallable = OperateFileCallable{
            calcFingerPrint()
        }

        future = threadManager.runOnCacheThread(uploadMoreThanOneGBFileCallable)

    }

    private fun calcFingerPrint() {

        var currentCalcSize = 0L

        val totalFileSize = abstractLocalFile.size

        val oneGSize = 1024 * 1024 * 1024L

        val sha256s = mutableListOf<ByteArray>()

        while (totalFileSize - currentCalcSize != 0L) {

            val offset = if (totalFileSize - currentCalcSize >= oneGSize) {
                oneGSize
            } else {
                totalFileSize - currentCalcSize
            }

            val sha256 = Util.calcSHA256OfFileReturnByte(abstractLocalFile.path, currentCalcSize, offset)

            currentCalcSize += if(totalFileSize - currentCalcSize >= oneGSize){
                oneGSize
            }else{
                totalFileSize - currentCalcSize
            }

            sha256s.add(sha256)

        }

        var tempFingerPrintByteArray = ByteArray(0)
        var tempFingerPrint: String

        for (i in 0 until sha256s.size) {

            if (i == 0) {

                tempFingerPrintByteArray = sha256s[i]
                tempFingerPrint = Util.covertByteToString(sha256s[i])

                Log.d(TAG, "file hash0: $tempFingerPrint fingerPrint:$tempFingerPrint")

            } else {

                val currentItemByteArray = sha256s[i]

                val newByteArray = ByteArray(tempFingerPrintByteArray.size + currentItemByteArray.size)

                System.arraycopy(tempFingerPrintByteArray, 0, newByteArray, 0, tempFingerPrintByteArray.size)
                System.arraycopy(currentItemByteArray, 0, newByteArray, tempFingerPrintByteArray.size, currentItemByteArray.size)

                tempFingerPrintByteArray = Util.calcSHA256OfFileReturnByte(newByteArray)
                tempFingerPrint = Util.covertByteToString(tempFingerPrintByteArray)

                Log.d(TAG, "file hash$i: ${Util.covertByteToString(currentItemByteArray)} fingerPrint:$tempFingerPrint")

            }

            fingerPrints.add(tempFingerPrint)

        }

    }

    private fun checkFileExist() {

        val operationSuccessWithFile = stationFileRepository.getFileWithoutCreateNewThread(fileUploadParam.driveUUID, fileUploadParam.dirUUID, "")

        val fileLists = (operationSuccessWithFile as OperationSuccessWithFile).list

        val findFile = fileLists.find {
            it.name == abstractLocalFile.name
        }

        if (findFile != null) {

            var position = -1
            for (i in 0 until fingerPrints.size) {
                if (fingerPrints[i] == findFile.uuid) {
                    position = i
                    break
                }
            }

            if (position == fingerPrints.size - 1) {
                //TODO: file already uploaded
            } else if (position == -1) {
                //TODO:file not exist,new file and append
            } else {

                //TODO:directly append file

            }

        }


    }


}