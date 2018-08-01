package com.winsun.fruitmix.newdesign201804.file.operation

import android.content.Context
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
import com.winsun.fruitmix.util.SnackbarUtil

class CreateFolderUseCase(val fileDataSource: FileDataSource, val baseView: BaseView,
                          val folderItems: List<AbstractRemoteFile>,
                          val view: View, val handleCreateSucceed: (newFolder: AbstractRemoteFile) -> Unit) {

    fun createFolder(context: Context, parentFolder: AbstractRemoteFile) {

        val editText = EditText(context)

        editText.hint = context.getString(R.string.no_title_folder)

        AlertDialog.Builder(context).setTitle(context.getString(R.string.new_create) + context.getString(R.string.folder))
                .setView(editText)
                .setPositiveButton(R.string.new_create) { dialog, which ->

                    var folderName = editText.text.toString()

                    if (folderName.isEmpty())
                        folderName = editText.hint.toString()

                    if (checkFolderNameExist(folderName)) {
                        SnackbarUtil.showSnackBar(view, Snackbar.LENGTH_SHORT,
                               R.string.name_already_exist)
                    } else
                        doCreateFolder(context, folderName, parentFolder)

                }
                .setNegativeButton(R.string.cancel) { dialog, which -> }
                .create().show()

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