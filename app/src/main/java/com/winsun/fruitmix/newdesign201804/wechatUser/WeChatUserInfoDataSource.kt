package com.winsun.fruitmix.newdesign201804.wechatUser

import com.winsun.fruitmix.token.WeChatTokenUserWrapper
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.util.Util

object WeChatUserInfoDataSource {

    var weChatTokenUserWrapper: WeChatTokenUserWrapper = WeChatTokenUserWrapper()

    init {

        weChatTokenUserWrapper.avatarUrl = ""
        weChatTokenUserWrapper.nickName = "Test"
        weChatTokenUserWrapper.guid = ""
        weChatTokenUserWrapper.token = ""

    }

    fun getUser():User{

        val user = User()

        user.avatar = weChatTokenUserWrapper.avatarUrl
        user.userName = weChatTokenUserWrapper.nickName
        user.defaultAvatar = Util.getUserNameForAvatar(user.userName)

        return user

    }

}

