package com.winsun.fruitmix.executor;

import com.winsun.fruitmix.file.data.upload.FileUploadState;
import com.winsun.fruitmix.file.data.upload.UploadFileUseCase;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2017/11/15.
 */

public class UploadFileTask implements Callable<Boolean> {

    private FileUploadState fileUploadState;
    private UploadFileUseCase uploadFileUseCase;

    public UploadFileTask(FileUploadState fileUploadState,UploadFileUseCase uploadFileUseCase) {
        this.fileUploadState = fileUploadState;
        this.uploadFileUseCase = uploadFileUseCase;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Boolean call() throws Exception {

        uploadFileUseCase.updateFile(fileUploadState);

        return true;
    }
}
