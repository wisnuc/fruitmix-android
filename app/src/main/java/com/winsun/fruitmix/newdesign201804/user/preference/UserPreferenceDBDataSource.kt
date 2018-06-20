package com.winsun.fruitmix.newdesign201804.user.preference

import android.util.Log
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.db.DBUtils

private const val TAG = "UserPreferenceDB"

class UserPreferenceDBDataSource(val dbUtils: DBUtils):UserPreferenceDataSource {

    fun addUserPreference(userUUID: String, userPreference: UserPreference) {

        val result = dbUtils.insertUserPreference(userUUID, userPreference)

        Log.d(TAG, "addUserPreference result: $result")

    }


    fun getUserPreference(userUUID: String): UserPreference? {
        return dbUtils.getUserPreference(userUUID)
    }

    override fun updateUserPreference(userUUID: String, userPreference: UserPreference) {
        val result = dbUtils.updateUserPreference(userUUID, userPreference)

        Log.d(TAG, "updateUserPreference result: $result")

    }


}