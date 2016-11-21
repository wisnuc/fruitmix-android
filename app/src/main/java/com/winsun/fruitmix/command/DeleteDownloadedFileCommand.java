package com.winsun.fruitmix.command;

import com.winsun.fruitmix.eventbus.DeleteDownloadedRequestEvent;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Administrator on 2016/11/18.
 */

public class DeleteDownloadedFileCommand extends AbstractCommand {

    private List<String> fileUUIDs;

    public DeleteDownloadedFileCommand(List<String> fileUUIDs) {
        this.fileUUIDs = fileUUIDs;
    }

    @Override
    public void execute() {

        EventBus.getDefault().post(new DeleteDownloadedRequestEvent(OperationType.DELETE, OperationTargetType.DOWNLOADED_FILE, fileUUIDs));
    }

    @Override
    public void unExecute() {

    }
}
