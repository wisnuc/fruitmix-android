package com.winsun.fruitmix.firmware.model

/**
 * Created by Administrator on 2017/12/28.
 */
enum class FirmwareState {
    STARTING, STARTED, STOPPED, STOPPING,NULL
}

enum class NewFirmwareState {
    IDLE, FAILED, READY, DOWNLOADING, REPACKING, VERIFYING,NULL
}

enum class CheckUpdateState {
    PENDING, WORKING,NULL
}


data class Firmware(val currentFirmwareVersion: String, val firmwareState: FirmwareState, val newFirmwareVersion: String,
                    val newFirmwareState: NewFirmwareState,val length:Long,val downloaded:Long,val releaseDate: String, val checkUpdateState: CheckUpdateState)