package com.winsun.fruitmix.file.data.station;

import android.support.annotation.NonNull;
import android.util.Log;

import com.winsun.fruitmix.base.data.SCloudTokenContainer;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.file.data.download.FileDownloadErrorState;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam;
import com.winsun.fruitmix.file.data.download.param.FileFromBoxDownloadParam;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.upload.FileUploadState;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.FileFormData;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.TextFormData;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.IHttpFileUtil;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteFileFolderParser;
import com.winsun.fruitmix.parser.RemoteRootDriveFolderParser;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.Collections;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/7/19.
 */

public class StationFileDataSourceImpl extends BaseRemoteDataSourceImpl implements StationFileDataSource, SCloudTokenContainer {

    private static final String ROOT_DRIVE_PARAMETER = "/drives";

    public static final String TAG = StationFileDataSourceImpl.class.getSimpleName();
    private static final String OP = "op";
    private static final String MKDIR = "mkdir";
    private static final String DIRS = "/dirs/";

    private IHttpFileUtil iHttpFileUtil;

    private static StationFileDataSource instance;

    private String mSCloudToken;

    public static StationFileDataSource getInstance(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory, IHttpFileUtil iHttpFileUtil) {

        if (instance == null)
            instance = new StationFileDataSourceImpl(iHttpUtil, httpRequestFactory, iHttpFileUtil);

        return instance;
    }

    public static void destroyInstance() {

        instance = null;

    }


    private StationFileDataSourceImpl(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory, IHttpFileUtil iHttpFileUtil) {
        super(iHttpUtil, httpRequestFactory);
        this.iHttpFileUtil = iHttpFileUtil;
    }

    @Override
    public void getRootDrive(BaseLoadDataCallback<AbstractRemoteFile> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(ROOT_DRIVE_PARAMETER);

        wrapper.loadCall(httpRequest, callback, new RemoteRootDriveFolderParser());

    }

    /*
     * WISNUC API:GET DRIVE
     */
    @Override
    public void getFile(String rootUUID, final String folderUUID, BaseLoadDataCallback<AbstractRemoteFile> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(ROOT_DRIVE_PARAMETER + "/" + rootUUID + DIRS + folderUUID);

        wrapper.loadCall(httpRequest, callback, new RemoteFileFolderParser());

    }

    @Override
    public OperationResult getFile(String rootUUID, String folderUUID) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(ROOT_DRIVE_PARAMETER + "/" + rootUUID + DIRS + folderUUID);

