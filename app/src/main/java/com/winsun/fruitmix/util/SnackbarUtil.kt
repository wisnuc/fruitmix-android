package com.winsun.fruitmix.util

import android.support.design.widget.Snackbar
import android.view.View

object SnackbarUtil {

    private lateinit var snackBar: Snackbar

    fun showSnackBar(view: View, duration: Int,messageResId: Int = 0, messageStr: String = "") {

        if (::snackBar.isInitialized) {

            if (messageResId != 0)
                snackBar.setText(messageResId)
            else
                snackBar.setText(messageStr)

        } else {

            snackBar = if (messageResId != 0)
                Snackbar.make(view, messageResId, duration)
            else
                Snackbar.make(view, messageStr, duration)

        }

        snackBar.show()

    }


}