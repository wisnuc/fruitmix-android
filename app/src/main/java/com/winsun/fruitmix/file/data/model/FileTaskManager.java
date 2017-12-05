package com.winsun.fruitmix.file.data.model;

import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.file.data.download.FileDownloadFinishedState;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileDownloadPendingState;
import com.winsun.fruitmix.file.data.download.FileDownloadState;
import com.winsun.fruitmix.file.data.download.FileStartDownloadState;
import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.file.data.station.InjectStationFileRepository;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.data.upload.FileStartUploadState;
import com.winsun.fruitmix.file.data.upload.FileUploadFinishedState;
import com.winsun.fruitmix.file.data.upload.FileUploadItem;
import com.winsun.fruitmix.file.data.upload.FileUploadPendingState;
import com.winsun.fruitmix.file.data.upload.FileUploadState;
import com.winsun.fruitmix.file.data.upload.InjectUploadFileCase;
import com.winsun.fruitmix.file.data.upload.UploadFileUseCase;
import com.winsun.fruitmix.network.InjectNetworkStateManager;
import com.winsun.fruitmix.network.NetworkStateManager;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.thread.manage.ThreadManagerImpl;
import com.winsun.fruitmix.util.FileTool;
import com.winsun.fruitmix.util.Util;

import java.io.File;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/11/7.
 */

public class FileTaskManager {

    private static final String TAG = FileTaskManager.class.getSimpleName();

    private static FileTaskManager instance;

    private List<FileTaskItem> fileTaskItems;

    private static int FILE_DOWNLOADING_MAX_NUM = 3;

    private FileTaskManager() {

        fileTaskItems = new ArrayList<>();

    }

    public synchronized static FileTaskManager getInstance() {

        if (instance == null)
            instance = new FileTaskManager();

        return instance;
    }

    public void addFinishedFileTaskItem(FileTaskItem fileTaskItem) {

        if (checkIsAlreadyDownloadingState(fileTaskItem.getFileUUID())) return;

        if (getFileTaskItemByName(fileTaskItem.getFileName()) != null) return;

        fileTaskItems.add(fileTaskItem);

        if (fileTaskItem instanceof FileUploadItem) {

            ((FileUploadItem) fileTaskItem).setFileUploadState(new FileUploadFinishedState((FileUploadItem) fileTaskItem));

        } else if (fileTaskItem instanceof FileDownloadItem) {

            ((FileDownloadItem) fileTaskItem).setFileDownloadState(new FileDownloadFinishedState((FileDownloadItem) fileTaskItem));

        }

    }

    public void addFileDownloadItem(FileDownloadItem fileDownloadItem, StationFileRepository stationFileRepository,
                                    NetworkStateManager networkStateManager, String currentUserUUID) {

        if (checkIsAlreadyDownloadingStateOrDownloadedState(fileDownloadItem.getFileUUID())) return;

        FileDownloadState fileDownloadState;

        if (checkDownloadingItemIsMax()) {

            fileDownloadState = new FileDownloadPendingState(fileDownloadItem, stationFileRepository, currentUserUUID,
                    networkStateManager);

        } else {

            fileDownloadState = new FileStartDownloadState(fileDownloadItem, stationFileRepository,
                    ThreadManagerImpl.getInstance(), currentUserUUID, networkStateManager);
        }

        // must first add and then set,because setFileDownloadState will call notifyDownloadStateChanged,update ui using fileTaskItems

        fileTaskItems.add(fileDownloadItem);

        fileDownloadItem.setFileDownloadState(fileDownloadState);

        Log.d(TAG, "addFileDownloadItem: " + fileDownloadItem.getFileName());

    }

    public void initPendingUploadItem(UploadFileUseCase uploadFileUseCase, NetworkStateManager networkStateManager,
                                      String temporaryUploadFolderPath, String currentUserUUID, FileTool fileTool) {

        File file = new File(fileTool.getTemporaryUploadFolderPath(temporaryUploadFolderPath, currentUserUUID));

        if (file.exists() && file.isDirectory()) {

            String[] files = file.list();

            for (String fileChildPath : files) {

                String uploadFilePath = file.getAbsolutePath() + File.separator + fileChildPath;

                Log.d(TAG, "initPendingUploadItem: file path: " + uploadFilePath);

                addFileUploadItem(uploadFilePath, uploadFileUseCase, networkStateManager, false);

            }

        }

    }

    public void addFileUploadItem(String uploadFilePath, UploadFileUseCase uploadFileUseCase, NetworkStateManager networkStateManager,
                                  boolean startUploadImmediately) {

        File file = new File(uploadFilePath);

        String fileHash = Util.calcSHA256OfFile(uploadFilePath);

        addFileUploadItem(new FileUploadItem(fileHash, file.getName(), file.length(), uploadFilePath), uploadFileUseCase, networkStateManager, startUploadImmediately);

    }

