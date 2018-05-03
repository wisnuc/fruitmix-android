package com.winsun.fruitmix.newdesign201804.user

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.component.UserNameAvatarContainer
import com.winsun.fruitmix.newdesign201804.wechatUser.WeChatUserInfoDataSource
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.android.synthetic.main.user_name_avatar_container.view.*

private const val TRANSITION_AVATAR_NAME = "transition_avatar_name"

fun startUserInfoActivity(toolbar: View?, activity: Activity, transitionView: View) {

    val intent = Intent(activity, UserInfoActivity::class.java)

    ViewCompat.setTransitionName(transitionView, TRANSITION_AVATAR_NAME)

    val pair = Pair(transitionView, TRANSITION_AVATAR_NAME)

    val pairs = Util.createSafeTransitionPairs(toolbar, activity, false, pair)

    val activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *pairs)

    activity.startActivity(intent, activityOptionsCompat.toBundle())

}

class UserInfoActivity : BaseToolbarActivity() {

    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStatusBarToolbarBgColor(R.color.new_design_primary_color)
        setToolbarWhiteStyle(toolbarViewModel)

        val user = WeChatUserInfoDataSource.getUser()

        ActivityCompat.postponeEnterTransition(this)

        user_name_avatar_container.userAvatar4.registerAvatarLoadListener {

            scheduleStartPostponedTransition(user_name_avatar_container.userAvatar4)

        }

        ViewCompat.setTransitionName(user_name_avatar_container.userAvatar4, TRANSITION_AVATAR_NAME)

        val userNameAvatarContainer = UserNameAvatarContainer(user_name_avatar_container, user)
        userNameAvatarContainer.initView(this)

        rootView.setOnClickListener {
            userNameAvatarContainer.quitEditState()
        }

    }

    override fun generateContent(root: ViewGroup?): View {
        rootView = LayoutInflater.from(this).inflate(R.layout.activity_user_info, root, false)

        return rootView
    }

    override fun getToolbarTitle(): String {
        return getString(R.string.me)
    }

    private fun scheduleStartPostponedTransition(view: View) {

        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                ActivityCompat.startPostponedEnterTransition(this@UserInfoActivity)
                return false
            }
        })

    }

    override fun onBackPressed() {

        finishActivity()

    }

    private fun finishActivity() {

        supportFinishAfterTransition()

    }


}
