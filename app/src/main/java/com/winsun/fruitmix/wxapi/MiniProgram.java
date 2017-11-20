package com.winsun.fruitmix.wxapi;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.winsun.fruitmix.R;

import java.io.ByteArrayOutputStream;

/**
 * Created by Administrator on 2017/7/13.
 */

public class MiniProgram {

    public static final String APP_ID = "wx99b54eb728323fe8";

    private static final int THUMB_SIZE = 150;

    public static final String TAG = MiniProgram.class.getSimpleName();

    private static final String REQ_SCORE = "snsapi_userinfo";

    public static void sendAuthRequest(IWXAPI iwxapi) {

        SendAuth.Req req = new SendAuth.Req();

        req.scope = REQ_SCORE;

        iwxapi.sendReq(req);

    }

    public static IWXAPI registerToWX(Context context) {

        IWXAPI iwxapi = WXAPIFactory.createWXAPI(context, APP_ID, true);

        iwxapi.registerApp(APP_ID);

        return iwxapi;
    }

    public static void shareMiniWXApp(Context context, IWXAPI iwxapi, Resources resources, String ticket) {

        WXMiniProgramObject wxMiniProgramObject = new WXMiniProgramObject();

        wxMiniProgramObject.webpageUrl = "http://www.qq.com";
        wxMiniProgramObject.userName = "gh_37b3425fe189";

        wxMiniProgramObject.path = "pages/login/login" + "?ticket=" + ticket;

//        wxMiniProgramObject.path = "pages/login/login";

        WXMediaMessage wxMediaMessage = new WXMediaMessage(wxMiniProgramObject);

        wxMediaMessage.title = context.getString(R.string.invite_mini_program_title);

        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.invite_miniprogram_thumb2);

//        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
//        bitmap.recycle();

        wxMediaMessage.thumbData = bmpToByteArray(bitmap, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("miniprogram");
        req.message = wxMediaMessage;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        boolean result = iwxapi.sendReq(req);

        Log.d(TAG, "shareMiniWXApp: result " + result);
    }


    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
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
