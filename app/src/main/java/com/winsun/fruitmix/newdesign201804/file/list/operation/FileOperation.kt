package com.winsun.fruitmix.newdesign201804.file.list.operation

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.file.data.download.param.FileFromStationFolderDownloadParam
import com.winsun.fruitmix.file.data.model.AbstractFile
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile
import com.winsun.fruitmix.interfaces.BaseView
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.model.DivideBottomMenuItem
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.component.createFileDownloadParam
import com.winsun.fruitmix.newdesign201804.file.detail.FILE_UUID_KEY
import com.winsun.fruitmix.newdesign201804.file.detail.FileDetailActivity
import com.winsun.fruitmix.newdesign201804.file.list.data.FileDataSource
import com.winsun.fruitmix.newdesign201804.file.list.data.FileMenuBottomDialogFactory
import com.winsun.fruitmix.newdesign201804.file.list.data.FileWithSwitchBottomItem
import com.winsun.fruitmix.newdesign201804.file.move.FILE_OPERATE_COPY
import com.winsun.fruitmix.newdesign201804.file.move.FILE_OPERATE_MOVE
import com.winsun.fruitmix.newdesign201804.file.move.FILE_OPERATE_SHARE_TO_SHARED_FOLDER
import com.winsun.fruitmix.newdesign201804.file.move.MoveFileActivity
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.data.TransmissionTaskDataSource
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.*
import com.winsun.fruitmix.thread.manage.ThreadManager
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.util.Util

