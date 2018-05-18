package com.winsun.fruitmix.newdesign201804.user

import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.model.operationResult.OperationSuccess
import com.winsun.fruitmix.newdesign201804.user.UserDataSource
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.util.Util
import java.util.*

class FakeUserDataRepository : UserDataSource {

    private var random: Random = Random()

    override fun getUsers(baseLoadDataCallback: BaseLoadDataCallback<User>) {

        val users = mutableListOf<User>()

        val mark = User()
        mark.userName = "Mark"
        Util.setUserDefaultAvatar(mark, random)

        users.add(mark)

        val leo = User()
        leo.userName = "Leo"
        Util.setUserDefaultAvatar(leo, random)

        users.add(leo)

        val jackYang = User()
        jackYang.userName = "Jack Yang"
        Util.setUserDefaultAvatar(jackYang, random)

        users.add(jackYang)

        val lewis = User()
        lewis.userName = "Lewis"
        Util.setUserDefaultAvatar(lewis, random)

        users.add(lewis)

        baseLoadDataCallback.onSucceed(users, OperationSuccess())
    }

}