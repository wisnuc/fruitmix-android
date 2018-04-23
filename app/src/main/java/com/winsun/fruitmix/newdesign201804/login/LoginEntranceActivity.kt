package com.winsun.fruitmix.newdesign201804.login

import android.content.Intent
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View

import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.equipment.list.EquipmentListActivity
import com.winsun.fruitmix.newdesign201804.wechatUser.WeChatUserInfoDataSource
import com.winsun.fruitmix.token.data.InjectTokenRemoteDataSource
import kotlinx.android.synthetic.main.activity_login_entrance.*

class LoginEntranceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_entrance)

        user_protocol.paint.flags = Paint.UNDERLINE_TEXT_FLAG

        userAvatar.visibility = View.INVISIBLE
        login_with_exist_user.visibility = View.INVISIBLE

        user_protocol.setOnClickListener {
            showUserProtocol()
        }

        val loginPresenter = LoginPresenter(InjectTokenRemoteDataSource.provideTokenDataSource(this),
                WeChatUserInfoDataSource)

        wechat_login_layout.setOnClickListener {

            loginPresenter.loginWithWechat(this, object : BaseOperateCallbackImpl() {
                override fun onSucceed() {
                    enterEquipmentListActivity()
                }

            })

        }

    }

    private fun showUserProtocol() {

        val dialog = AlertDialog.Builder(this).setTitle(getString(R.string.user_protocol))
                .setMessage("打发时间了的分散发的说法大发的飞大发的发地方" +
                        "发地方士大夫asdsad发的发斯蒂芬三大发的发的fsd" +
                        "圣诞节拉风飞的萨芬撒地方飞" +
                        "发动机可分为去哦让发动机为亲朋偶然风刀霜剑撒的发动机" +
                        "操作V第三节课V魔弦传说的法律九分裤" +
                        "范德萨水电费了健康撒地方认为缺乏服务器而非的行政村啊V型从中选出V字")
                .setPositiveButton(getString(R.string.close), null)
                .setCancelable(true)
                .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.new_design_primary_color))

    }

    private fun enterEquipmentListActivity() {

        val intent = Intent(this, EquipmentListActivity::class.java)

        startActivity(intent)

    }

}
