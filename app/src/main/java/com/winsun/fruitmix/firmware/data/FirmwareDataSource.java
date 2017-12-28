package com.winsun.fruitmix.firmware.data;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.firmware.model.Firmware;

/**
 * Created by Administrator on 2017/12/28.
 */

public interface FirmwareDataSource {

    void installFirmware(String versionName, BaseOperateDataCallback<Void> callback);

    void updateFirmwareState(String state, BaseOperateDataCallback<Void> callback);

    void updateDownloadFirmwareState(String versionName,String state, BaseOperateDataCallback<Void> callback);

    void checkFirmwareUpdate(BaseOperateDataCallback<Void> callback);

    void getFirmware(BaseLoadDataCallback<Firmware> callback);

}
