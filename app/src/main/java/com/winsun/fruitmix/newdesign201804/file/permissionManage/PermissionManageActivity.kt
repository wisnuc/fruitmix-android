package com.winsun.fruitmix.newdesign201804.file.permissionManage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.toolbox.ImageLoader
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.file.data.model.RemotePublicDrive
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.component.getCurrentUserUUID
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.data.InjectSharedFolderDataSource
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.data.SharedFolderDataSource
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.user.datasource.InjectUser
import com.winsun.fruitmix.util.SnackbarUtil
import kotlinx.android.synthetic.main.activity_permission_manage.*
import kotlinx.android.synthetic.main.permission_manage_item.view.*

object ManageSharedFolder {

    private lateinit var mSharedFolder: RemotePublicDrive

    fun saveSharedFolder(sharedFolder: RemotePublicDrive) {
        mSharedFolder = sharedFolder
    }

    fun getSharedFolder(): RemotePublicDrive {
        return mSharedFolder
    }

}

const val PERMISSION_MANAGE_REQUEST_CODE = 0x1001

class PermissionManageActivity : BaseToolbarActivity() {

    companion object {

        fun start(activity: Activity, sharedFolder: RemotePublicDrive) {

            ManageSharedFolder.saveSharedFolder(sharedFolder)

            activity.startActivityForResult(Intent(activity, PermissionManageActivity::class.java), PERMISSION_MANAGE_REQUEST_CODE)

        }

    }

    private lateinit var sharedFolder: RemotePublicDrive

    private val selectedUserUUIDs = mutableListOf<String>()

    private lateinit var sharedFolderDataSource: SharedFolderDataSource

    private val context = this

    override fun onCreate(savedInstanceState: Bundle?) {

        sharedFolder = ManageSharedFolder.getSharedFolder()

        super.onCreate(savedInstanceState)

        setToolbarWhiteStyle(toolbarViewModel)
        setStatusBarToolbarBgColor(R.color.new_design_primary_color)

        toolbarViewModel.navigationIconResId.set(R.drawable.white_clear)

        toolbarViewModel.showSelect.set(true)

        toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(this, R.color.eighty_seven_percent_white))

        toolbarViewModel.selectTextEnable.set(true)

        toolbarViewModel.selectTextResID.set(R.string.confirm)

        toolbarViewModel.setToolbarSelectBtnOnClickListener {
            handleToolbarSelectBtnOnClick()
        }

        sharedFolderDataSource = InjectSharedFolderDataSource.inject(this)

        val imageLoader = InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this)

        val permissionManageAdapter = PermissionManageAdapter(imageLoader)

        recyclerView.adapter = permissionManageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userDataSource = InjectUser.provideRepository(this)

        selectedUserUUIDs.addAll(sharedFolder.writeList)

        userDataSource.getUsers(getCurrentUserUUID(), object : BaseLoadDataCallback<User> {
            override fun onSucceed(data: MutableList<User>?, operationResult: OperationResult?) {

                permissionManageAdapter.setItemList(data)
                permissionManageAdapter.notifyDataSetChanged()

            }

            override fun onFail(operationResult: OperationResult?) {

                SnackbarUtil.showSnackBar(recyclerView, Snackbar.LENGTH_SHORT, messageStr = "获取用户失败")

            }
        })

    }

    override fun generateContent(root: ViewGroup?): View {
        return LayoutInflater.from(root?.context).inflate(R.layout.activity_permission_manage, root, false)
    }

    override fun getToolbarTitle(): String {
        return sharedFolder.name
    }

    private fun handleToolbarSelectBtnOnClick() {

        val writeList = sharedFolder.writeList

        val isChanged = writeList.filter {
            selectedUserUUIDs.contains(it)
        }.size != writeList.size

        if (isChanged) {

            sharedFolderDataSource.updateSharedDiskWriteList(sharedFolder.uuid, selectedUserUUIDs, object : BaseOperateCallback {
                override fun onFail(operationResult: OperationResult?) {
                    SnackbarUtil.showSnackBar(recyclerView, Snackbar.LENGTH_SHORT, messageStr = operationResult!!.getResultMessage(context))
                }

                override fun onSucceed() {
                    setResult(Activity.RESULT_OK)

                    finish()
                }
            })

        } else {

            finish()

        }

    }


    private inner class PermissionManageAdapter(val imageLoader: ImageLoader) : BaseRecyclerViewAdapter<SimpleViewHolder, User>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            return SimpleViewHolder(LayoutInflater.from(parent?.context)
                    .inflate(R.layout.permission_manage_item, parent, false))

        }

        override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

            val view = holder?.itemView
            val user = mItemList[position]

            view?.userAvatar5?.setUser(user, imageLoader)
            view?.userName?.text = user.userName

            view?.userCheckBox?.isChecked = selectedUserUUIDs.contains(user.uuid)

        }

    }


}
