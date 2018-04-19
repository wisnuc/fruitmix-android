package com.winsun.fruitmix.newdesign201804.equipment.list

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.ActiveView
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.newdesign201804.wechatUser.WeChatUserInfoDataSource
import com.winsun.fruitmix.stations.InjectStation
import com.winsun.fruitmix.user.User
import kotlinx.android.synthetic.main.activity_equipment_list.*

class EquipmentListActivity : AppCompatActivity(),ActiveView {

    private var isDestroy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipment_list)

        val user = User()

        val weChatTokenUserWrapper = WeChatUserInfoDataSource.weChatTokenUserWrapper

        user.avatar = weChatTokenUserWrapper.avatarUrl
        user.userName = weChatTokenUserWrapper.nickName

        userAvatar.setUser(user, InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this))

        equipmentRecyclerView.layoutManager = GridLayoutManager(this, 2)
        equipmentRecyclerView.itemAnimator = DefaultItemAnimator()

        val equipmentListPresenter = EquipmentListPresenter(InjectStation.provideStationDataSource(this),
                weChatTokenUserWrapper.guid,this)

        val equipmentListAdapter = equipmentListPresenter.getEquipmentListAdapter()

        equipmentRecyclerView.adapter = equipmentListAdapter

        equipmentListPresenter.refreshEquipment()

    }

    override fun onDestroy() {
        super.onDestroy()

        isDestroy = true
    }

    override fun isActive(): Boolean {
        return !isDestroy
    }

}
