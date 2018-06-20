package com.winsun.fruitmix.newdesign201804.equipment.list

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import com.winsun.fruitmix.BaseActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.ActiveView
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.newdesign201804.equipment.list.data.FakeEquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.equipment.list.data.InjectEquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.login.LAN_LOGIN_REQUEST_CODE
import com.winsun.fruitmix.newdesign201804.user.UserInfoActivity
import com.winsun.fruitmix.newdesign201804.user.startUserInfoActivity
import com.winsun.fruitmix.newdesign201804.wechatUser.WeChatUserInfoDataSource
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.activity_equipment_list.*

class EquipmentListActivity : AppCompatActivity(), ActiveView,EquipmentListBaseView {

    private var isDestroy = false

    private lateinit var equipmentListPresenter: EquipmentListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipment_list)

        Util.setStatusBarColor(this, R.color.new_design_primary_color)

        val weChatTokenUserWrapper = WeChatUserInfoDataSource.weChatTokenUserWrapper

        val user = WeChatUserInfoDataSource.getUser()

        userAvatar.setUser(user, InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this))

        userAvatar.setOnClickListener {

            startUserInfoActivity(null,this,userAvatar)

        }

        userName.text = weChatTokenUserWrapper.nickName

        equipmentRecyclerView.layoutManager = GridLayoutManager(this, 2)
        equipmentRecyclerView.itemAnimator = DefaultItemAnimator()

        equipmentListPresenter = EquipmentListPresenter(InjectEquipmentItemDataSource.inject(this),
                weChatTokenUserWrapper.guid, this,this)

        val equipmentListAdapter = equipmentListPresenter.getEquipmentListAdapter()

        equipmentRecyclerView.adapter = equipmentListAdapter

        FakeEquipmentItemDataSource.resetCacheDirty()

    }

    override fun onResume() {
        super.onResume()

        equipmentListPresenter.refreshEquipment()

    }

    override fun onDestroy() {
        super.onDestroy()

        isDestroy = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == LAN_LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK)
            finish()

    }

    override fun isActive(): Boolean {
        return !isDestroy
    }

    override fun getActivity(): Activity {
        return this
    }

}
