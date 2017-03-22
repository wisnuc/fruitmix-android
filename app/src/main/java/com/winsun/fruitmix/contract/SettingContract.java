package com.winsun.fruitmix.contract;

import android.content.Context;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;

/**
 * Created by Administrator on 2017/3/21.
 */

public interface SettingContract {

    interface SettingView extends BaseView {

        void setAutoUploadPhotosSwitchChecked(boolean checked);

        void setAlreadyUploadMediaCountTextViewVisibility(int visibility);

        void setAlreadyUploadMediaCountText(String text);

        String getString(int resID);

        void setCacheSizeText(String text);

        boolean getAutoUploadPhotosSwitchChecked();
    }

    interface SettingPresenter extends BasePresenter<SettingView> {

        void initView(Context context);

        void clearCache(Context context);

        void onDestroy();

    }

}
