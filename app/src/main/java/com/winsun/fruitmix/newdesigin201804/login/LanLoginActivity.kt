package com.winsun.fruitmix.newdesigin201804.login

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.anim.AnimatorBuilder
import com.winsun.fruitmix.anim.BaseAnimationListener
import com.winsun.fruitmix.databinding.ActivityLanLoginBinding
import com.winsun.fruitmix.util.Util

class LanLoginActivity : BaseToolbarActivity() {

    private lateinit var mActivityLanLoginBinding: ActivityLanLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mActivityLanLoginBinding.forgetPasswordTv.setOnClickListener {
            handleForgetPasswordOnClick()
        }

        mActivityLanLoginBinding.forgetPasswordIcon.setOnClickListener {
            handleForgetPasswordOnClick()
        }

    }

    private fun handleForgetPasswordOnClick() {

        if (mActivityLanLoginBinding.forgetPasswordExplainLayout.visibility == View.VISIBLE)
            return

        mActivityLanLoginBinding.forgetPasswordExplainLayout.visibility = View.VISIBLE

        createAnimator(mActivityLanLoginBinding.forgetPasswordExplainLayout,0,Util.dip2px(this,100f))
                .start()

        AnimatorBuilder(this, R.animator.forget_password_explain_translation, mActivityLanLoginBinding.forgetPasswordExplainLayout)
                .startAnimator()

    }

    private fun createAnimator(view:View,start:Int,end:Int):ValueAnimator{

        val valueAnimator = ValueAnimator.ofInt(start,end)

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
