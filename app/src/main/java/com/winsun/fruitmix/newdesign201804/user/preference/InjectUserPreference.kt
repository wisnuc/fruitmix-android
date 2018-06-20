package com.winsun.fruitmix.newdesign201804.user.preference

import android.content.Context
import com.winsun.fruitmix.db.DBUtils
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl

class InjectUserPreference {

    companion object {

        fun inject(context: Context):UserPreferenceRepository{
            return UserPreferenceRepository(UserPreferenceDBDataSource(DBUtils.getInstance(context)),ThreadManagerImpl.getInstance())
        }

    }

}