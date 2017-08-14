package com.winsun.fruitmix.util;

import android.content.Context;
import android.util.Base64;

import com.winsun.fruitmix.equipment.InjectEquipment;
import com.winsun.fruitmix.eventbus.AbstractFileRequestEvent;
import com.winsun.fruitmix.eventbus.MediaRequestEvent;
import com.winsun.fruitmix.eventbus.RequestEvent;
import com.winsun.fruitmix.eventbus.TokenRequestEvent;
import com.winsun.fruitmix.eventbus.UserRequestEvent;
import com.winsun.fruitmix.http.CheckIpHttpUtil;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.IHttpFileUtil;
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

    public static HttpResponse loadFileInFolder(Context context, String folderUUID, String rootUUID) throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(context, Util.LIST_FILE_PARAMETER + "/" + folderUUID + "/" + rootUUID);
    }

    public static HttpResponse loadFileSharedWithMe(Context context) throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(context, Util.FILE_SHARE_PARAMETER + Util.FILE_SHARED_WITH_ME_PARAMETER);
    }

    public static HttpResponse loadFileShareWithOthers(Context context) throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(context, Util.FILE_SHARE_PARAMETER + Util.FILE_SHARED_WITH_OTHERS_PARAMETER);
    }

    public static HttpResponse loadUser(Context context) throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(context, Util.ACCOUNT_PARAMETER);

    }

    public static HttpResponse loadOtherUsers(Context context) throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(context, Util.LOGIN_PARAMETER);

    }

    public static HttpResponse loadMedia(Context context) throws MalformedURLException, IOException, SocketTimeoutException {

        return FNAS.RemoteCall(context, Util.MEDIA_PARAMETER); // get all pictures;

    }

    public static HttpResponse loadToken(Context context, String gateway, String userUUID, String userPassword) throws MalformedURLException, IOException, SocketTimeoutException {

        String url = gateway + ":" + FNAS.PORT + Util.TOKEN_PARAMETER;

        return FNAS.RemoteCallWithUrl(context, url, Util.KEY_AUTHORIZATION, Util.KEY_BASE_HEAD + Base64.encodeToString((userUUID + ":" + userPassword).getBytes(), Base64.NO_WRAP));

    }

    public static HttpResponse loadDeviceId(Context context) throws MalformedURLException, IOException, SocketTimeoutException {
        HttpResponse httpResponse = new HttpResponse();
        if (LocalCache.DeviceID == null || LocalCache.DeviceID.equals("")) {
            httpResponse = FNAS.PostRemoteCall(context, Util.DEVICE_ID_PARAMETER, "");
        }

        return httpResponse;
    }

    public static void retrieveRemoteDeviceID(Context context) {

        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.REMOTE_DEVICE_ID));
    }

    public static void retrieveRemoteToken(Context context, String gateway, String userUUID, String pwd) {

        EventBus.getDefault().post(new TokenRequestEvent(OperationType.GET, OperationTargetType.REMOTE_TOKEN, gateway, userUUID, pwd));
    }

    public static void retrieveUser(Context context) {

        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.REMOTE_USER));
    }

    public static void retrieveLocalMediaInCamera() {
        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.LOCAL_MEDIA_IN_CAMERA));
    }

    public static void retrieveLocalMedia(Context context) {

        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.LOCAL_MEDIA));
    }

    public static void retrieveLocalLoggedInUser() {
        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.LOCAL_LOGGED_IN_USER));
    }

    public static void retrieveRemoteMedia(Context context) {

        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.REMOTE_MEDIA));
    }

    public static void retrieveRemoteFile(Context context, String folderUUID, String rootUUID) {

        EventBus.getDefault().post(new AbstractFileRequestEvent(OperationType.GET, OperationTargetType.REMOTE_FILE, folderUUID, rootUUID));
    }

    public static void retrieveDownloadedFile() {
        EventBus.getDefault().post(new RequestEvent(OperationType.GET, OperationTargetType.DOWNLOADED_FILE));
    }

    public static void createRemoteUser(String userName, String userPassword) {
        EventBus.getDefault().post(new UserRequestEvent(OperationType.CREATE, OperationTargetType.REMOTE_USER, userName, userPassword));
    }

    public static void createRemoteMedia(Context context, Media media) {

        EventBus.getDefault().post(new MediaRequestEvent(OperationType.CREATE, OperationTargetType.REMOTE_MEDIA, media));
    }

    public static String generateUrl(String req) {

        HttpRequestFactory httpRequestFactory = InjectHttp.provideHttpRequestFactory();

        return httpRequestFactory.getGateway() + ":" + httpRequestFactory.getPort() + req;
    }

    public static String getDownloadOriginalMediaUrl(Media media) {
        return generateUrl(Util.MEDIA_PARAMETER + "/" + media.getUuid() + "/download");
    }

    public static String getDownloadFileUrl(String fileUUID, String parentFolderUUID) {
        return generateUrl(Util.DOWNLOAD_FILE_PARAMETER + "/" + parentFolderUUID + "/" + fileUUID);
    }

    private static HttpResponse RemoteCall(Context context, String req) throws MalformedURLException, IOException, SocketTimeoutException {

//        return GetRemoteCall(Gateway + ":" + FNAS.PORT + req);

        HttpRequest httpRequest = new HttpRequest(generateUrl(req), Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);

//        return new OkHttpUtil().remoteCall(httpRequest);

        return getHttpResponse(context, httpRequest);
    }

    public static HttpResponse RemoteCallWithUrl(String url) throws MalformedURLException, IOException, SocketTimeoutException {

        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);

        return OkHttpUtil.getInstance().remoteCall(httpRequest);

    }

    private static HttpResponse getHttpResponse(Context context, HttpRequest httpRequest) throws IOException {

//        return new OkHttpUtil().remoteCall(httpRequest);

        if (LocalCache.currentEquipmentName == null) {
            LocalCache.initCurrentEquipmentName();
        }

        CheckIpHttpUtil iHttpFileUtil = CheckIpHttpUtil.getInstance(OkHttpUtil.getInstance(),InjectEquipment.provideEquipmentSearchManager(context));

        return CheckIpHttpUtil.getInstance(OkHttpUtil.getInstance(), InjectEquipment.provideEquipmentSearchManager(context)).remoteCall(httpRequest);
    }

    private static HttpResponse RemoteCallWithUrl(Context context, String url, String headerKey, String headerValue) throws MalformedURLException, IOException, SocketTimeoutException {

        HttpRequest httpRequest = new HttpRequest(url, Util.HTTP_GET_METHOD);
        httpRequest.setHeader(headerKey, headerValue);

        return OkHttpUtil.getInstance().remoteCall(httpRequest);

    }

    // create object and store it to the server
    public static HttpResponse PostRemoteCall(Context context, String req, String data) throws MalformedURLException, IOException, SocketTimeoutException {
//        return RemoteCallMethod(Util.HTTP_POST_METHOD, req, data);

        HttpRequest httpRequest = new HttpRequest(generateUrl(req), Util.HTTP_POST_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
        httpRequest.setBody(data);

        return getHttpResponse(context, httpRequest);
    }

    // modify data and save it
    public static HttpResponse PatchRemoteCall(Context context, String req, String data) throws MalformedURLException, IOException, SocketTimeoutException {
//        return RemoteCallMethod(Util.HTTP_PATCH_METHOD, req, data);

        HttpRequest httpRequest = new HttpRequest(generateUrl(req), Util.HTTP_PATCH_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
        httpRequest.setBody(data);

        return getHttpResponse(context, httpRequest);
    }

    public static HttpResponse DeleteRemoteCall(Context context, String req, String data) throws MalformedURLException, IOException, SocketTimeoutException {
//        return RemoteCallMethod(Util.HTTP_DELETE_METHOD, req, data);

        HttpRequest httpRequest = new HttpRequest(generateUrl(req), Util.HTTP_DELETE_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + JWT);
        httpRequest.setBody(data);

        return getHttpResponse(context, httpRequest);
    }

    public static void handleLogout() {

        EventBus.getDefault().post(new RequestEvent(OperationType.STOP_UPLOAD, null));

        Util.setRemoteMediaLoaded(false);

    }

}
