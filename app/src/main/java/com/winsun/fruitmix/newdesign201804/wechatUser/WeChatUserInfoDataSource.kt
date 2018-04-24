package com.winsun.fruitmix.newdesign201804.wechatUser

import com.winsun.fruitmix.token.WeChatTokenUserWrapper
import com.winsun.fruitmix.user.User

object WeChatUserInfoDataSource {

    var weChatTokenUserWrapper: WeChatTokenUserWrapper = WeChatTokenUserWrapper()

    init {

        weChatTokenUserWrapper.avatarUrl = ""
        weChatTokenUserWrapper.nickName = "Test"
        weChatTokenUserWrapper.guid = ""
        weChatTokenUserWrapper.token = ""

    }

}

