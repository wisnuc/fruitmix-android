package com.winsun.fruitmix.newdesign201804.equipment.reinitialization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.equipment.add.FINISH_REINITIALIZATION_RESULT_CODE
import com.winsun.fruitmix.newdesign201804.equipment.list.data.FakeEquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.wechatUser.WeChatUserInfoDataSource
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.activity_reinitialization.*

const val REINITIALIZE_EQUIPMENT_NAME_KEY = "reinitialize_equipment_name_key"

class ReinitializationActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbarWhiteStyle(toolbarViewModel)
        setStatusBarToolbarBgColor(R.color.new_design_primary_color)

        val equipmentName = intent.getStringExtra(REINITIALIZE_EQUIPMENT_NAME_KEY)

        val user = WeChatUserInfoDataSource.getUser()

        val reinitializationPresenter = ReinitializationPresenter(reinitializationViewPager.context,
                user, reinitializationViewPager, {

            setResult(FINISH_REINITIALIZATION_RESULT_CODE)
            finish()

        }, equipmentName, FakeEquipmentItemDataSource)

        reinitializationPresenter.init()

    }

    override fun generateContent(root: ViewGroup?): View {

        return LayoutInflater.from(this).inflate(R.layout.activity_reinitialization, root, false)

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.add_equipment)
    }

}
