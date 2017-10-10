package com.winsun.fruitmix.util;

import android.content.Context;
import android.util.Base64;

import com.winsun.fruitmix.equipment.data.InjectEquipment;
import com.winsun.fruitmix.eventbus.AbstractFileRequestEvent;
import com.winsun.fruitmix.eventbus.MediaRequestEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.eventbus.TokenRequestEvent;
import com.winsun.fruitmix.eventbus.UserRequestEvent;
import com.winsun.fruitmix.http.CheckIpHttpUtil;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.http.OkHttpUtil;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

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
