package com.winsun.fruitmix.newdesign201804.equipment.list.data

import android.util.Log
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.equipment.abnormal.data.DiskItemInfo
import com.winsun.fruitmix.newdesign201804.equipment.abnormal.data.DiskState
import com.winsun.fruitmix.newdesign201804.equipment.add.data.DiskMode
import com.winsun.fruitmix.newdesign201804.equipment.list.EquipmentItem
import com.winsun.fruitmix.newdesign201804.equipment.list.EquipmentType
import com.winsun.fruitmix.newdesign201804.equipment.model.*
import com.winsun.fruitmix.newdesign201804.wechatUser.WeChatUserInfoDataSource
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.util.Util

private const val TAG = "FakeEquipmentItemData"

object FakeEquipmentItemDataSource : EquipmentItemDataSource {

    private val baseEquipmentItems: MutableList<BaseEquipmentItem> = mutableListOf()
    private val baseEquipmentItemMaps = mutableMapOf<String, BaseEquipmentItem>()

    private var cacheDirty: Boolean

    init {

        cacheDirty = true

        val user = WeChatUserInfoDataSource.getUser()

        baseEquipmentItems.add(CloudConnectEquipItem("test1", Util.createLocalUUid(), "10.10.9.229"))
        baseEquipmentItems.add(CloudUnConnectedEquipmentItem(user, "WS215i", "test2", Util.createLocalUUid(),
                "10.10.9.77"))

        baseEquipmentItems.add(DiskAbnormalEquipmentItem("test3", Util.createLocalUUid(), DiskMode.SINGLE,
                mutableListOf(DiskItemInfo(DiskState.LOST, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"))))

        baseEquipmentItems.add(DiskAbnormalEquipmentItem("test3", Util.createLocalUUid(), DiskMode.SINGLE,
                mutableListOf(DiskItemInfo(DiskState.NORMAL, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"),
                        DiskItemInfo(DiskState.LOST, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"))))

        baseEquipmentItems.add(DiskAbnormalEquipmentItem("test3-raid1", Util.createLocalUUid(), DiskMode.RAID1,
                mutableListOf(DiskItemInfo(DiskState.NORMAL, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"),
                        DiskItemInfo(DiskState.LOST, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"))))

        baseEquipmentItems.add(DiskAbnormalEquipmentItem("test3-raid1", Util.createLocalUUid(), DiskMode.RAID1,
                mutableListOf(DiskItemInfo(DiskState.NORMAL, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"),
                        DiskItemInfo(DiskState.NEW_AVAILABLE, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"),
                        DiskItemInfo(DiskState.LOST, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"))))

        baseEquipmentItems.add(DiskAbnormalEquipmentItem("test3-raid1", Util.createLocalUUid(), DiskMode.RAID1,
                mutableListOf(DiskItemInfo(DiskState.NORMAL, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"),
                        DiskItemInfo(DiskState.LOST, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"),
                        DiskItemInfo(DiskState.LOST, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"))))

        baseEquipmentItems.add(DiskAbnormalEquipmentItem("test3-raid1", Util.createLocalUUid(), DiskMode.RAID1,
                mutableListOf(DiskItemInfo(DiskState.NORMAL, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"),
                        DiskItemInfo(DiskState.NEW_AVAILABLE, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"),
                        DiskItemInfo(DiskState.LOST, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"),
                        DiskItemInfo(DiskState.LOST, "WD", 2.0 * 1024 * 1024, "WCC3F1EF8S8U"))))

        baseEquipmentItems.add(PowerOffEquipmentItem("test4", Util.createLocalUUid()))
        baseEquipmentItems.add(UnderReviewEquipmentItem("test5", Util.createLocalUUid()))
        baseEquipmentItems.add(OfflineEquipmentItem("test6", Util.createLocalUUid()))

        baseEquipmentItems.forEach {
            baseEquipmentItemMaps[it.uuid] = it
        }

    }

    override fun getEquipmentItems(baseLoadDataCallback: BaseLoadDataCallback<BaseEquipmentItem>) {

        cacheDirty = false

        Log.d(TAG, "cacheDirty: ${isCacheDirty()}")

        baseLoadDataCallback.onSucceed(baseEquipmentItems, OperationSuccess())

    }

    override fun getEquipmentItemInCache(itemUUID: String): BaseEquipmentItem? {
        return baseEquipmentItemMaps[itemUUID]
    }

    override fun addEquipmentItems(baseEquipmentItem: BaseEquipmentItem, baseOperateCallback: BaseOperateCallback) {

        baseEquipmentItems.add(baseEquipmentItem)

        baseEquipmentItemMaps[baseEquipmentItem.uuid] = baseEquipmentItem

        cacheDirty = true

        Log.d(TAG, "cacheDirty: ${isCacheDirty()}")

        baseOperateCallback.onSucceed()

    }

    fun resetCacheDirty() {
        cacheDirty = true
    }


    override fun isCacheDirty(): Boolean {
        return cacheDirty
    }

}