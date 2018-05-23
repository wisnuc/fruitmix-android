package com.winsun.fruitmix.newdesign201804.login

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.anim.AnimatorBuilder
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.databinding.ActivityLanLoginBinding
import com.winsun.fruitmix.equipment.search.data.InjectEquipment
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.equipment.abnormal.EQUIPMENT_ITEM_UUID_KEY
import com.winsun.fruitmix.newdesign201804.equipment.list.data.InjectEquipmentItemDataSource
import com.winsun.fruitmix.util.SnackbarUtil
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.activity_lan_login.view.*

class LanLoginActivity : BaseToolbarActivity() {

    private lateinit var mActivityLanLoginBinding: ActivityLanLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbarWhiteStyle(toolbarViewModel)
        setStatusBarToolbarBgColor(R.color.lan_login_primary_color)

        mActivityLanLoginBinding.forgetPasswordTv.setOnClickListener {
            handleForgetPasswordOnClick()
        }

        mActivityLanLoginBinding.forgetPasswordIcon.setOnClickListener {
            handleForgetPasswordOnClick()
        }

        val itemUUID = intent.getStringExtra(EQUIPMENT_ITEM_UUID_KEY)

        val lanLoginPresenter = LanLoginPresenter(itemUUID, InjectEquipmentItemDataSource.inject(this),
                InjectEquipment.provideEquipmentDataSource(this))

        lanLoginPresenter.initView(mActivityLanLoginBinding.root)

        mActivityLanLoginBinding.loginBtn.setOnClickListener {

            val password = mActivityLanLoginBinding.lanPasswordEditText.text.toString()

            showProgressDialog(getString(R.string.operating_title,getString(R.string.login)))

            lanLoginPresenter.lanLogin(password,object :BaseOperateCallback{
                override fun onFail(operationResult: OperationResult?) {

                    dismissDialog()

                    SnackbarUtil.showSnackBar(mActivityLanLoginBinding.forgetPasswordTv, Snackbar.LENGTH_SHORT,
                            messageStr = getString(R.string.fail,getString(R.string.login)))
                }

                override fun onSucceed() {

                    dismissDialog()

                    finish()

                }
            })

        }

    }

    private fun handleForgetPasswordOnClick() {

        if (mActivityLanLoginBinding.forgetPasswordExplainLayout.visibility == View.VISIBLE)
            return

        mActivityLanLoginBinding.forgetPasswordExplainLayout.visibility = View.VISIBLE

        createAnimator(mActivityLanLoginBinding.forgetPasswordExplainLayout, 0, Util.dip2px(this, 100f))
                .start()

        R.layout.activity_lan_login

    }

    private fun createAnimator(view: View, start: Int, end: Int): ValueAnimator {

        val valueAnimator = ValueAnimator.ofInt(start, end)

        valueAnimator.addUpdateListener {

            val layoutParams = view.layoutParams

            layoutParams.height = it.animatedValue as Int

            view.layoutParams = layoutParams

        }

        return valueAnimator

    }

    override fun generateContent(root: ViewGroup?): View {

        mActivityLanLoginBinding = ActivityLanLoginBinding.inflate(LayoutInflater.from(this),
                root, false)

        return mActivityLanLoginBinding.root

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.lan_login_title)
    }

}
