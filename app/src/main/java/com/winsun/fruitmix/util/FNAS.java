package com.winsun.fruitmix.util;

import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.model.OperationType;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/4/22.
 */
public class FNAS {

    public static final String TAG = FNAS.class.getSimpleName();

    public static String Gateway = "http://192.168.5.98";
    public static String JWT = null;
    public static String userUUID = null;

    public static String TEMPORARY_GATEWAY = "";
    public static String TEMPORARY_JWT = "";
    public static String TEMPORARY_USER_UUID = "";

    public static String PORT = "3000";

    public static void handleLogout() {

        EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));

        Util.setRemoteMediaLoaded(false);

    }

}
