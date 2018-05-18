package com.winsun.fruitmix.newdesign201804.file.permissionManage

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.toolbox.ImageLoader
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.user.FakeUserDataRepository
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.user.User
import kotlinx.android.synthetic.main.activity_permission_manage.*
import kotlinx.android.synthetic.main.permission_manage_item.view.*

class PermissionManageActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbarWhiteStyle(toolbarViewModel)
        setStatusBarToolbarBgColor(R.color.new_design_primary_color)

        toolbarViewModel.navigationIconResId.set(R.drawable.white_clear)

        toolbarViewModel.showSelect.set(true)

        toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(this, R.color.twenty_six_percent_white))

        toolbarViewModel.selectTextEnable.set(false)

        toolbarViewModel.selectTextResID.set(R.string.confirm)

        toolbarViewModel.setToolbarSelectBtnOnClickListener {

        }

        val imageLoader = InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this)

        val permissionManageAdapter = PermissionManageAdapter(imageLoader)

        recyclerView.adapter = permissionManageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userDataSource = FakeUserDataRepository()

        userDataSource.getUsers(object : BaseLoadDataCallback<User> {
            override fun onSucceed(data: MutableList<User>?, operationResult: OperationResult?) {

                permissionManageAdapter.setItemList(data)
                permissionManageAdapter.notifyDataSetChanged()

            }

            override fun onFail(operationResult: OperationResult?) {

            }
        })

    }

    override fun generateContent(root: ViewGroup?): View {
        return LayoutInflater.from(root?.context).inflate(R.layout.activity_permission_manage, root, false)
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.permission_manage)
    }

    private class PermissionManageAdapter(val imageLoader: ImageLoader) : BaseRecyclerViewAdapter<SimpleViewHolder, User>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            return SimpleViewHolder(LayoutInflater.from(parent?.context)
                    .inflate(R.layout.permission_manage_item, parent, false))

        }

        override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

            val view = holder?.itemView
            val user = mItemList[position]

            view?.userAvatar5?.setUser(user, imageLoader)
            view?.userName?.text = user.userName

        }

    }

}
