package com.winsun.fruitmix.command;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.download.DownloadedFileWrapper;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;

/**
 * Created by Administrator on 2016/11/18.
 */

public class DeleteDownloadedFileCommand extends AbstractCommand {

    private Collection<DownloadedFileWrapper> files;

    private String currentUserUUID;

    private StationFileRepository stationFileRepository;

    public DeleteDownloadedFileCommand(Collection<DownloadedFileWrapper> files, String currentUserUUID, StationFileRepository stationFileRepository) {
        this.files = files;
        this.currentUserUUID = currentUserUUID;
        this.stationFileRepository = stationFileRepository;
    }

    @Override
    public void execute() {

        stationFileRepository.deleteFileFinishedTaskItems(files, currentUserUUID, new BaseOperateDataCallback<Void>() {
            @Override
            public void onSucceed(Void data, OperationResult result) {

                EventBus.getDefault().post(new OperationEvent(Util.DOWNLOADED_FILE_DELETED, new OperationSuccess(R.string.delete_text)));

            }

            @Override
            public void onFail(OperationResult result) {

                EventBus.getDefault().post(new OperationEvent(Util.DOWNLOADED_FILE_DELETED, result));
            }
        });

//        EventBus.getDefault().post(new DeleteDownloadedRequestEvent(OperationType.DELETE, OperationTargetType.DOWNLOADED_FILE, fileUUIDs));
    }

    @Override
    public void unExecute() {

    }
}
