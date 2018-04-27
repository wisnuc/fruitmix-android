package com.winsun.fruitmix.newdesign201804.login

import android.content.Context
import com.winsun.fruitmix.callback.BaseLoadDataCallback
import com.winsun.fruitmix.callback.BaseOperateCallback
import com.winsun.fruitmix.model.operationResult.OperationResult
import com.winsun.fruitmix.newdesign201804.wechatUser.WeChatUserInfoDataSource
import com.winsun.fruitmix.token.WeChatTokenUserWrapper
import com.winsun.fruitmix.token.data.TokenDataSource
import com.winsun.fruitmix.util.ToastUtil
import com.winsun.fruitmix.wxapi.MiniProgram
import com.winsun.fruitmix.wxapi.WXEntryActivity

public class LoginPresenter(val tokenDataSource: TokenDataSource, val wechatUserInfoDataSource: WeChatUserInfoDataSource) {

    fun loginWithWechat(context: Context, baseOperateCallback: BaseOperateCallback) {

        WXEntryActivity.setWxEntryGetWeChatCodeCallback(object : WXEntryActivity.WXEntryGetWeChatCodeCallback {
            override fun fail(resID: Int) {
                ToastUtil.showToast(context, context.getString(resID))
            }

            override fun succeed(code: String?) {
                handleGetCodeSucceed(code, baseOperateCallback)
            }

        })

        MiniProgram.sendAuthRequest(MiniProgram.registerToWX(context))

    }

    private fun handleGetCodeSucceed(code: String?, baseOperateCallback: BaseOperateCallback) {

        tokenDataSource.getCloudToken(code, object : BaseLoadDataCallback<WeChatTokenUserWrapper> {

            override fun onSucceed(data: MutableList<WeChatTokenUserWrapper>?, operationResult: OperationResult?) {

                val weChatTokenUserWrapper = data?.get(0)

                wechatUserInfoDataSource.weChatTokenUserWrapper = weChatTokenUserWrapper!!

                baseOperateCallback.onSucceed()
            }

            override fun onFail(operationResult: OperationResult?) {
                baseOperateCallback.onFail(operationResult)
            }

        })

    }

}

