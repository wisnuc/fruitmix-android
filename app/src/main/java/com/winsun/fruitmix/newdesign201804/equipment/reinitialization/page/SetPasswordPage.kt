package com.winsun.fruitmix.newdesign201804.equipment.reinitialization.page

import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.winsun.fruitmix.R
import com.winsun.fruitmix.http.InjectHttp
import com.winsun.fruitmix.newdesign201804.component.UserNameAvatarContainer
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.presenter.SetPasswordPresenter
import com.winsun.fruitmix.user.OperateUserViewModel
import com.winsun.fruitmix.user.User
import kotlinx.android.synthetic.main.set_password_reinitialization.view.*
import kotlinx.android.synthetic.main.user_name_avatar_container.view.*

class SetPasswordPage(val context: Context, val currentUser: User,
                      val preStep: () -> Unit, val nextStep: (setPassword: String) -> Unit) : InitialPage {

    private val view = LayoutInflater.from(context).inflate(R.layout.set_password_reinitialization, null)

    private var setPassword = ""

    override fun getView(): View {
        return view
    }

    override fun refreshView() {

        val userNameAvatarContainer = UserNameAvatarContainer(view.user_name_avatar_container,currentUser)
        userNameAvatarContainer.initView(context)

        setBtnEnableOrNot(view.nextStepBtn, true)

        val setPasswordPresenter = SetPasswordPresenter()

        view.preStepBtn.setOnClickListener {
            preStep()
        }

        view.nextStepBtn.setOnClickListener {

            val newPassword = view.passwordTextInputEditText.text.toString()
            val confirmPassword = view.confirmPasswordTextInputEditText.text.toString()

            val operateUserViewModel = OperateUserViewModel()

            if (setPasswordPresenter.checkOperateUserPassword(context, newPassword,
                            confirmPassword, operateUserViewModel)) {

                nextStep(setPassword)

            } else {

                view.password_textinputlayout.isErrorEnabled = operateUserViewModel.userPasswordErrorEnable.get()
                view.password_textinputlayout.error = operateUserViewModel.userPasswordError.get()

                view.confirmPasswordTextInputLayout.isErrorEnabled = operateUserViewModel.userConfirmPasswordErrorEnable.get()
                view.confirmPasswordTextInputLayout.error = operateUserViewModel.userConfirmPasswordError.get()

            }

        }

    }

    private fun setBtnEnableOrNot(btn: Button, enabled: Boolean) {

        if (enabled) {
            btn.setBackgroundResource(R.drawable.green_btn_bg)
            btn.setTextColor(ContextCompat.getColor(btn.context, R.color.eighty_seven_percent_white))
        } else {
            btn.setBackgroundColor(R.drawable.white_btn_bg)
            btn.setTextColor(ContextCompat.getColor(btn.context, R.color.twenty_six_percent_black))
        }

    }

}