class FileOperation(val currentUserUUID: String, val threadManager: ThreadManager,
                    val transmissionTaskDataSource: TransmissionTaskDataSource,
                    val view: View, val fileDataSource: FileDataSource,
                    val context: Context, val baseView: BaseView,
                    val fileOperationView: FileOperationView,
                    val handleRenameSucceed: (position: Int) -> Unit, val handleDeleteSucceed: (position: Int) -> Unit) {

    fun doShowFileMenuBottomDialog(context: Context, abstractFile: AbstractFile, position: Int) {

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        bottomMenuItems.add(BottomMenuItem(R.drawable.modify_icon, context.getString(R.string.rename), object : BaseAbstractCommand() {
            override fun execute() {
                super.execute()

                rename(abstractFile, position)
            }
        }))

        bottomMenuItems.add(BottomMenuItem(R.drawable.black_move, context.getString(R.string.move_to), object : BaseAbstractCommand() {
            override fun execute() {
                super.execute()
                enterMovePage(abstractFile)
            }
        }))

        if (!abstractFile.isFolder) {

            val bottomMenuItem = FileWithSwitchBottomItem(R.drawable.offline_available, context.getString(R.string.offline_available),
                    object : BaseAbstractCommand() {}, {

                val abstractRemoteFile = abstractFile as AbstractRemoteFile

                val fileDownloadParam = abstractRemoteFile.createFileDownloadParam()

                val downloadTask = DownloadTask(Util.createLocalUUid(), currentUserUUID, abstractRemoteFile, fileDataSource, fileDownloadParam,
                        currentUserUUID, threadManager)

                transmissionTaskDataSource.addTransmissionTask(downloadTask)

                SnackbarUtil.showSnackBar(view, Snackbar.LENGTH_SHORT, R.string.add_task_hint)

                it.dismissDialog()

            })

            bottomMenuItems.add(bottomMenuItem)

            bottomMenuItems.add(BottomMenuItem(R.drawable.open_with_other_app, context.getString(R.string.open_with_other_app), object : BaseAbstractCommand() {

                override fun execute() {
                    super.execute()

                    openFileAfterOnClick(abstractFile as AbstractRemoteFile)

                }

            }))

        }

        bottomMenuItems.add(DivideBottomMenuItem())

        if (!abstractFile.isFolder) {

            bottomMenuItems.add(BottomMenuItem(R.drawable.make_a_copy, context.getString(R.string.make_a_copy), object : BaseAbstractCommand() {

                override fun execute() {
                    super.execute()

                }

            }))

            bottomMenuItems.add(BottomMenuItem(R.drawable.edit_tag, context.getString(R.string.edit_tag), object : BaseAbstractCommand() {}))

        }

        bottomMenuItems.add(BottomMenuItem(R.drawable.share_to_shared_folder, context.getString(R.string.share_to_shared_folder), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                enterShareToSharedFolderPage(abstractFile)
            }

        }))

        bottomMenuItems.add(BottomMenuItem(R.drawable.copy_to, context.getString(R.string.copy_to), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                enterCopyPage(abstractFile)
            }

        }))

        bottomMenuItems.add(BottomMenuItem(R.drawable.delete_download_task, context.getString(R.string.delete_text), object : BaseAbstractCommand() {

            override fun execute() {
                super.execute()

                deleteFile(abstractFile, position)
            }

        }))

        val dialog = FileMenuBottomDialogFactory(abstractFile, bottomMenuItems, {

            val intent = Intent(context, FileDetailActivity::class.java)

            val abstractRemoteFile = it as AbstractRemoteFile
            intent.putExtra(FILE_UUID_KEY, abstractRemoteFile.uuid)

            //TODO: check pass uuid

            context.startActivity(intent)

        }).createDialog(context)

        dialog.show()

    }

    private fun openFileAfterOnClick(abstractFile: AbstractRemoteFile) {
        if (FileUtil.checkFileExistInDownloadFolder(abstractFile.name))
            FileUtil.openAbstractRemoteFile(context, abstractFile.name)
        else {

            val fileDownloadParam = FileFromStationFolderDownloadParam(abstractFile.uuid,
                    abstractFile.parentFolderUUID, abstractFile.rootFolderUUID, abstractFile.name)

            val task = DownloadTask(Util.createLocalUUid(), currentUserUUID, abstractFile, fileDataSource, fileDownloadParam,
                    currentUserUUID, threadManager)

            task.init()

            val taskProgressDialog = ProgressDialog(context)
            taskProgressDialog.setTitle(context.getString(R.string.downloading))
            taskProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            taskProgressDialog.isIndeterminate = false

            taskProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), { dialog, which ->

                task.cancelTask()

                dialog.dismiss()

            })

            taskProgressDialog.setCancelable(false)
            taskProgressDialog.max = 100

            task.registerObserver(object : TaskStateObserver {
                override fun notifyStateChanged(currentState: TaskState) {

                    if (currentState is StartingTaskState) {

                        taskProgressDialog.progress = currentState.progress

                        taskProgressDialog.setProgressNumberFormat(currentState.speed)

                    } else if (currentState is FinishTaskState) {

                        taskProgressDialog.dismiss()

                        FileUtil.openAbstractRemoteFile(context, abstractFile.name)

                        task.unregisterObserver(this)

                    }

                }
            })

            task.startTask()

            taskProgressDialog.show()

        }
    }

    private fun rename(abstractFile: AbstractFile, position: Int) {

        val editText = EditText(context)
        editText.hint = abstractFile.name

        AlertDialog.Builder(context).setTitle(context.getString(R.string.rename))
                .setView(editText)
                .setPositiveButton(R.string.rename, { dialog, which ->

                    val oldName = abstractFile.name
                    val newName = editText.text.toString()

                    if (newName.isEmpty()) {
                        SnackbarUtil.showSnackBar(view, Snackbar.LENGTH_SHORT, R.string.new_name_empty_hint)
                    } else if (oldName != newName) {
                        doRename(oldName, newName, abstractFile, position)
                    }

                })
                .setNegativeButton(R.string.cancel, { dialog, which -> })
                .create().show()

    }

    private fun doRename(oldName: String, newName: String, abstractFile: AbstractFile, position: Int) {

        baseView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.rename)))

        val abstractRemoteFile = abstractFile as AbstractRemoteFile

        fileDataSource.renameFile(oldName, newName, abstractRemoteFile.rootFolderUUID, abstractRemoteFile.parentFolderUUID, object : BaseOperateCallback {

            override fun onFail(operationResult: OperationResult?) {

                baseView.dismissDialog()
                SnackbarUtil.showSnackBar(view, Snackbar.LENGTH_SHORT,
                        messageStr = operationResult!!.getResultMessage(context))
            }

            override fun onSucceed() {

                baseView.dismissDialog()

                abstractFile.name = newName

                handleRenameSucceed(position)

            }

        })

    }


    private fun enterMovePage(abstractFile: AbstractFile) {

        val abstractFiles = mutableListOf(abstractFile)

        MoveFileActivity.start(fileOperationView.getActivity(), abstractFiles, FILE_OPERATE_MOVE)

    }

    private fun enterCopyPage(abstractFile: AbstractFile) {

        val abstractFiles = mutableListOf(abstractFile)

        MoveFileActivity.start(fileOperationView.getActivity(), abstractFiles, FILE_OPERATE_COPY)

    }

    private fun enterShareToSharedFolderPage(abstractFile: AbstractFile) {

        val abstractFiles = mutableListOf(abstractFile)

        MoveFileActivity.start(fileOperationView.getActivity(), abstractFiles, FILE_OPERATE_SHARE_TO_SHARED_FOLDER)

    }

    private fun deleteFile(abstractFile: AbstractFile, position: Int) {

        AlertDialog.Builder(context).setTitle(R.string.delete_or_not)
                .setPositiveButton(R.string.delete_text,
                        { dialog, which ->

                            doDeleteFile(abstractFile, position)
                        })
                .setNegativeButton(R.string.cancel, null)
                .create().show()

    }

    private fun doDeleteFile(abstractFile: AbstractFile, position: Int) {

        baseView.showProgressDialog(context.getString(R.string.operating_title, context.getString(R.string.delete_text)))

        val abstractRemoteFile = abstractFile as AbstractRemoteFile

        fileDataSource.deleteFile(abstractFile.name, abstractRemoteFile.rootFolderUUID, abstractRemoteFile.parentFolderUUID, object : BaseOperateCallback {

            override fun onFail(operationResult: OperationResult?) {

                baseView.dismissDialog()
                SnackbarUtil.showSnackBar(view, Snackbar.LENGTH_SHORT,
                        messageStr = operationResult!!.getResultMessage(context))
            }

            override fun onSucceed() {

                baseView.dismissDialog()

                handleDeleteSucceed(position)

            }

        })

    }

}