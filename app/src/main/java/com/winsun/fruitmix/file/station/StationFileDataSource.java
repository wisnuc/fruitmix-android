package com.winsun.fruitmix.file.station;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.parser.RemoteFileFolderParser;

/**
 * Created by Administrator on 2017/7/19.
 */

public class StationFileDataSource extends BaseRemoteDataSourceImpl {

    public static final String LIST_FILE_PARAMETER = "/files/fruitmix/list";

    public StationFileDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    public void getFile(String folderUUID, String rootUUID, BaseLoadDataCallback<AbstractRemoteFile> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetRequest(LIST_FILE_PARAMETER + "/" + folderUUID + "/" + rootUUID);

        wrapper.loadCall(httpRequest, callback, new RemoteFileFolderParser());

    }


}
