package com.winsun.fruitmix.file.data.station;

import android.util.Log;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpFileUtil;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.parser.RemoteFileFolderParser;
import com.winsun.fruitmix.util.FileUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/7/19.
 */

public class StationFileDataSourceImpl extends BaseRemoteDataSourceImpl implements StationFileDataSource {

    public static final String LIST_FILE_PARAMETER = "/files/fruitmix/list";

    public static final String DOWNLOAD_FILE_PARAMETER = "/files/fruitmix/download";

    public static final String TAG = StationFileDataSourceImpl.class.getSimpleName();

    private IHttpFileUtil iHttpFileUtil;

    private static StationFileDataSource instance;

    public static StationFileDataSource getInstance(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory, IHttpFileUtil iHttpFileUtil) {

        if (instance == null)
            instance = new StationFileDataSourceImpl(iHttpUtil,httpRequestFactory,iHttpFileUtil);

        return instance;
    }


    private StationFileDataSourceImpl(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory, IHttpFileUtil iHttpFileUtil) {
        super(iHttpUtil, httpRequestFactory);
        this.iHttpFileUtil = iHttpFileUtil;
    }

    public void getFile(String folderUUID, String rootUUID, BaseLoadDataCallback<AbstractRemoteFile> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(LIST_FILE_PARAMETER + "/" + folderUUID + "/" + rootUUID);

        wrapper.loadCall(httpRequest, callback, new RemoteFileFolderParser());

    }

    @Override
    public void downloadFile(FileDownloadState fileDownloadState, BaseOperateDataCallback<FileDownloadItem> callback) throws MalformedURLException, IOException, SocketTimeoutException {

        //TODO:add file state(downloading,pending,finishing.etc) and scheduler,use state mode and do function:1.log child node 2.log parent node 3.find node and return

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(DOWNLOAD_FILE_PARAMETER + "/" + fileDownloadState.getParentFolderUUID() + "/" + fileDownloadState.getFileUUID());

        ResponseBody responseBody = iHttpFileUtil.downloadFile(httpRequest);

        Log.d(TAG, "call: downloadFile");

        boolean result = FileUtil.writeResponseBodyToFolder(responseBody, fileDownloadState);

        Log.d(TAG, "call: download result:" + result);

        if (result)
            callback.onSucceed(fileDownloadState.getFileDownloadItem(), new OperationSuccess());
        else
            callback.onFail(new OperationIOException());

    }

}