    private void addFileUploadItem(FileUploadItem fileUploadItem, UploadFileUseCase uploadFileUseCase, NetworkStateManager networkStateManager,
                                   boolean startUploadImmediately) {

        if (checkIsAlreadyDownloadingState(fileUploadItem.getFileUUID())) return;

        String fileOriginalName = fileUploadItem.getFileName();
        String fileName = fileOriginalName;

        while (true) {

            int renameCode = 0;

            if (getPendingFileTaskItemByName(fileName) != null) {

                fileName = uploadFileUseCase.renameFileName(++renameCode, fileOriginalName);

            } else
                break;

        }

        fileUploadItem.setFileName(fileName);

        FileUploadState fileUploadState;

/*        if (checkDownloadingItemIsMax()) {
            fileUploadState = new FileUploadPendingState(fileUploadItem, InjectUploadFileCase.provideInstance(context),
                    InjectNetworkStateManager.provideNetworkStateManager(context));
        } else {
            fileUploadState = new FileStartUploadState(fileUploadItem, ThreadManagerImpl.getInstance(),
                    InjectUploadFileCase.provideInstance(context), InjectNetworkStateManager.provideNetworkStateManager(context));
        }*/

        if (startUploadImmediately)
            fileUploadState = new FileStartUploadState(fileUploadItem, ThreadManagerImpl.getInstance(),
                    uploadFileUseCase, networkStateManager);
        else
            fileUploadState = new FileUploadPendingState(fileUploadItem, uploadFileUseCase, networkStateManager);

        fileTaskItems.add(fileUploadItem);

        fileUploadItem.setFileUploadState(fileUploadState);

        Log.d(TAG, "addFileUploadItem: " + fileUploadItem.getFilePath() + " fileName: " + fileUploadItem.getFileName());

    }

    public void deleteFileTaskItem(List<String> fileUnionKeys) {

        for (String fileUnionKey : fileUnionKeys) {

            Iterator<FileTaskItem> itemIterator = fileTaskItems.iterator();
            while (itemIterator.hasNext()) {
                FileTaskItem fileTaskItem = itemIterator.next();

                if (checkFileTaskItem(fileTaskItem, fileUnionKey))
                    itemIterator.remove();

            }

        }

    }

    private boolean checkDownloadingItemIsMax() {

        int downloadingItem = 0;

        for (FileTaskItem fileTaskItem : fileTaskItems) {
            if (fileTaskItem.getTaskState() == TaskState.DOWNLOADING_OR_UPLOADING || fileTaskItem.getTaskState() == TaskState.START_DOWNLOAD_OR_UPLOAD) {
                downloadingItem++;
            }
            if (downloadingItem == FILE_DOWNLOADING_MAX_NUM) {
                return true;
            }
        }

        return false;

    }

    private boolean checkIsAlreadyDownloadingState(String fileUnionKey) {

        FileTaskItem fileTaskItem = getFileTaskItem(fileUnionKey);

        return fileTaskItem != null && (fileTaskItem.getTaskState() == TaskState.START_DOWNLOAD_OR_UPLOAD ||
                fileTaskItem.getTaskState() == TaskState.DOWNLOADING_OR_UPLOADING);

    }

    private boolean checkIsAlreadyDownloadingStateOrDownloadedState(String fileUnionKey) {

        FileTaskItem fileTaskItem = getFileTaskItem(fileUnionKey);

        return fileTaskItem != null && (fileTaskItem.getTaskState() == TaskState.START_DOWNLOAD_OR_UPLOAD ||
                fileTaskItem.getTaskState() == TaskState.DOWNLOADING_OR_UPLOADING || fileTaskItem.getTaskState() == TaskState.FINISHED);

    }

    public boolean checkIsDownloaded(String fileUnionKey) {

        FileTaskItem fileTaskItem = getFileTaskItem(fileUnionKey);

        return fileTaskItem != null && fileTaskItem.getTaskState() == TaskState.FINISHED;

    }

    /**
     * get file task item by fileUnionKey or FilePath
     *
     * @param fileUnionKey file uuid for download or file path for upload
     * @return
     */
    public FileTaskItem getFileTaskItem(String fileUnionKey) {

        for (FileTaskItem fileTaskItem : fileTaskItems) {

            if (checkFileTaskItem(fileTaskItem, fileUnionKey))
                return fileTaskItem;

        }

        return null;

    }

