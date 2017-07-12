package com.winsun.fruitmix.invitation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteTicketParser;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/7/12.
 */

public class Invitation {

    public static final String TAG = Invitation.class.getSimpleName();

    private static final int THUMB_SIZE = 150;

    private IHttpUtil iHttpUtil;

    private IWXAPI iwxapi;

    public Invitation(IHttpUtil iHttpUtil,IWXAPI iwxapi) {
        this.iHttpUtil = iHttpUtil;
        this.iwxapi = iwxapi;
    }

    public void createTicket(BaseOperateDataCallback<String> callback) {

        HttpRequest httpRequest = new HttpRequest(FNAS.generateUrl(Util.TICKETS_PARAMETER), Util.HTTP_POST_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);
        httpRequest.setBody("");

        try {
            HttpResponse httpResponse = iHttpUtil.remoteCall(httpRequest);

            if (httpResponse.getResponseCode() == 200) {

                String ticket = new RemoteTicketParser().parse(httpResponse.getResponseData());

                callback.onSucceed(ticket, new OperationSuccess(0));

            } else
                callback.onFail(new OperationNetworkException(httpResponse.getResponseCode()));

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            callback.onFail(new OperationJSONException());
        }


    }

    public void shareMiniWXApp(Resources resources, String ticket) {

        WXMiniProgramObject wxMiniProgramObject = new WXMiniProgramObject();

        wxMiniProgramObject.webpageUrl = "http://www.qq.com";
        wxMiniProgramObject.userName = "gh_37b3425fe189";

        wxMiniProgramObject.path = "pages/login/index?ticket=" + ticket;

        WXMediaMessage wxMediaMessage = new WXMediaMessage(wxMiniProgramObject);

        wxMediaMessage.title = "test";
        wxMediaMessage.description = "test android share mini wx app";

        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.mipmap.launcher_logo);

        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
        bitmap.recycle();
        wxMediaMessage.thumbData = bmpToByteArray(thumbBmp, true);
        wxMediaMessage.setThumbImage(bitmap);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("miniprogram");
        req.message = wxMediaMessage;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        boolean result = iwxapi.sendReq(req);

        Log.d(TAG, "shareMiniWXApp: result " + result);
    }


    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


}
