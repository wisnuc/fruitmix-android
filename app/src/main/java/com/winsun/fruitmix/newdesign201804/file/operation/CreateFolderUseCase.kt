package com.winsun.fruitmix.newdesign201804.file.operation

import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseOperateDataCallback
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.http.HttpResponse
import com.winsun.fruitmix.interfaces.BaseView
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.parser.RemoteMkDirParser
import com.winsun.fruitmix.util.SimpleTextWatcher
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.name_edit_text_layout.view.*

class CreateFolderUseCase(val fileDataSource: FileDataSource, val baseView: BaseView,
                          val folderItems: List<AbstractRemoteFile>,
                          val view: View, val handleCreateSucceed: (newFolder: AbstractRemoteFile) -> Unit) {

    fun createFolder(context: Context, parentFolder: AbstractRemoteFile) {

        val nameEditTextLayout = View.inflate(context, R.layout.name_edit_text_layout, null)

        val editText = nameEditTextLayout.nameTextInputEditText
        val inputLayout = nameEditTextLayout.nameTextInputLayout

        val defaultName = context.getString(R.string.no_title_folder)
        editText.setText(defaultName)
        editText.setSelection(defaultName.length)

        val dialog = AlertDialog.Builder(context).setTitle(context.getString(R.string.new_create) + context.getString(R.string.folder))
                .setView(nameEditTextLayout)
                .setPositiveButton(R.string.new_create) { dialog, which ->

                    val folderName = editText.text.toString()

                    doCreateFolder(context, folderName, parentFolder)

                }
                .setNegativeButton(R.string.cancel) { dialog, which -> }
                .create()

        editText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                super.onTextChanged(s, start, before, count)

                val folderName = s.toString()

                when {

                    folderName.isEmpty() -> {

                        inputLayout.isErrorEnabled = true
                        inputLayout.error = context.getString(R.string.name_can_not_be_empty)

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
                    }

                    Util.checkNameFirstWordIsIllegal(folderName) || Util.checkNameIsIllegal(folderName) -> {

                        inputLayout.isErrorEnabled = true
                        inputLayout.error = context.getString(R.string.name_contains_illegal_char)

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

                    }
                    checkFolderNameExist(folderName) -> {

                        inputLayout.isErrorEnabled = true
                        inputLayout.error = context.getString(R.string.name_already_exist)

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

                    }
                    else -> {

                        inputLayout.isErrorEnabled = false

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = true

                    }

                }

            }
        })

        dialog.show()

    }

    private fun checkFolderNameExist(folderName: String): Boolean {

        return folderItems.any {
            it.name == folderName
        }

    }

    private fun doCreateFolder(context: Context, folderName: String, parentFolder: AbstractRemoteFile) {

        baseView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.create)))

        fileDataSource.createFolder(folderName, parentFolder.rootFolderUUID, parentFolder.uuid, object : BaseOperateDataCallback<HttpResponse> {

            override fun onSucceed(data: HttpResponse?, result: OperationResult?) {

                baseView.dismissDialog()

                val parser = RemoteMkDirParser()

                val newFolder = parser.parse(data?.responseData)

                newFolder.rootFolderUUID = parentFolder.rootFolderUUID
                newFolder.parentFolderUUID = parentFolder.uuid

                handleCreateSucceed(newFolder)

            }

            override fun onFail(operationResult: OperationResult?) {

                baseView.dismissDialog()

                SnackbarUtil.showSnackBar(view, Snackbar.LENGTH_SHORT,
                        messageStr = operationResult?.getResultMessage(context)!!)

            }

        })


    }

}