package com.winsun.fruitmix.newdesign201804.user

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.user.User

interface UserDataSource {

    fun getUsers(baseLoadDataCallback: BaseLoadDataCallback<User>)

}