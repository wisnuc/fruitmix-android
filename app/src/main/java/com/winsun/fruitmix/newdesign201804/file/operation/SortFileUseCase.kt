package com.winsun.fruitmix.newdesign201804.file.operation

import android.content.Context
import com.winsun.fruitmix.R
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.dialog.BottomMenuListDialogFactory
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.newdesign201804.file.list.presenter.ItemContent
import com.winsun.fruitmix.newdesign201804.user.preference.*

class SortFileUseCase(val userPreference: UserPreference, val currentUserUUID: String,
                      val userPreferenceDataSource: UserPreferenceDataSource,
                      val refreshDataAfterSortPolicyChanged: () -> Unit) {

    private val sortPolicy: FileSortPolicy = userPreference.fileSortPolicy

    fun showSortBottomDialog(context: Context, saveIntoDB: Boolean) {

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        val bottomMenuItem = BottomMenuItem(
                getSortModeIconResID(SortMode.NAME),
                context.getString(R.string.sort_by_name), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                sortPolicy.setCurrentSortMode(SortMode.NAME)

                if (saveIntoDB)
                    userPreferenceDataSource.updateUserPreference(currentUserUUID, userPreference)

                refreshDataAfterSortPolicyChanged()
            }

        })

        bottomMenuItem.rightResID = getBottomMenuRightResID(SortMode.NAME)

        bottomMenuItems.add(bottomMenuItem)

        val sortByModifyTimeItem = BottomMenuItem(getSortModeIconResID(SortMode.MODIFY_TIME), context.getString(R.string.sort_by_modify_time), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                sortPolicy.setCurrentSortMode(SortMode.MODIFY_TIME)

                if (saveIntoDB)
                    userPreferenceDataSource.updateUserPreference(currentUserUUID, userPreference)

                refreshDataAfterSortPolicyChanged()

            }

        })

        sortByModifyTimeItem.rightResID = getBottomMenuRightResID(SortMode.MODIFY_TIME)
        bottomMenuItems.add(sortByModifyTimeItem)

        val sortByCreateTimeItem = BottomMenuItem(getSortModeIconResID(SortMode.CREATE_TIME), context.getString(R.string.sort_by_create_time), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                sortPolicy.setCurrentSortMode(SortMode.CREATE_TIME)

                if (saveIntoDB)
                    userPreferenceDataSource.updateUserPreference(currentUserUUID, userPreference)

                refreshDataAfterSortPolicyChanged()

            }

        })

        sortByCreateTimeItem.rightResID = getBottomMenuRightResID(SortMode.CREATE_TIME)
        bottomMenuItems.add(sortByCreateTimeItem)

        val sortByCapacityItem = BottomMenuItem(getSortModeIconResID(SortMode.SIZE), context.getString(R.string.sort_by_capacity), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                sortPolicy.setCurrentSortMode(SortMode.SIZE)

                if (saveIntoDB)
                    userPreferenceDataSource.updateUserPreference(currentUserUUID, userPreference)

                refreshDataAfterSortPolicyChanged()

            }

        })

        sortByCapacityItem.rightResID = getBottomMenuRightResID(SortMode.SIZE)
        bottomMenuItems.add(sortByCapacityItem)

        BottomMenuListDialogFactory(bottomMenuItems).createDialog(context).show()

    }

    private fun getSortModeIconResID(sortMode: SortMode): Int {
        return if (sortPolicy.getCurrentSortMode() == sortMode) {

            if (sortPolicy.getCurrentSortDirection() == SortDirection.POSITIVE)
                R.drawable.black_up_arrow
            else
                R.drawable.black_down_arrow

        } else 0
    }

    private fun getBottomMenuRightResID(sortMode: SortMode): Int {
        return if (sortPolicy.getCurrentSortMode() == sortMode)
            R.drawable.green_done
        else
            0
    }

    fun getSortComparator() :Comparator<ItemContent>{
        return Comparator { o1, o2 ->
            when (sortPolicy.getCurrentSortMode()) {
                SortMode.NAME -> {
                    return@Comparator o1.getFileName().compareTo(o2.getFileName())
                }
                SortMode.SIZE -> {

                    when {
                        o1.getFileSize() > o2.getFileSize() -> return@Comparator 1
                        o1.getFileSize() < o2.getFileSize() -> return@Comparator -1
                        else -> return@Comparator 0
                    }

                }
                SortMode.CREATE_TIME -> {

                    when {
                        o1.getFileModifyTime() > o2.getFileModifyTime() -> return@Comparator 1
                        o1.getFileModifyTime() < o2.getFileModifyTime() -> return@Comparator -1
                        else -> return@Comparator 0
                    }

                }
                SortMode.MODIFY_TIME -> {

                    when {
                        o1.getFileModifyTime() > o2.getFileModifyTime() -> return@Comparator 1
                        o1.getFileModifyTime() < o2.getFileModifyTime() -> return@Comparator -1
                        else -> return@Comparator 0
                    }

                }
            }

        }
    }


}