    private FileTaskItem getFileTaskItemByName(String fileName) {

        for (FileTaskItem fileTaskItem : fileTaskItems) {

            if (fileTaskItem.getFileName().equals(fileName))
                return fileTaskItem;

        }

        return null;

    }

    private FileTaskItem getPendingFileTaskItemByName(String fileName) {

        for (FileTaskItem fileTaskItem : fileTaskItems) {

            if (fileTaskItem.getFileName().equals(fileName) && fileTaskItem.getTaskState() == TaskState.PENDING)
                return fileTaskItem;

        }

        return null;

    }


    private boolean checkFileTaskItem(FileTaskItem fileTaskItem, String fileUnionKey) {

        return fileTaskItem.getUnionKey().equals(fileUnionKey);

    }


    public void startPendingTaskItem() {

        for (FileTaskItem fileTaskItem : fileTaskItems) {

            if (fileTaskItem.getTaskState() == TaskState.PENDING) {

                if (fileTaskItem instanceof FileDownloadItem && !checkDownloadingItemIsMax()) {

                    FileDownloadItem fileDownloadItem = (FileDownloadItem) fileTaskItem;

                    FileDownloadPendingState state = (FileDownloadPendingState) fileDownloadItem.getFileDownloadState();

                    Log.d(TAG, "startPendingTaskItem: " + fileDownloadItem.getFileName());

                    fileDownloadItem.setFileDownloadState(new FileStartDownloadState(fileDownloadItem,
                            state.getStationFileRepository(), ThreadManagerImpl.getInstance(), state.getCurrentUserUUID(),
                            state.getNetworkStateManager()));

                } else if (fileTaskItem instanceof FileUploadItem) {

                    FileUploadItem fileUploadItem = (FileUploadItem) fileTaskItem;

                    FileUploadPendingState fileUploadPendingState = (FileUploadPendingState) fileUploadItem.getFileUploadState();

                    Log.d(TAG, "startPendingTaskItem: " + fileUploadItem.getFilePath());

                    fileUploadItem.setFileUploadState(new FileStartUploadState(fileUploadItem, ThreadManagerImpl.getInstance(),
                            fileUploadPendingState.getUploadFileUseCase(), fileUploadPendingState.getNetworkStateManager()));
                }

            }

        }

    }

    public void cancelAllStartItem() {
        for (FileTaskItem fileTaskItem : fileTaskItems) {

            TaskState taskState = fileTaskItem.getTaskState();

            if (taskState == TaskState.START_DOWNLOAD_OR_UPLOAD || taskState == TaskState.DOWNLOADING_OR_UPLOADING) {

                fileTaskItem.cancelTaskItem();

            }

        }
    }

    public void cancelAllStartItemAndSetPending(Context context) {

        for (FileTaskItem fileTaskItem : fileTaskItems) {

            TaskState taskState = fileTaskItem.getTaskState();

            if (taskState == TaskState.START_DOWNLOAD_OR_UPLOAD || taskState == TaskState.DOWNLOADING_OR_UPLOADING) {

                fileTaskItem.cancelTaskItem();

                NetworkStateManager networkStateManager = InjectNetworkStateManager.provideNetworkStateManager(context);

                if (fileTaskItem instanceof FileDownloadItem) {

                    FileDownloadItem fileDownloadItem = (FileDownloadItem) fileTaskItem;

                    Log.d(TAG, "cancel start download or downloading state and set pending state,file name: " + fileDownloadItem.getFileName());

                    StationFileRepository stationFileRepository = InjectStationFileRepository.provideStationFileRepository(context);
                    String currentUserUUID = InjectSystemSettingDataSource.provideSystemSettingDataSource(context).getCurrentLoginUserUUID();

                    fileDownloadItem.setFileDownloadState(new FileDownloadPendingState(fileDownloadItem,
                            stationFileRepository, currentUserUUID,
                            networkStateManager));

                } else if (fileTaskItem instanceof FileUploadItem) {

                    UploadFileUseCase uploadFileUseCase = InjectUploadFileCase.provideInstance(context);

                    FileUploadItem fileUploadItem = (FileUploadItem) fileTaskItem;

                    Log.d(TAG, "cancel start upload or uploading state and set pending state,file path: " + fileUploadItem.getFilePath());

                    fileUploadItem.setFileUploadState(new FileUploadPendingState(fileUploadItem,
                            uploadFileUseCase, networkStateManager));

                }

            }

        }

    }

    public List<FileTaskItem> getFileTaskItems() {

        return Collections.unmodifiableList(fileTaskItems);
    }

    public void clearFileTaskItems() {
        if (fileTaskItems != null) {
            fileTaskItems.clear();
        }
    }
}
