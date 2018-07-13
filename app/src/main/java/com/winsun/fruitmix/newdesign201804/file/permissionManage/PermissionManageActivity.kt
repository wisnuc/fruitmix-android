package com.winsun.fruitmix.newdesign201804.file.permissionManage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.volley.toolbox.ImageLoader
import com.winsun.fruitmix.BaseActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.file.data.model.RemotePublicDrive
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.util.getCurrentUserUUID
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.data.InjectSharedFolderDataSource
import com.winsun.fruitmix.newdesign201804.file.sharedFolder.data.SharedFolderDataSource
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.user.User
import com.winsun.fruitmix.user.datasource.InjectUser
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.activity_permission_manage.*
import kotlinx.android.synthetic.main.permission_manage_item.view.*
import kotlinx.android.synthetic.main.permission_manage_title.*
import kotlinx.android.synthetic.main.permission_manage_title.view.*

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

class PermissionManageActivity : BaseActivity() {

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

        setContentView(R.layout.activity_permission_manage)

        Util.setStatusBarColor(this, R.color.new_design_primary_color)

        val mToolbar = permission_manage_title.permissionManageTitleToolbar

        setSupportActionBar(mToolbar)

        val actionBar = supportActionBar

        actionBar?.setDisplayShowTitleEnabled(false)

        mToolbar.setNavigationOnClickListener {
            finish()
        }

        editTextTitle.setText(sharedFolder.name)
        editTextTitle.setSelection(editTextTitle.text.length)

        confirmTv.setOnClickListener {
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

                SnackbarUtil.showSnackBar(recyclerView, Snackbar.LENGTH_SHORT, messageStr = getString(R.string.fail, getString(R.string.get_user)))

            }
        })

        editTextTitle.clearFocus()

    }

    private fun handleToolbarSelectBtnOnClick() {

        val writeList = sharedFolder.writeList

        val isWriteListChanged = writeList.filter {
            selectedUserUUIDs.contains(it)
        }.size != writeList.size

        val isSelectedUserUUIDsChanged = selectedUserUUIDs.filter {
            writeList.contains(it)
        }.size != selectedUserUUIDs.size

        val editTextTitleValue = editTextTitle.text.toString()

        val isNameChanged = editTextTitleValue != sharedFolder.name

        if (isWriteListChanged || isSelectedUserUUIDsChanged) {

            sharedFolderDataSource.updateSharedDiskWriteList(sharedFolder.uuid, selectedUserUUIDs, object : BaseOperateCallback {
                override fun onFail(operationResult: OperationResult?) {
                    SnackbarUtil.showSnackBar(recyclerView, Snackbar.LENGTH_SHORT, messageStr = operationResult!!.getResultMessage(context))
                }

                override fun onSucceed() {
                    setResult(Activity.RESULT_OK)

                    finish()
                }
            })

        } else if (isNameChanged) {

            sharedFolderDataSource.updateSharedDiskName(sharedFolder.uuid, editTextTitleValue, object : BaseOperateCallback {

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

            view?.userCheckBox?.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked)
                    selectedUserUUIDs.add(user.uuid)
                else
                    selectedUserUUIDs.remove(user.uuid)

            }

        }

    }


}
