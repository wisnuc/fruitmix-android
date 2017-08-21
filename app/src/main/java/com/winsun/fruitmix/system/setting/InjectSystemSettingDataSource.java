package com.winsun.fruitmix.system.setting;

import android.content.Context;

/**
 * Created by Administrator on 2017/8/21.
 */

public class InjectSystemSettingDataSource {

    public static SystemSettingDataSource provideSystemSettingDataSource(Context context){
        return SystemSettingDataSource.getInstance(context);
    }

}
