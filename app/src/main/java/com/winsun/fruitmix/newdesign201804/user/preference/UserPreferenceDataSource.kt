package com.winsun.fruitmix.newdesign201804.user.preference

import com.winsun.fruitmix.callback.BaseLoadDataCallback

interface UserPreferenceDataSource {

    fun updateUserPreference(userUUID:String,userPreference: UserPreference)

}