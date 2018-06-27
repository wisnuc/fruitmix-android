package com.winsun.fruitmix.newdesign201804.file.list.data;

import android.support.annotation.NonNull;

import com.winsun.fruitmix.BaseDataRepository;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateCallbackImpl;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.file.data.download.param.FileDownloadParam;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.Task;
import com.winsun.fruitmix.newdesign201804.file.transmissionTask.model.TaskState;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FileDataRepository extends BaseDataRepository implements FileDataSource {

    private StationFileRepository mStationFileRepository;

    private FileNewOperateDataSource mFileNewOperateDataSource;

    private static FileDataRepository instance = null;

    private FileDataRepository(ThreadManager threadManager, StationFileRepository stationFileRepository,
                               FileNewOperateDataSource fileNewOperateDataSource) {
        super(threadManager);
        mStationFileRepository = stationFileRepository;
        mFileNewOperateDataSource = fileNewOperateDataSource;
    }

    public static FileDataRepository getInstance(ThreadManager threadManager, StationFileRepository stationFileRepository,
                                                 FileNewOperateDataSource fileNewOperateDataSource) {
        if (instance == null)
            instance = new FileDataRepository(threadManager, stationFileRepository, fileNewOperateDataSource);

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    @Override
    public void getFile(@NotNull String rootUUID, @NonNull String folderUUID, @NonNull String folderName, @NotNull BaseLoadDataCallback<AbstractRemoteFile> baseLoadDataCallback) {
        mStationFileRepository.getFile(rootUUID, folderUUID,folderName, baseLoadDataCallback);
    }

    @Override
    public void searchFile(@NotNull List<String> keys, @NotNull BaseLoadDataCallback<AbstractRemoteFile> baseLoadDataCallback) {

    }

    @Override
    public void getRootDrive(@NotNull BaseLoadDataCallback<AbstractRemoteFile> baseLoadDataCallback) {
        mStationFileRepository.getRootDrive(baseLoadDataCallback);
    }

    @Override
    public void createFolder(@NotNull String folderName, @NotNull String driveUUID, @NotNull String dirUUID, @NotNull BaseOperateDataCallback<HttpResponse> callback) {
        mStationFileRepository.createFolder(folderName, driveUUID, dirUUID, callback);
    }

    @Override
    public void renameFile(@NotNull final String oldName, @NotNull final String newName, @NotNull final String driveUUID, @NotNull final String dirUUID, @NotNull final BaseOperateCallback callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mFileNewOperateDataSource.renameFile(oldName, newName, driveUUID, dirUUID,
                        createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void copyFile(@NotNull final AbstractRemoteFile srcFolder, @NotNull final AbstractRemoteFile targetFolder,
                         @NotNull final List<? extends AbstractRemoteFile> entries, @NotNull final BaseOperateDataCallback<Task> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mFileNewOperateDataSource.copyFile(srcFolder, targetFolder, entries, createOperateCallbackRunOnMainThread(callback));
            }
        });


    }

    @Override
    public void moveFile(@NotNull final AbstractRemoteFile srcFolder, @NotNull final AbstractRemoteFile targetFolder,
                         @NotNull final List<? extends AbstractRemoteFile> entries, @NotNull final BaseOperateDataCallback<Task> callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mFileNewOperateDataSource.moveFile(srcFolder, targetFolder, entries, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }

    @Override
    public void downloadFile(@NotNull FileDownloadParam fileDownloadParam, @NotNull Task task) {

        mFileNewOperateDataSource.downloadFile(fileDownloadParam, task, new BaseOperateCallbackImpl());

    }

    @Override
    public void uploadFile(@NotNull FileUploadParam fileUploadParam, @NotNull Task task) {
        mFileNewOperateDataSource.uploadFile(fileUploadParam, task);
    }

    @Override
    public void deleteFile(@NotNull final String fileName, @NotNull final String driveUUID, @NotNull final String dirUUID, @NotNull final BaseOperateCallback callback) {

        mThreadManager.runOnCacheThread(new Runnable() {
            @Override
            public void run() {
                mFileNewOperateDataSource.deleteFile(fileName, driveUUID, dirUUID, createOperateCallbackRunOnMainThread(callback));
            }
        });

    }
}
