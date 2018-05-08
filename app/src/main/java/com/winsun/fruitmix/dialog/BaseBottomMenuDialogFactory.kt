package com.winsun.fruitmix.dialog

import android.app.Dialog
import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.view.View
import com.winsun.fruitmix.model.BottomMenuItem

abstract class BaseBottomMenuDialogFactory(val bottomMenuItems:List<BottomMenuItem>):DialogFactory{

    override fun createDialog(context: Context): Dialog {

        val bottomSheetDialog = BottomSheetDialog(context)

        val bottomSheetView = createBottomSheetView(context, bottomMenuItems)

        bottomSheetDialog.setContentView(bottomSheetView)

        val parent = bottomSheetView.parent as View
        val behavior = BottomSheetBehavior.from(parent)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        for (bottomMenuItem in bottomMenuItems) {
            bottomMenuItem.setDialog(bottomSheetDialog)
        }

        return bottomSheetDialog

    }

    protected abstract fun createBottomSheetView(context: Context, bottomMenuItems:List<BottomMenuItem>):View



}