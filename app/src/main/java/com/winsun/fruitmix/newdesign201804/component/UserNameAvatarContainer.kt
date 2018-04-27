package com.winsun.fruitmix.newdesign201804.component

import android.content.Context
import android.view.View
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.user.User
import kotlinx.android.synthetic.main.user_name_avatar_container.view.*

class UserNameAvatarContainer(private val userNameAvatarContainerView: View, val currentUser: User) {

    fun initView(context: Context) {

        userNameAvatarContainerView.userAvatar4.setUser(currentUser,
                InjectHttp.provideImageGifLoaderInstance(context).getImageLoader(context))

        userNameAvatarContainerView.userNameTv.text = currentUser.userName

        userNameAvatarContainerView.modifyUserNameIv.setOnClickListener {

            userNameAvatarContainerView.userNameTv.visibility = View.INVISIBLE
            userNameAvatarContainerView.modifyUserNameEditText.visibility = View.VISIBLE

            userNameAvatarContainerView.modifyUserNameEditText.setText(currentUser.userName)

        }

    }

    fun quitEditState() {

        userNameAvatarContainerView.userNameTv.visibility = View.VISIBLE
        userNameAvatarContainerView.modifyUserNameEditText.visibility = View.INVISIBLE

        userNameAvatarContainerView.userNameTv.text = currentUser.userName

    }

}