package com.winsun.fruitmix.file.data.station;

import android.util.Log;

import com.winsun.fruitmix.base.data.SCloudTokenContainer;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.FileFormData;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.TextFormData;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteFileFolderParser;
import com.winsun.fruitmix.parser.RemoteRootDriveFoldersParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Collections;

/**
 * Created by Administrator on 2017/7/19.
 */

public class StationFileDataSourceImpl extends BaseRemoteDataSourceImpl implements StationFileDataSource, SCloudTokenContainer {

    private static final String ROOT_DRIVE_PARAMETER = "/drives";

    public static final String TAG = StationFileDataSourceImpl.class.getSimpleName();
    private static final String OP = "op";
    private static final String MKDIR = "mkdir";
    private static final String DIRS = "/dirs/";

    private static StationFileDataSource instance;

    private String mSCloudToken;

    public static StationFileDataSource getInstance(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {

        if (instance == null)
            instance = new StationFileDataSourceImpl(iHttpUtil, httpRequestFactory);

        return instance;
    }

    public static void destroyInstance() {

        instance = null;

    }


    private StationFileDataSourceImpl(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);

    }

    @Override
    public void getRootDrive(BaseLoadDataCallback<AbstractRemoteFile> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(ROOT_DRIVE_PARAMETER);

        wrapper.loadCall(httpRequest, callback, new RemoteRootDriveFoldersParser());

    }

    /*
     * WISNUC API:GET DRIVE
     */
    @Override
    public void getFile(String rootUUID, final String folderUUID,BaseLoadDataCallback<AbstractRemoteFile> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(ROOT_DRIVE_PARAMETER + "/" + rootUUID + DIRS + folderUUID);

        wrapper.loadCall(httpRequest, callback, new RemoteFileFolderParser());

    }

    @Override
    public OperationResult getFile(String rootUUID, String folderUUID) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(ROOT_DRIVE_PARAMETER + "/" + rootUUID + DIRS + folderUUID);

        return wrapper.loadCall(httpRequest, new RemoteFileFolderParser());

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
    public void setSCloudToken(String sCloudToken) {

        mSCloudToken = sCloudToken;

    }
}
