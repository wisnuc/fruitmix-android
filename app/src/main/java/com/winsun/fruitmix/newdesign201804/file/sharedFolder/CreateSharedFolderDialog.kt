package com.winsun.fruitmix.newdesign201804.file.sharedFolder

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.util.inflateView
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter
import com.winsun.fruitmix.recyclerview.SimpleViewHolder
import com.winsun.fruitmix.user.User

import kotlinx.android.synthetic.main.create_shared_folder_item.view.*
import kotlinx.android.synthetic.main.create_shared_folder_layout.view.*

class CreateSharedFolderDialog(val users: List<User>, val handleOnCreateSharedDisk: (selectedUsers: List<User>, diskName: String) -> Unit) {

    private val selectUsers = mutableListOf<User>()

    private lateinit var dialog: AlertDialog

    fun createDialog(context: Context) {

        val view = View.inflate(context, R.layout.create_shared_folder_layout, null)

        val sharedFolderNameEt = view.sharedFolderNameEt

        view.sharedUserRecyclerView.layoutManager = LinearLayoutManager(context)

        val createShareAdapter = CreateShareAdapter()

        view.sharedUserRecyclerView.adapter = createShareAdapter

        createShareAdapter.setItemList(users)
        createShareAdapter.notifyDataSetChanged()

        dialog = AlertDialog.Builder(context)
                .setTitle(R.string.create_new_shared_disk)
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.create, { dialog, which ->

                    dialog.dismiss()

                    val name = sharedFolderNameEt.text.toString()

                    handleOnCreateSharedDisk(selectUsers, name)

                }).create()

        dialog.show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

    }

    private inner class CreateShareAdapter : BaseRecyclerViewAdapter<SimpleViewHolder, User>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleViewHolder {

            val view = parent?.inflateView(R.layout.create_shared_folder_item)

            return SimpleViewHolder(view)
        }

        override fun onBindViewHolder(holder: SimpleViewHolder?, position: Int) {

            val user = mItemList[position]

            val view = holder?.itemView

            view?.sharedUserNameTv?.text = user.userName

            view?.sharedUserCheckBox?.isChecked = selectUsers.contains(user)

            view?.sharedUserCheckBox?.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked)
                    selectUsers.add(user)
                else
                    selectUsers.remove(user)

                if (::dialog.isInitialized)
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = !selectUsers.isEmpty()

            }

        }

    }

}