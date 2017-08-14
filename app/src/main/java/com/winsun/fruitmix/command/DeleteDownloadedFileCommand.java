package com.winsun.fruitmix.command;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.DeleteDownloadedRequestEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.model.OperationTargetType;
import com.winsun.fruitmix.model.OperationType;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2016/11/18.
 */

public class DeleteDownloadedFileCommand extends AbstractCommand {

    private List<String> fileUUIDs;

    private String currentUserUUID;

    private StationFileRepository stationFileRepository;

    public DeleteDownloadedFileCommand(List<String> fileUUIDs, String currentUserUUID, StationFileRepository stationFileRepository) {
        this.fileUUIDs = fileUUIDs;
        this.currentUserUUID = currentUserUUID;
        this.stationFileRepository = stationFileRepository;
    }

    @Override
    public void execute() {

        ThreadManager threadManager = ThreadManager.getInstance();

        threadManager.runOnCacheThread(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                stationFileRepository.deleteDownloadedFileRecord(fileUUIDs, currentUserUUID);

                EventBus.getDefault().post(new OperationEvent(Util.DOWNLOADED_FILE_DELETED, new OperationSuccess(R.string.delete_text)));

                return true;
            }
        });


//        EventBus.getDefault().post(new DeleteDownloadedRequestEvent(OperationType.DELETE, OperationTargetType.DOWNLOADED_FILE, fileUUIDs));
    }

    @Override
    public void unExecute() {

    }
}
