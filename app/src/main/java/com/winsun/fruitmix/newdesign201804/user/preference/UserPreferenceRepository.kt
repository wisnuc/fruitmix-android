package com.winsun.fruitmix.newdesign201804.user.preference

import com.winsun.fruitmix.BaseDataRepository
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.user.User

class UserPreferenceRepository(val userPreferenceDBDataSource: UserPreferenceDBDataSource,threadManager: ThreadManager)
    :BaseDataRepository(threadManager),UserPreferenceDataSource{

    fun init(userUUID: String){

        mThreadManager.runOnCacheThread {

            var userPreference = userPreferenceDBDataSource.getUserPreference(userUUID)

            if(userPreference == null){
                userPreference = UserPreference()
                userPreferenceDBDataSource.addUserPreference(userUUID,userPreference)
            }

            val userPreferenceContainer = UserPreferenceContainer

            userPreferenceContainer.userPreference = userPreference

        }

    }


    override fun updateUserPreference(userUUID: String, userPreference: UserPreference) {
        mThreadManager.runOnCacheThread {
            userPreferenceDBDataSource.updateUserPreference(userUUID,userPreference)
        }
    }

}