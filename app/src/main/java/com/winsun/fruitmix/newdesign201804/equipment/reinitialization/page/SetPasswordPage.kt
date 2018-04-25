package com.winsun.fruitmix.newdesign201804.equipment.reinitialization.page

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.winsun.fruitmix.R
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.user.User
import kotlinx.android.synthetic.main.set_password_reinitialization.view.*

class SetPasswordPage(val context: Context,val currentUser:User,
                              val preStep:()->Unit,val nextStep:(setPassword:String)->Unit): InitialPage {

    private val view = LayoutInflater.from(context).inflate(R.layout.set_password_reinitialization,null)

    private var setPassword = ""

    override fun getView(): View {
        return view
    }

    override fun refreshView() {

        view.userAvatar4.setUser(currentUser,
                InjectHttp.provideImageGifLoaderInstance(context).getImageLoader(context))

        view.userNameTv.text = currentUser.userName

        setBtnEnableOrNot(view.nextStepBtn,true)

        view.preStepBtn.setOnClickListener {
            preStep()
        }

        view.nextStepBtn.setOnClickListener {
            nextStep(setPassword)
        }

    }

    private fun setBtnEnableOrNot(btn:Button, enabled:Boolean){

        if(enabled){
            btn.setBackgroundResource(R.drawable.green_btn_bg)
            btn.setTextColor(ContextCompat.getColor(btn.context,R.color.eighty_seven_percent_white))
        }else{
            btn.setBackgroundColor(R.drawable.white_btn_bg)
            btn.setTextColor(ContextCompat.getColor(btn.context,R.color.twenty_six_percent_black))
        }

    }

}