package com.winsun.fruitmix.newdesign201804.equipment.reinitialization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.equipment.list.data.FakeEquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.wechatUser.WeChatUserInfoDataSource
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.activity_reinitialization.*

const val REINITIAL_EQUIPMENT_NAME_KEY = "equipment_name_key"

class ReinitializationActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val equipmentName = intent.getStringExtra(REINITIAL_EQUIPMENT_NAME_KEY)

        val user = User()

        val weChatTokenUserWrapper = WeChatUserInfoDataSource.weChatTokenUserWrapper

        user.avatar = weChatTokenUserWrapper.avatarUrl
        user.userName = weChatTokenUserWrapper.nickName
        user.defaultAvatar = Util.getUserNameForAvatar(user.userName)

        val reinitializationPresenter = ReinitializationPresenter(reinitializationViewPager.context,
                user, reinitializationViewPager, { finish() }, equipmentName, FakeEquipmentItemDataSource)

        reinitializationPresenter.init()

    }

    override fun generateContent(root: ViewGroup?): View {

        return LayoutInflater.from(this).inflate(R.layout.activity_reinitialization, root, false)

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.add_equipment)
    }

}
