package com.winsun.fruitmix.executor;

import com.winsun.fruitmix.callback.BaseOperateDataCallbackImpl;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.station.StationFileRepository;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2016/11/3.
 */

public class DownloadFileTask implements Callable<Boolean> {

    public static final String TAG = DownloadFileTask.class.getSimpleName();

    private FileDownloadState fileDownloadState;

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;

    public DownloadFileTask(FileDownloadState fileDownloadState, StationFileRepository stationFileRepository,String currentUserUUID) {
        this.fileDownloadState = fileDownloadState;
        this.stationFileRepository = stationFileRepository;

        this.currentUserUUID = currentUserUUID;
    }

    @Override
    public Boolean call() throws Exception {

        //TODO:add file state(downloading,pending,finishing.etc) and scheduler,use state mode and do function:1.log child node 2.log parent node 3.find node and return

        stationFileRepository.downloadFile(currentUserUUID,fileDownloadState,new BaseOperateDataCallbackImpl<FileDownloadItem>());

        return true;

/*        String downloadFileUrl = FNAS.getDownloadFileUrl(fileDownloadState.getFileUUID(), fileDownloadState.getParentFolderUUID());

        HttpRequest httpRequest = new HttpRequest(downloadFileUrl, Util.HTTP_GET_METHOD);
        httpRequest.setHeader(Util.KEY_AUTHORIZATION, Util.KEY_JWT_HEAD + FNAS.JWT);

        ResponseBody responseBody = OkHttpUtil.getInstance().getResponseBody(httpRequest);

        Log.d(TAG, "call: getResponseBody");

        boolean result = FileUtil.writeResponseBodyToFolder(responseBody, fileDownloadState);

        Log.d(TAG, "call: download result:" + result);

        if (result) {

            FileDownloadItem fileDownloadItem = fileDownloadState.getFileDownloadItem();

            DownloadedItem downloadedItem = new DownloadedItem(fileDownloadItem);

            downloadedItem.setFileTime(System.currentTimeMillis());
            downloadedItem.setFileCreatorUUID(FNAS.userUUID);
            dbUtils.insertDownloadedFile(downloadedItem);
        }

        return result;*/
    }

}

