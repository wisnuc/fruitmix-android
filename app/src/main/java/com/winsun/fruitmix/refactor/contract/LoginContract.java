package com.winsun.fruitmix.refactor.contract;

import android.content.Context;

import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface LoginContract {

    interface LoginView extends BaseView {

        void setEquipmentGroupNameText(String equipmentGroupNameText);

        void setEquipmentChildNameText(String equipmentChildNameText);

        void setUserDefaultPortraitText(String userDefaultPortraitText);

        void setUserDefaultPortraitBgColor(int userDefaultPortraitBgColor);

        void handleLoginSucceed();

        void handleLoginFail(OperationResult result);

        void finishActivity();

        void hidePwdEditHint();

        void showPwdEditHint();
    }

    interface LoginPresenter extends BasePresenter<LoginView> {

        void showEquipmentAndUser();

        void login(String userPassword);

        void onFocusChange(boolean hasFocus);
    }

}
