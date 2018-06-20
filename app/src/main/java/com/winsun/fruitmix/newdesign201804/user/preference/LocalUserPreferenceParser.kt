package com.winsun.fruitmix.newdesign201804.user.preference

import android.database.Cursor
import com.winsun.fruitmix.db.DBHelper
import com.winsun.fruitmix.parser.LocalDataParser
import com.winsun.fruitmix.user.User

class LocalUserPreferenceParser:LocalDataParser<UserPreference>{

    override fun parse(cursor: Cursor): UserPreference {

       val sortMode  = cursor.getInt(cursor.getColumnIndex(DBHelper.USER_PREFERENCE_FILE_SORT_MODE))

        val userPreference = UserPreference()

        when(sortMode){
            0 -> userPreference.fileSortPolicy.setCurrentSortMode(SortMode.NAME)
            1 -> userPreference.fileSortPolicy.setCurrentSortMode(SortMode.SIZE)
            2 -> userPreference.fileSortPolicy.setCurrentSortMode(SortMode.CREATE_TIME)
            3 -> userPreference.fileSortPolicy.setCurrentSortMode(SortMode.MODIFY_TIME)
        }

        val sortDirection = cursor.getInt(cursor.getColumnIndex(DBHelper.USER_PREFERENCE_FILE_SORT_DIRECTION))

        when(sortDirection){
            0 -> userPreference.fileSortPolicy.setCurrentSortDirection(SortDirection.POSITIVE)
            1 -> userPreference.fileSortPolicy.setCurrentSortDirection(SortDirection.NEGATIVE)
        }

        val fileViewMode = cursor.getInt(cursor.getColumnIndex(DBHelper.USER_PREFERENCE_FILE_VIEW_MODE))

        when(fileViewMode){
            0 -> userPreference.fileViewModePolicy.setCurrentFileViewMode(FileViewMode.GRID)
            1 -> userPreference.fileViewModePolicy.setCurrentFileViewMode(FileViewMode.LIST)
        }

        return userPreference

    }

}