        return wrapper.loadCall(httpRequest, new RemoteFileFolderParser());

    }


    /*
     * WISNUC API:GET DIRENTRY
     */
    @Override
    public void downloadFile(FileDownloadState fileDownloadState, BaseOperateDataCallback<FileDownloadItem> callback) throws MalformedURLException, IOException, SocketTimeoutException {

        FileDownloadParam fileDownloadParam = fileDownloadState.getFileDownloadParam();

        HttpRequest httpRequest;

        if (fileDownloadParam instanceof FileFromBoxDownloadParam) {

//            httpRequest = httpRequestFactory.createHttpGetRequest(fileDownloadParam.getFileDownloadPath(),
//                    Util.KEY_JWT_HEAD + ((FileFromBoxDownloadParam) fileDownloadParam).getCloudToken());

//            httpRequest = httpRequestFactory.createHttpGetFileRequest(fileDownloadParam.getFileDownloadPath(),
//                    getAuthorizationValue(((FileFromBoxDownloadParam) fileDownloadParam).getCloudToken()));

            FileFromBoxDownloadParam fileFromBoxDownloadParam = (FileFromBoxDownloadParam) fileDownloadParam;

            httpRequest = httpRequestFactory.createHttpGetFileRequest(fileDownloadParam.getFileDownloadPath(),
                    fileFromBoxDownloadParam.getBoxUUID(),fileFromBoxDownloadParam.getStationID(), mSCloudToken);

        } else {

            httpRequest = httpRequestFactory.createHttpGetFileRequest(fileDownloadParam.getFileDownloadPath());

        }

        if (!wrapper.checkPreCondition(httpRequest, callback))
            return;

        ResponseBody responseBody = null;

        FileDownloadItem fileDownloadItem = fileDownloadState.getFileDownloadItem();

        try {
            responseBody = iHttpUtil.getResponseBody(httpRequest);

            Log.d(TAG, "call: getResponseBody");

            boolean result = FileUtil.writeResponseBodyToFolder(responseBody, fileDownloadState);

            Log.d(TAG, "call: download result:" + result);

            if (result)
                callback.onSucceed(fileDownloadItem, new OperationSuccess());
            else
                callback.onFail(new OperationIOException());

        } catch (NetworkException e) {
            e.printStackTrace();

            fileDownloadItem.setFileTime(System.currentTimeMillis());

            fileDownloadItem.setFileDownloadState(new FileDownloadErrorState(fileDownloadItem));

            callback.onFail(new OperationNetworkException(e.getHttpResponse()));

        }

    }

    /*
     * WISNUC API:POST DIRENTRY LIST
     */
    @Override
    public void createFolder(String folderName, String driveUUID, String dirUUID, BaseOperateDataCallback<HttpResponse> callback) {

        String path = ROOT_DRIVE_PARAMETER + "/" + driveUUID + DIRS + dirUUID + "/entries";

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(path, "");

        if (!wrapper.checkPreCondition(httpRequest, callback)) {
            return;
        }

        Log.i(TAG, "createFolder: start create");

        HttpResponse httpResponse;
        try {

            if (httpRequest.getBody().length() != 0)
                httpResponse = createFolderWithCloudAPI(httpRequest, folderName);
            else
                httpResponse = createFolderWithStationAPI(httpRequest, folderName);

            if (httpResponse != null && httpResponse.getResponseCode() == 200)
                callback.onSucceed(httpResponse, new OperationSuccess());
            else
                callback.onFail(new OperationNetworkException(httpResponse));

        } catch (MalformedURLException e) {

            callback.onFail(new OperationMalformedUrlException());

        } catch (SocketTimeoutException ex) {

            callback.onFail(new OperationSocketTimeoutException());

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());
        }

    }

    private HttpResponse createFolderWithCloudAPI(HttpRequest httpRequest, String folderName) throws IOException {

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(httpRequest.getBody());

            jsonObject.put(OP, MKDIR);
            jsonObject.put("toName", folderName);

            httpRequest.setBody(jsonObject.toString());

            return iHttpUtil.remoteCall(httpRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private HttpResponse createFolderWithStationAPI(HttpRequest httpRequest, String folderName) throws IOException {

        JSONObject value;
        try {
            value = new JSONObject();

            value.put(OP, MKDIR);

            TextFormData textFormData = new TextFormData(folderName, value.toString());

            return iHttpUtil.remoteCallRequest(iHttpUtil.createPostRequest(httpRequest,
                    iHttpUtil.createFormDataRequestBody(Collections.singletonList(textFormData), Collections.<FileFormData>emptyList())));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }


    @Override
    public OperationResult uploadFile(LocalFile file, String driveUUID, String dirUUID) {

        HttpRequest httpRequest = createUploadFileHttpRequest(driveUUID, dirUUID);

        if (!wrapper.checkUrl(httpRequest.getUrl())) {
            return new OperationMalformedUrlException();
        }

        Log.i(TAG, "uploadFile: start upload: " + httpRequest.getUrl());

        Request request;
        try {
            request = iHttpUtil.createPostRequest(httpRequest, getUploadFileRequestBody(httpRequest, file));
        } catch (JSONException e) {
            e.printStackTrace();

            return new OperationJSONException();
        }

        return handleUploadFileRequest(request);

    }

    @NonNull
    private OperationResult handleUploadFileRequest(Request request) {
        HttpResponse httpResponse;
        try {

            httpResponse = iHttpUtil.remoteCallRequest(request);

            if (httpResponse != null && httpResponse.getResponseCode() == 200)
                return new OperationSuccess();
            else
                return new OperationNetworkException(httpResponse);

        } catch (MalformedURLException e) {

            return new OperationMalformedUrlException();

        } catch (SocketTimeoutException ex) {

            return new OperationSocketTimeoutException();

        } catch (IOException e) {
            e.printStackTrace();

            return new OperationIOException();
        }
    }


    private HttpRequest createUploadFileHttpRequest(String driveUUID, String dirUUID) {
        String path = ROOT_DRIVE_PARAMETER + "/" + driveUUID + DIRS + dirUUID + "/entries";

        return httpRequestFactory.createHttpPostFileRequest(path, "");
    }


    @Override
    public OperationResult uploadFileWithProgress(LocalFile file, FileUploadState fileUploadState, String driveUUID, String dirUUID) {

        HttpRequest httpRequest = createUploadFileHttpRequest(driveUUID, dirUUID);

        if (!wrapper.checkUrl(httpRequest.getUrl())) {
            return new OperationMalformedUrlException();
        }

        Log.i(TAG, "uploadFile: start upload: " + httpRequest.getUrl());

        Request request;
        try {
            request = iHttpFileUtil.createUploadWithProgressRequest(httpRequest,
                    getUploadFileRequestBody(httpRequest, file), fileUploadState);
        } catch (JSONException e) {
            e.printStackTrace();

            return new OperationJSONException();
        }

        return handleUploadFileRequest(request);

    }


    @NonNull
    private RequestBody getUploadFileRequestBody(HttpRequest httpRequest, LocalFile localFile) throws JSONException {
        RequestBody requestBody;

        if (httpRequest.getBody().length() != 0) {

            JSONObject jsonObject = new JSONObject(httpRequest.getBody());

            jsonObject.put(OP, "newfile");
            jsonObject.put("toName", localFile.getName());
            jsonObject.put(Util.SHA_256_STRING, localFile.getFileHash());
            jsonObject.put(Util.SIZE_STRING, Long.valueOf(localFile.getSize()));

            String jsonObjectStr = jsonObject.toString();

            Log.d(TAG, "uploadFile: " + jsonObjectStr);

            TextFormData textFormData = new TextFormData(Util.MANIFEST_STRING, jsonObjectStr);
            FileFormData fileFormData = new FileFormData("", localFile.getName(), new File(localFile.getPath()));

            requestBody = iHttpUtil.createFormDataRequestBody(Collections.singletonList(textFormData), Collections.singletonList(fileFormData));


        } else {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Util.SIZE_STRING, Long.valueOf(localFile.getSize()));
            jsonObject.put(Util.SHA_256_STRING, localFile.getFileHash());

            String jsonObjectStr = jsonObject.toString();

            Log.d(TAG, "uploadFile: " + jsonObjectStr);

            FileFormData fileFormData = new FileFormData(localFile.getName(), jsonObjectStr, new File(localFile.getPath()));

            requestBody = iHttpUtil.createFormDataRequestBody(Collections.<TextFormData>emptyList(), Collections.singletonList(fileFormData));

        }
        return requestBody;
    }

    @Override
    public void setSCloudToken(String sCloudToken) {

        mSCloudToken = sCloudToken;

    }
}
