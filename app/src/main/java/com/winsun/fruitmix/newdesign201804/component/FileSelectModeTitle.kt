package com.winsun.fruitmix.newdesign201804.component

import android.view.View
import com.winsun.fruitmix.R
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import kotlinx.android.synthetic.main.file_select_mode_title.view.*

class FileSelectModeTitle(val view: View,
                                 val exitBtnOnClick: () -> Unit, val moveBtnOnClick: () -> Unit,
                                 val downloadBtnOnClick: () -> Unit, val moreBtnOnClick: () -> Unit) {

    init {

        view.exitBtn.setOnClickListener {
            exitBtnOnClick()
        }

        view.moveBtn.setOnClickListener {
            moveBtnOnClick()
        }

        view.downloadBtn.setOnClickListener {
            downloadBtnOnClick()
        }

        view.moreBtn.setOnClickListener {
            moreBtnOnClick()
        }

    }

    fun notifySelectCountChanged(count: Int) {
        view.selectCountTextView.text = count.toString()
    }

}