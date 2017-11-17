package com.winsun.fruitmix.file.data.station;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.file.data.download.FileDownloadErrorState;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.LocalFile;
import com.winsun.fruitmix.file.data.upload.FileUploadState;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/7/19.
 */

public class StationFileDataSourceImpl extends BaseRemoteDataSourceImpl implements StationFileDataSource {

    public static final String LIST_FILE_PARAMETER = "/drives";

    public static final String DOWNLOAD_FILE_PARAMETER = "/drives";

    public static final String ROOT_DRIVE_PARAMETER = "/drives";

    public static final String TAG = StationFileDataSourceImpl.class.getSimpleName();

    private IHttpFileUtil iHttpFileUtil;

    private static StationFileDataSource instance;

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

    @Override
    public void getFile(String rootUUID, final String folderUUID, BaseLoadDataCallback<AbstractRemoteFile> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(LIST_FILE_PARAMETER + "/" + rootUUID + "/dirs/" + folderUUID);

        wrapper.loadCall(httpRequest, callback, new RemoteFileFolderParser());

    }

    @Override
    public void downloadFile(FileDownloadState fileDownloadState, BaseOperateDataCallback<FileDownloadItem> callback) throws MalformedURLException, IOException, SocketTimeoutException {

        String encodedFileName = URLEncoder.encode(fileDownloadState.getFileName(), "UTF-8");

        HttpRequest httpRequest = httpRequestFactory.createHttpGetFileRequest(DOWNLOAD_FILE_PARAMETER + "/"
                + fileDownloadState.getDriveUUID() + "/dirs/" + fileDownloadState.getParentFolderUUID()
                + "/entries/" + fileDownloadState.getFileUUID() + "?name=" + encodedFileName);

        if(!wrapper.checkPreCondition(httpRequest,callback))
            return;

        ResponseBody responseBody = null;
        try {
            responseBody = iHttpFileUtil.downloadFile(httpRequest);

            Log.d(TAG, "call: downloadFile");

            boolean result = FileUtil.writeResponseBodyToFolder(responseBody, fileDownloadState);

            Log.d(TAG, "call: download result:" + result);

            if (result)
                callback.onSucceed(fileDownloadState.getFileDownloadItem(), new OperationSuccess());
            else
                callback.onFail(new OperationIOException());

        } catch (NetworkException e) {
            e.printStackTrace();

            FileDownloadItem fileDownloadItem = fileDownloadState.getFileDownloadItem();

            fileDownloadItem.setFileDownloadState(new FileDownloadErrorState(fileDownloadItem));

            callback.onFail(new OperationNetworkException(e.getHttpResponse()));

        }

    }

    @Override
    public void createFolder(String folderName, String driveUUID, String dirUUID, BaseOperateDataCallback<HttpResponse> callback) {

        String path = "/drives/" + driveUUID + "/dirs/" + dirUUID + "/entries";

        HttpRequest httpRequest = httpRequestFactory.createHttpPostRequest(path, "");

        if (!wrapper.checkPreCondition(httpRequest,callback)) {
            return;
        }

        Log.i(TAG, "createFolder: start create");

        HttpResponse httpResponse;
        try {
            httpResponse = iHttpFileUtil.createFolder(httpRequest, folderName);

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

    @Override
    public OperationResult uploadFile(LocalFile file, String driveUUID, String dirUUID) {

        String path = "/drives/" + driveUUID + "/dirs/" + dirUUID + "/entries";

        HttpRequest httpRequest = httpRequestFactory.createHttpPostFileRequest(path, "");

        if (!wrapper.checkUrl(httpRequest.getUrl())) {
            return new OperationMalformedUrlException();
        }

        Log.i(TAG, "uploadFile: start upload: " + httpRequest.getUrl());

        HttpResponse httpResponse;

        try {

            httpResponse = iHttpFileUtil.uploadFile(httpRequest, file);

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

    @Override
    public OperationResult uploadFileWithProgress(LocalFile file, FileUploadState fileUploadState, String driveUUID, String dirUUID) {

        String path = "/drives/" + driveUUID + "/dirs/" + dirUUID + "/entries";

        HttpRequest httpRequest = httpRequestFactory.createHttpPostFileRequest(path, "");

        if (!wrapper.checkUrl(httpRequest.getUrl())) {
            return new OperationMalformedUrlException();
        }

        Log.i(TAG, "uploadFile: start upload: " + httpRequest.getUrl());

        HttpResponse httpResponse;

        try {

            httpResponse = iHttpFileUtil.uploadFileWithProgress(fileUploadState,httpRequest, file);

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
}
