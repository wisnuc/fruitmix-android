package com.winsun.fruitmix.newdesign201804.equipment.reinitialization.presenter

import android.content.Context
import com.winsun.fruitmix.user.BaseOperateUserPresenter
import com.winsun.fruitmix.user.OperateUserViewModel

class SetPasswordPresenter : BaseOperateUserPresenter(){

    public override fun checkOperateUserPassword(context: Context?, newPassword: String?, confirmPassword: String?, operateUserViewModel: OperateUserViewModel?): Boolean {
        return super.checkOperateUserPassword(context, newPassword, confirmPassword, operateUserViewModel)
    }

}