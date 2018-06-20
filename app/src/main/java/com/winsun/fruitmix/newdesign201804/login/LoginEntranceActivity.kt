package com.winsun.fruitmix.newdesign201804.login

import android.content.Intent
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View

import com.winsun.fruitmix.R
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.equipment.list.EquipmentListActivity
import com.winsun.fruitmix.newdesign201804.wechatUser.WeChatUserInfoDataSource
import com.winsun.fruitmix.token.data.InjectTokenRemoteDataSource
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.activity_login_entrance.*
import kotlinx.android.synthetic.main.user_protocal_message.view.*

class LoginEntranceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_entrance)

        Util.setStatusBarColor(this, R.color.new_design_primary_color)

        user_protocol.paint.flags = Paint.UNDERLINE_TEXT_FLAG

        userAvatar.visibility = View.INVISIBLE
        login_with_exist_user.visibility = View.INVISIBLE

        user_protocol.setOnClickListener {
            showUserProtocol()
        }

        val loginPresenter = LoginPresenter(InjectTokenRemoteDataSource.provideTokenDataSource(this),
                WeChatUserInfoDataSource)

        wechat_login_layout.setOnClickListener {

/*            loginPresenter.loginWithWechat(this, object : BaseOperateCallbackImpl() {
                override fun onSucceed() {
                    enterEquipmentListActivity()
                }

            })*/

            enterEquipmentListActivity()

        }

    }

    private fun showUserProtocol() {

        val view = LayoutInflater.from(this).inflate(R.layout.user_protocal_message, null, false)

        view.userProtocolTextView.text = "打发时间了的分散发的说法大发的飞大发的发地方" +
                "发地方士大夫asdsad发的发斯蒂芬三大发的发的fsd" +
                "圣诞节拉风飞的萨芬撒地方飞" +
                "发动机可分为去哦让发动机为亲朋偶然风刀霜剑撒的发动机" +
                "操作V第三节课V魔弦传说的法律九分裤" +
                "范德萨水电费了健康撒地方认为缺乏服务器而非的行政村啊V型从中选出V字fadsfsad" +
                "示范点发地方发的说法大沙发沙发是飞洒发发发阿斯顿发生发生分歧热风见附发士大夫撒旦" +
                "发生的发生的房间去玩儿初三的【阿分码数的废弃物减肥法卡士大夫飞洒地方去潍坊市的骄傲IP分群文件" +
                "范围二十多岁的贫困撒地方的覅偶武器的萨芬破瓦分开发撒的【让我欺负起违反佛法孙大伟飞士大夫的" +
                "富士达我去额额为企鹅的算法的电风扇的罚款了的服务器而破地方撒撒地方、撒地方分仍无法范德萨" +
                "范围而佛山东方咖啡色的凯撒的反馈撒地方的地方萨克爱的防晒霜的司法考试的发起为啥的方法的" +
                "水电费却无法开发商的算法的武器二分的色块佛范德萨范德萨范德萨发额粉色发斯蒂芬尽责为辅" +
                "发送到佛牌【批发商的咖啡控的撒【封口费发的【分配咖啡三分手费士大夫【客服发的发士大夫" +
                "发达一番发放的的父母父母V撒地方分的大师分VM浮球阀否卡风靡全球，放声大哭发发麻烦" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放" +
                "案发开发的搜房的开发没法发咖啡方法撒都发发麻烦死的的发生的开发发发发生的开发撒地方撒发放"

        val dialog = AlertDialog.Builder(this).setTitle(getString(R.string.user_protocol))
                .setView(view)
                .setPositiveButton(getString(R.string.close), null)
                .setCancelable(true)
                .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.new_design_primary_color))

    }

    private fun enterEquipmentListActivity() {

        val intent = Intent(this, EquipmentListActivity::class.java)

        startActivity(intent)

        finish()

    }

}
