package com.winsun.fruitmix.newdesign201804.user

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.component.UserNameAvatarContainer
import com.winsun.fruitmix.newdesign201804.wechatUser.WeChatUserInfoDataSource
import kotlinx.android.synthetic.main.activity_user_info.*

class UserInfoActivity : BaseToolbarActivity() {

    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStatusBarToolbarBgColor(R.color.new_design_primary_color)
        setToolbarWhiteStyle(toolbarViewModel)

        val user = WeChatUserInfoDataSource.getUser()

        val userNameAvatarContainer = UserNameAvatarContainer(user_name_avatar_container, user)
        userNameAvatarContainer.initView(this)

        rootView.setOnClickListener {
            userNameAvatarContainer.quitEditState()
        }

    }

    override fun generateContent(root: ViewGroup?): View {
        rootView = LayoutInflater.from(this).inflate(R.layout.activity_user_info, root, false)

        return rootView
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.me)
    }

}
