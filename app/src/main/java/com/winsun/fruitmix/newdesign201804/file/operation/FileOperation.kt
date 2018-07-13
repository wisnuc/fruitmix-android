package com.winsun.fruitmix.newdesign201804.file.operation

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
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
import com.winsun.fruitmix.newdesign201804.util.createFileDownloadParam
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
import com.winsun.fruitmix.util.FileTool
import com.winsun.fruitmix.util.FileUtil
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.util.Util

class FileOperation(val currentUserUUID: String, val threadManager: ThreadManager,
                    val transmissionTaskDataSource: TransmissionTaskDataSource,
                    val view: View, val fileDataSource: FileDataSource,
                    val baseView: BaseView, val fileOperationView: FileOperationView,
                    val handleRenameSucceed: (position: Int) -> Unit, val handleDeleteSucceed: (position: Int) -> Unit) {

    fun doShowFileMenuBottomDialog(context: Context, abstractFile: AbstractFile, position: Int) {

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        bottomMenuItems.add(BottomMenuItem(R.drawable.modify_icon, context.getString(R.string.rename), object : BaseAbstractCommand() {
            override fun execute() {
                super.execute()

                rename(context, abstractFile, position)
            }
        }))

        bottomMenuItems.add(BottomMenuItem(R.drawable.black_move, context.getString(R.string.move_to), object : BaseAbstractCommand() {
            override fun execute() {
                super.execute()
                enterMovePage(abstractFile)
            }
        }))

        if (!abstractFile.isFolder) {

            val abstractRemoteFile = abstractFile as AbstractRemoteFile

            val bottomMenuItem = FileWithSwitchBottomItem(R.drawable.offline_available, context.getString(R.string.offline_available),
                    object : BaseAbstractCommand() {}, {

                if (it.isSwitchEnabled()) {
                    deleteDownloadedFile(abstractRemoteFile)
                } else {
                    downloadFile(abstractRemoteFile)
                }

                it.dismissDialog()

            })

            bottomMenuItem.setSwitchEnableState(abstractRemoteFile.getDownloadedFile(currentUserUUID).exists())

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

                deleteFile(context, abstractFile, position)
            }

        }))

        val dialog = FileMenuBottomDialogFactory(abstractFile, bottomMenuItems, {

            FileDetailActivity.start(abstractFile as AbstractRemoteFile, context)

        }).createDialog(context)

        dialog.show()

    }

    private fun deleteDownloadedFile(abstractFile: AbstractRemoteFile) {

        val result = FileTool.getInstance().deleteFile(abstractFile.getDownloadedFile(currentUserUUID).absolutePath)

        if (result) {

            SnackbarUtil.showSnackBar(view, Snackbar.LENGTH_SHORT,
                    messageStr = baseView.getString(R.string.success, baseView.getString(R.string.delete_file)))

        } else {
            SnackbarUtil.showSnackBar(view, Snackbar.LENGTH_SHORT,
                    messageStr = baseView.getString(R.string.fail, baseView.getString(R.string.delete_file)))
        }

    }

    private fun downloadFile(abstractRemoteFile: AbstractRemoteFile) {
        val fileDownloadParam = abstractRemoteFile.createFileDownloadParam()

        val downloadTask = DownloadTask(Util.createLocalUUid(), currentUserUUID, abstractRemoteFile, fileDataSource, fileDownloadParam,
                threadManager)

        transmissionTaskDataSource.addTransmissionTask(downloadTask)

        SnackbarUtil.showSnackBar(view, Snackbar.LENGTH_SHORT, R.string.add_task_hint)
    }

    fun openFileAfterOnClick(abstractFile: AbstractRemoteFile) {

        val downloadedFile = abstractFile.getDownloadedFile(currentUserUUID)

        if (downloadedFile.exists())

            FileUtil.openDownloadedFile(fileOperationView.getActivity(), downloadedFile)
        else {

            val fileDownloadParam = FileFromStationFolderDownloadParam(abstractFile.uuid,
                    abstractFile.parentFolderUUID, abstractFile.rootFolderUUID, abstractFile.name)

            val task = DownloadTask(Util.createLocalUUid(), currentUserUUID, abstractFile, fileDataSource, fileDownloadParam,
                    threadManager)

            task.init()

            val taskProgressDialog = ProgressDialog(fileOperationView.getActivity())
            taskProgressDialog.setTitle(fileOperationView.getActivity().getString(R.string.downloading))
            taskProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            taskProgressDialog.isIndeterminate = false

            taskProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, fileOperationView.getActivity().getString(R.string.cancel), { dialog, which ->

                task.deleteTask()

                dialog.dismiss()

            })

            taskProgressDialog.setCancelable(false)
            taskProgressDialog.max = 100

            task.registerObserver(object : TaskStateObserver {
                override fun notifyStateChanged(currentState: TaskState, preState: TaskState) {

                    if (currentState is StartingTaskState) {

                        taskProgressDialog.progress = currentState.progress

                        taskProgressDialog.setProgressNumberFormat(currentState.speed)

                    } else if (currentState is FinishTaskState) {

                        taskProgressDialog.dismiss()

                        FileUtil.openDownloadedFile(fileOperationView.getActivity(), abstractFile.getDownloadedFile(currentUserUUID))

                        task.unregisterObserver(this)

                    }

                }
            })

            task.startTask()

            taskProgressDialog.show()

        }
    }

    private fun rename(context: Context, abstractFile: AbstractFile, position: Int) {

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
                        doRename(context, oldName, newName, abstractFile, position)
                    }

                })
                .setNegativeButton(R.string.cancel, { dialog, which -> })
                .create().show()

    }

    private fun doRename(context: Context, oldName: String, newName: String, abstractFile: AbstractFile, position: Int) {

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

    private fun deleteFile(context: Context, abstractFile: AbstractFile, position: Int) {

        AlertDialog.Builder(context).setTitle(R.string.delete_or_not)
                .setPositiveButton(R.string.delete_text,
                        { dialog, which ->

                            doDeleteFile(context, abstractFile, position)
                        })
                .setNegativeButton(R.string.cancel, null)
                .create().show()

    }

    private fun doDeleteFile(context: Context, abstractFile: AbstractFile, position: Int) {

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