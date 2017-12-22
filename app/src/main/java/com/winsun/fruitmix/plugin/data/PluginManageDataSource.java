package com.winsun.fruitmix.plugin.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;

/**
 * Created by Administrator on 2017/12/22.
 */

public interface PluginManageDataSource {

    String TYPE_SAMBA = "samba";
    String TYPE_DLNA = "dlna";

    void getPluginStatus(String type,BaseLoadDataCallback<PluginStatus> callback);

    void updatePluginStatus(String type, String action, BaseOperateDataCallback<Void> callback);

    void getBTStatus(BaseLoadDataCallback<PluginStatus> callback);

    void updateBTStatus(String op,BaseOperateDataCallback<Void> callback);

}
