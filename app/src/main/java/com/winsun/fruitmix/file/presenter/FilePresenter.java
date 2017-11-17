package com.winsun.fruitmix.file.presenter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.command.ChangeToDownloadPageCommand;
import com.winsun.fruitmix.command.DownloadFileCommand;
import com.winsun.fruitmix.command.MacroCommand;
import com.winsun.fruitmix.command.NullCommand;
import com.winsun.fruitmix.command.OpenFileCommand;
import com.winsun.fruitmix.command.ShowSelectModeViewCommand;
import com.winsun.fruitmix.command.ShowUnSelectModeViewCommand;
import com.winsun.fruitmix.databinding.RemoteFileItemLayoutBinding;
import com.winsun.fruitmix.databinding.RemoteFolderItemLayoutBinding;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.eventbus.TaskStateChangedEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.data.download.TaskState;
import com.winsun.fruitmix.file.data.download.DownloadedFileWrapper;
import com.winsun.fruitmix.file.data.download.FileDownloadItem;
import com.winsun.fruitmix.file.data.download.FileTaskManager;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.file.data.model.FileTaskItem;
import com.winsun.fruitmix.file.data.model.RemoteFile;
import com.winsun.fruitmix.file.data.model.RemoteFolder;
import com.winsun.fruitmix.file.data.model.RemotePrivateDrive;
import com.winsun.fruitmix.file.data.model.RemotePublicDrive;
import com.winsun.fruitmix.file.data.station.StationFileRepository;
import com.winsun.fruitmix.file.view.FileDownloadActivity;
import com.winsun.fruitmix.file.view.interfaces.FileListSelectModeListener;
import com.winsun.fruitmix.file.view.interfaces.FileView;
import com.winsun.fruitmix.file.view.interfaces.HandleFileListOperateCallback;
import com.winsun.fruitmix.file.view.viewmodel.FileItemViewModel;
import com.winsun.fruitmix.file.view.viewmodel.FileViewModel;
import com.winsun.fruitmix.interfaces.OnViewSelectListener;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.video.PlayVideoActivity;
import com.winsun.fruitmix.viewholder.BaseBindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/7/27.
 */

public class FilePresenter implements OnViewSelectListener, ActiveView {

    private static final String SHARED_DRIVE_UUID = "shared_drive_uuid";

    private static String ROOT_DRIVE_UUID = "root_drive_uuid";

    private FileRecyclerViewAdapter fileRecyclerViewAdapter;

    private List<AbstractRemoteFile> abstractRemoteFiles;

    private boolean remoteFileLoaded = false;

    private String currentFolderUUID;
    private String currentFolderName;

    private List<String> retrievedFolderUUIDList;

    private List<String> retrievedFolderNameList;

    private boolean selectMode = false;

    private List<AbstractRemoteFile> selectedFiles;

    private AbstractCommand showUnSelectModeViewCommand;

    private AbstractCommand showSelectModeViewCommand;

    private AbstractCommand nullCommand;

    private String rootUUID;

    private String driveRootUUID;

    private List<AbstractRemoteFile> sharedDriveFiles;

    private List<String> alreadyDownloadedFilePathForShare;

    private List<AbstractRemoteFile> needDownloadFilesForShare;

    private ProgressDialog currentDownloadFileForShareProgressDialog;

    private MacroCommand currentDownloadFileForShareCommand;

    private ProgressDialog currentDownloadFileProgressDialog;

    private int progressMax = 100;

    private DownloadFileCommand mCurrentDownloadFileCommand;

    private boolean cancelDownload = false;

    private NoContentViewModel noContentViewModel;
    private LoadingViewModel loadingViewModel;

    private FileViewModel fileViewModel;

    private Activity activity;

    private FileView fileView;

    private HandleFileListOperateCallback handleFileListOperateCallback;

    private ChangeToDownloadPageCommand.ChangeToDownloadPageCallback changeToDownloadPageCallback;

    private StationFileRepository stationFileRepository;

    private String currentUserUUID;

    private FileTaskManager fileTaskManager;

    private UserDataRepository userDataRepository;

    private SystemSettingDataSource systemSettingDataSource;

    private FileListSelectModeListener fileListSelectModeListener;

    public FilePresenter(final Activity activity, FileView fileView, FileListSelectModeListener fileListSelectModeListener, StationFileRepository stationFileRepository, NoContentViewModel noContentViewModel, LoadingViewModel loadingViewModel, FileViewModel fileViewModel,
                         HandleFileListOperateCallback handleFileListOperateCallback, UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource, FileTaskManager fileTaskManager) {
        this.activity = activity;
        this.fileView = fileView;
        this.fileListSelectModeListener = fileListSelectModeListener;
        this.stationFileRepository = stationFileRepository;
        this.noContentViewModel = noContentViewModel;
        this.loadingViewModel = loadingViewModel;
        this.fileViewModel = fileViewModel;
        this.handleFileListOperateCallback = handleFileListOperateCallback;
        this.fileTaskManager = fileTaskManager;

        this.userDataRepository = userDataRepository;
        this.systemSettingDataSource = systemSettingDataSource;

        initCurrentUserUUIDAndRootUUID(userDataRepository, systemSettingDataSource);

        showUnSelectModeViewCommand = new ShowUnSelectModeViewCommand(this);

        showSelectModeViewCommand = new ShowSelectModeViewCommand(this);

        nullCommand = new NullCommand();

        changeToDownloadPageCallback = new ChangeToDownloadPageCommand.ChangeToDownloadPageCallback() {
            @Override
            public void changeToDownloadPage() {
                activity.startActivity(new Intent(activity, FileDownloadActivity.class));
            }
        };

        fileRecyclerViewAdapter = new FileRecyclerViewAdapter();

        init();
    }

    private void initCurrentUserUUIDAndRootUUID(UserDataRepository userDataRepository, SystemSettingDataSource systemSettingDataSource) {
        currentUserUUID = systemSettingDataSource.getCurrentLoginUserUUID();
//        rootUUID = userDataRepository.getUserByUUID(currentUserUUID).getHome();

        rootUUID = ROOT_DRIVE_UUID;
    }

    private void init() {
        abstractRemoteFiles = new ArrayList<>();

        retrievedFolderUUIDList = new ArrayList<>();
        retrievedFolderNameList = new ArrayList<>();

        selectedFiles = new ArrayList<>();

        currentFolderUUID = rootUUID;
        currentFolderName = activity.getString(R.string.file);

        driveRootUUID = "";

        sharedDriveFiles = new ArrayList<>();
    }


    private class ReDownloadCommand extends AbstractCommand {

        private AbstractRemoteFile abstractRemoteFile;

        ReDownloadCommand(AbstractRemoteFile abstractRemoteFile) {
            this.abstractRemoteFile = abstractRemoteFile;
        }

        @Override
        public void execute() {

            File file = new File(FileUtil.getDownloadFileStoreFolderPath(), abstractRemoteFile.getName());

            if (file.exists()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                builder.setMessage(activity.getString(R.string.need_download_and_cover_original_file)).setPositiveButton(activity.getString(R.string.cover),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DownloadedFileWrapper downloadedFileWrapper = new DownloadedFileWrapper(abstractRemoteFile.getUuid(), abstractRemoteFile.getName());

                                stationFileRepository.deleteDownloadedFile(Collections.singleton(downloadedFileWrapper), currentUserUUID, new BaseOperateDataCallback<Void>() {
                                    @Override
                                    public void onSucceed(Void data, OperationResult result) {

                                        MacroCommand macroCommand = createDownloadFileMarcoCommand(abstractRemoteFile);

                                        macroCommand.execute();
                                    }

                                    @Override
                                    public void onFail(OperationResult result) {

                                        Toast.makeText(activity, activity.getString(R.string.delete_original_file_fail), Toast.LENGTH_SHORT).show();

                                    }
                                });


                            }
                        }).setNegativeButton(activity.getString(R.string.cancel), null)
                        .setCancelable(false).create().show();


            } else {

                MacroCommand macroCommand = createDownloadFileMarcoCommand(abstractRemoteFile);

                macroCommand.execute();
            }

        }

        @Override
        public void unExecute() {

        }
    }

    private MacroCommand createDownloadFileMarcoCommand(AbstractRemoteFile abstractRemoteFile) {

        MacroCommand macroCommand = new MacroCommand();

        AbstractCommand downloadFileCommand = new DownloadFileCommand(fileTaskManager, abstractRemoteFile, stationFileRepository, currentUserUUID, driveRootUUID);
        macroCommand.addCommand(downloadFileCommand);
        macroCommand.addCommand(new ChangeToDownloadPageCommand(changeToDownloadPageCallback));

        return macroCommand;
    }


    public FileRecyclerViewAdapter getFileRecyclerViewAdapter() {
        return fileRecyclerViewAdapter;
    }

    public void refreshView(boolean force) {

        if (force)
            remoteFileLoaded = false;

        if (!remoteFileLoaded) {

            initCurrentUserUUIDAndRootUUID(userDataRepository, systemSettingDataSource);

            if (force)
                init();

            if (!retrievedFolderUUIDList.contains(currentFolderUUID)) {
                retrievedFolderUUIDList.add(currentFolderUUID);
                retrievedFolderNameList.add(currentFolderName);
            }

            getFile();

        }

    }

    public void refreshCurrentFolder() {

        fileViewModel.swipeRefreshEnabled.set(true);

        getFileInThread();

    }

    private void getFile() {

        noContentViewModel.showNoContent.set(false);

        loadingViewModel.showLoading.set(true);

        fileViewModel.swipeRefreshEnabled.set(false);

        fileListSelectModeListener.onFileSelectOperationUnavailable();

        getFileInThread();

    }

    private void getFileInThread() {

        if (currentFolderUUID.equals(ROOT_DRIVE_UUID)) {

            stationFileRepository.getRootDrive(new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<AbstractRemoteFile>() {
                @Override
                public void onSucceed(List<AbstractRemoteFile> data, OperationResult operationResult) {

                    handleGetFileSucceed(handleRootDriveUUID(data), false);

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    handleGetFileFail();
                }
            }, this));

        } else if (currentFolderUUID.equals(SHARED_DRIVE_UUID)) {

            handleGetFileSucceed(sharedDriveFiles, false);

        } else {

            stationFileRepository.getFile(driveRootUUID, currentFolderUUID, new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<AbstractRemoteFile>() {
                @Override
                public void onSucceed(final List<AbstractRemoteFile> data, OperationResult operationResult) {

                    handleGetFileSucceed(data, true);

                }

                @Override
                public void onFail(OperationResult operationResult) {

                    if (operationResult instanceof OperationNetworkException) {
                        Toast.makeText(activity, operationResult.getResultMessage(activity), Toast.LENGTH_SHORT).show();
                    }

                    handleGetFileFail();

                }
            }, this));

        }

    }

    private List<AbstractRemoteFile> handleRootDriveUUID(List<AbstractRemoteFile> data) {

        sharedDriveFiles.clear();

        List<AbstractRemoteFile> result = new ArrayList<>();

        for (AbstractRemoteFile file : data) {

            if (file instanceof RemotePrivateDrive) {
                file.setName(activity.getString(R.string.my_file));
                result.add(file);
            } else if (file instanceof RemotePublicDrive) {

                if (file.getWriteList().contains(currentUserUUID))
                    sharedDriveFiles.add(file);

            }

        }

        if (sharedDriveFiles.size() != 0) {

            AbstractRemoteFile file = new RemotePublicDrive();
            file.setName(activity.getString(R.string.shared_drive));
            file.setUuid(SHARED_DRIVE_UUID);

            result.add(file);
        }

        return result;

    }

    private void handleGetFileFail() {

        loadingViewModel.showLoading.set(false);

        fileViewModel.swipeRefreshEnabled.set(true);
        fileView.setSwipeRefreshing(false);

        noContentViewModel.showNoContent.set(true);

        abstractRemoteFiles.clear();

        fileListSelectModeListener.onFileSelectOperationUnavailable();
    }

    private void handleGetFileSucceed(List<AbstractRemoteFile> files, boolean needSort) {

        loadingViewModel.showLoading.set(false);

        fileViewModel.swipeRefreshEnabled.set(true);
        fileView.setSwipeRefreshing(false);

        remoteFileLoaded = true;

        if (files.size() == 0) {

            noContentViewModel.showNoContent.set(true);

            fileViewModel.showFileRecyclerView.set(false);

            abstractRemoteFiles.clear();

            fileListSelectModeListener.onFileSelectOperationUnavailable();

        } else {

            noContentViewModel.showNoContent.set(false);

            fileViewModel.showFileRecyclerView.set(true);

            abstractRemoteFiles.clear();
            abstractRemoteFiles.addAll(files);

            if (needSort)
                sortFile(abstractRemoteFiles);

            fileRecyclerViewAdapter.notifyDataSetChanged();

            fileListSelectModeListener.onFileSelectOperationAvailable();
        }
    }


    private void sortFile(List<AbstractRemoteFile> abstractRemoteFiles) {

        List<AbstractRemoteFile> files = new ArrayList<>();
        List<AbstractRemoteFile> folders = new ArrayList<>();

        for (AbstractRemoteFile abstractRemoteFile : abstractRemoteFiles) {
            if (abstractRemoteFile.isFolder())
                folders.add(abstractRemoteFile);
            else
                files.add(abstractRemoteFile);
        }

        Comparator<AbstractRemoteFile> comparator = new Comparator<AbstractRemoteFile>() {
            @Override
            public int compare(AbstractRemoteFile lhs, AbstractRemoteFile rhs) {
                return (int) (Long.parseLong(lhs.getTime()) - Long.parseLong(rhs.getTime()));
            }
        };

        Collections.sort(folders, comparator);
        Collections.sort(files, comparator);

        abstractRemoteFiles.clear();
        abstractRemoteFiles.addAll(folders);
        abstractRemoteFiles.addAll(files);

    }

    @Override
    public boolean isActive() {
        return activity != null;
    }

    public void handleEvent(TaskStateChangedEvent taskStateChangedEvent) {

        TaskState taskState = taskStateChangedEvent.getTaskState();

        switch (taskState) {
            case START_DOWNLOAD_OR_UPLOAD:
            case PENDING:
                break;
            case DOWNLOADING_OR_UPLOADING:

                if (mCurrentDownloadFileCommand != null) {

                    FileDownloadItem fileDownloadItem = mCurrentDownloadFileCommand.getFileDownloadItem();
                    currentDownloadFileProgressDialog.setProgress(fileDownloadItem.getCurrentProgress(progressMax));

                }

                break;
            case FINISHED:

                if (mCurrentDownloadFileCommand != null) {

                    FileDownloadItem fileDownloadItem = mCurrentDownloadFileCommand.getFileDownloadItem();

                    if (fileDownloadItem.getTaskState().equals(TaskState.FINISHED)) {

                        mCurrentDownloadFileCommand = null;

                        currentDownloadFileProgressDialog.dismiss();

                        OpenFileCommand openFileCommand = new OpenFileCommand(activity, fileDownloadItem.getFileName());
                        openFileCommand.execute();

                    }


                } else if (currentDownloadFileForShareCommand != null) {

                    checkFileForShareDownloaded(needDownloadFilesForShare);

                    if (needDownloadFilesForShare.isEmpty()) {

                        currentDownloadFileForShareCommand = null;

                        currentDownloadFileForShareProgressDialog.dismiss();

                        FileUtil.sendShareToOtherApp(activity, alreadyDownloadedFilePathForShare);
                    }

                }

                break;
            case ERROR:

                if (mCurrentDownloadFileCommand != null && mCurrentDownloadFileCommand.getFileDownloadItem().getTaskState().equals(TaskState.ERROR)) {

                    mCurrentDownloadFileCommand.unExecute();

                    mCurrentDownloadFileCommand = null;

                    currentDownloadFileProgressDialog.dismiss();

                    if (cancelDownload)
                        cancelDownload = false;
                    else
                        Toast.makeText(activity, activity.getText(R.string.download_failed), Toast.LENGTH_SHORT).show();

                } else if (currentDownloadFileForShareCommand != null) {

                    boolean occurError = false;

                    for (AbstractRemoteFile file : needDownloadFilesForShare) {

                        FileTaskItem fileTaskItem = fileTaskManager.getFileTaskItem(file.getUuid());

                        if (fileTaskItem.getTaskState().equals(TaskState.ERROR)) {
                            occurError = true;
                            break;
                        }

                    }

                    if (occurError) {

                        currentDownloadFileForShareCommand.unExecute();

                        currentDownloadFileForShareCommand = null;

                        currentDownloadFileForShareProgressDialog.dismiss();

                        Toast.makeText(activity, activity.getString(R.string.download_failed), Toast.LENGTH_SHORT).show();

                    }

                }

                break;
            case NO_ENOUGH_SPACE:

                if (mCurrentDownloadFileCommand != null) {

                    mCurrentDownloadFileCommand.unExecute();

                    mCurrentDownloadFileCommand = null;

                    currentDownloadFileProgressDialog.dismiss();

                    Toast.makeText(activity, activity.getString(R.string.no_enough_space), Toast.LENGTH_SHORT).show();

                } else if (currentDownloadFileForShareCommand != null) {

                    boolean occurNoEnoughSpace = false;

                    for (AbstractRemoteFile file : needDownloadFilesForShare) {

                        FileTaskItem fileTaskItem = fileTaskManager.getFileTaskItem(file.getUuid());

                        if (fileTaskItem.getTaskState().equals(TaskState.NO_ENOUGH_SPACE)) {
                            occurNoEnoughSpace = true;
                            break;
                        }

                    }

                    if (occurNoEnoughSpace) {

                        currentDownloadFileForShareCommand.unExecute();

                        currentDownloadFileForShareCommand = null;

                        currentDownloadFileForShareProgressDialog.dismiss();

                        Toast.makeText(activity, activity.getString(R.string.no_enough_space), Toast.LENGTH_SHORT).show();

                    }

                }

                break;
        }

    }

    public void handleOperationEvent(OperationEvent operationEvent) {

    }

    public String getCurrentFolderName() {
        return currentFolderName;
    }

    public boolean handleBackPressedOrNot() {
        return selectMode || notRootFolder();
    }

    private boolean notRootFolder() {

        return !currentFolderUUID.equals(rootUUID);
    }

    public void onBackPressed() {

        if (selectMode) {
            unSelectMode();
        } else if (notRootFolder()) {

            if (loadingViewModel.showLoading.get() && !noContentViewModel.showNoContent.get())
                return;

            retrievedFolderUUIDList.remove(retrievedFolderUUIDList.size() - 1);
            currentFolderUUID = retrievedFolderUUIDList.get(retrievedFolderUUIDList.size() - 1);

            retrievedFolderNameList.remove(retrievedFolderNameList.size() - 1);
            currentFolderName = retrievedFolderNameList.get(retrievedFolderNameList.size() - 1);

            getFile();

            handleFileListOperateCallback.handleFileListOperate(currentFolderName);

        }

    }

    public void onDestroy() {

        activity = null;

    }

    public boolean canEnterSelectMode() {

        for (AbstractRemoteFile file : abstractRemoteFiles) {

            if (file instanceof RemoteFile) {
                return true;
            }

        }
        return false;
    }

    public void quitSelectMode() {
        unSelectMode();
    }

    public void enterSelectMode() {
        selectMode();
    }

    @Override
    public void selectMode() {

        selectMode = true;
        refreshSelectMode(selectMode, null);
    }

    @Override
    public void unSelectMode() {

        selectMode = false;
        refreshSelectMode(selectMode, null);
    }

    private void refreshSelectMode(boolean selectMode, AbstractRemoteFile selectFile) {

        this.selectMode = selectMode;

        if (selectMode)
            fileViewModel.swipeRefreshEnabled.set(false);
        else
            fileViewModel.swipeRefreshEnabled.set(true);

        selectedFiles.clear();
        if (selectFile != null)
            selectedFiles.add(selectFile);

        fileRecyclerViewAdapter.notifyDataSetChanged();

    }


    public void shareSelectFilesToOtherApp(Context context) {

        alreadyDownloadedFilePathForShare = new ArrayList<>();

        needDownloadFilesForShare = new ArrayList<>();

        checkFileForShareDownloaded(selectedFiles);

        if (selectedFiles.isEmpty()) {
            FileUtil.sendShareToOtherApp(context, alreadyDownloadedFilePathForShare);
        } else {

            needDownloadFilesForShare.addAll(selectedFiles);

            currentDownloadFileForShareProgressDialog = ProgressDialog.show(context, null, String.format(context.getString(R.string.operating_title), context.getString(R.string.download_select_item)), true, true);
            currentDownloadFileForShareProgressDialog.setCanceledOnTouchOutside(false);

            currentDownloadFileForShareCommand = new MacroCommand();
            addSelectFilesToMacroCommand(currentDownloadFileForShareCommand, needDownloadFilesForShare);

            currentDownloadFileForShareCommand.execute();

        }

    }

    private void checkFileForShareDownloaded(List<AbstractRemoteFile> files) {

        Iterator<AbstractRemoteFile> iterator = files.iterator();

        while (iterator.hasNext()) {

            AbstractRemoteFile file = iterator.next();

            if (fileTaskManager.checkIsDownloaded(file.getUuid())) {

                alreadyDownloadedFilePathForShare.add(FileUtil.getDownloadFileStoreFolderPath() + file.getName());

                iterator.remove();
            }

        }

    }


    public void requestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    openFileWhenOnClick();

                } else {

                    Toast.makeText(activity, activity.getString(R.string.android_no_write_external_storage_permission), Toast.LENGTH_SHORT).show();

                }

        }

    }

    public void checkWriteExternalStoragePermission() {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);

        } else {
            openFileWhenOnClick();
        }

    }

    private void openFileWhenOnClick() {

        AbstractRemoteFile file = selectedFiles.get(0);

        FileTaskItem fileTaskItem = fileTaskManager.getFileTaskItem(file.getUuid());

        if (fileTaskItem != null &&
                (fileTaskItem.getTaskState().equals(TaskState.PENDING) || fileTaskItem.getTaskState().equals(TaskState.DOWNLOADING_OR_UPLOADING) || fileTaskItem.getTaskState().equals(TaskState.START_DOWNLOAD_OR_UPLOAD))) {

            AbstractCommand command = new ChangeToDownloadPageCommand(changeToDownloadPageCallback);
            command.execute();

            return;

        }

        mCurrentDownloadFileCommand = new DownloadFileCommand(fileTaskManager, file, stationFileRepository, currentFolderUUID, driveRootUUID);

        currentDownloadFileProgressDialog = new ProgressDialog(activity);

        currentDownloadFileProgressDialog.setTitle(activity.getString(R.string.downloading));

        currentDownloadFileProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        currentDownloadFileProgressDialog.setIndeterminate(false);

        currentDownloadFileProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCurrentDownloadFileCommand.unExecute();

                mCurrentDownloadFileCommand = null;

                cancelDownload = true;

                currentDownloadFileProgressDialog.dismiss();
            }
        });

        currentDownloadFileProgressDialog.setMax(progressMax);

        currentDownloadFileProgressDialog.setCancelable(false);

        currentDownloadFileProgressDialog.show();

        mCurrentDownloadFileCommand.execute();
    }

    public Dialog getBottomSheetDialog(List<BottomMenuItem> bottomMenuItems) {

        Dialog dialog = new BottomMenuDialogFactory(bottomMenuItems).createDialog(activity);

        for (BottomMenuItem bottomMenuItem : bottomMenuItems) {
            bottomMenuItem.setDialog(dialog);
        }

        return dialog;
    }

    public List<BottomMenuItem> getMainMenuItem() {

        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

        if (selectMode) {

            BottomMenuItem clearSelectItem = new BottomMenuItem(R.drawable.cancel, activity.getString(R.string.clear_select_item), showUnSelectModeViewCommand);

            bottomMenuItems.add(clearSelectItem);

            MacroCommand macroCommand = createDownloadSelectFilesCommand();

            BottomMenuItem downloadSelectItem = new BottomMenuItem(R.drawable.download, activity.getString(R.string.download_select_item), macroCommand);

            bottomMenuItems.add(downloadSelectItem);

        } else {

            BottomMenuItem selectItem = new BottomMenuItem(R.drawable.check, activity.getString(R.string.select_file), showSelectModeViewCommand);

            if (abstractRemoteFiles.isEmpty())
                selectItem.setDisable(true);

            bottomMenuItems.add(selectItem);
        }

        BottomMenuItem cancelMenuItem = new BottomMenuItem(R.drawable.close, activity.getString(R.string.cancel), nullCommand);

        bottomMenuItems.add(cancelMenuItem);

        return bottomMenuItems;

    }

    public void downloadSelectItems() {

        MacroCommand macroCommand = createDownloadSelectFilesCommand();

        macroCommand.execute();

    }

    private MacroCommand createDownloadSelectFilesCommand() {
        MacroCommand macroCommand = new MacroCommand();

        addSelectFilesToMacroCommand(macroCommand, selectedFiles);

        macroCommand.addCommand(showUnSelectModeViewCommand);

        macroCommand.addCommand(new ChangeToDownloadPageCommand(changeToDownloadPageCallback));

        return macroCommand;
    }

    private void addSelectFilesToMacroCommand(AbstractCommand macroCommand, List<AbstractRemoteFile> files) {

        for (AbstractRemoteFile abstractRemoteFile : files) {

            AbstractCommand abstractCommand = new DownloadFileCommand(fileTaskManager, abstractRemoteFile, stationFileRepository, currentUserUUID, driveRootUUID);
            macroCommand.addCommand(abstractCommand);

        }

    }


    class FileRecyclerViewAdapter extends RecyclerView.Adapter<BaseBindingViewHolder> {

        private static final int VIEW_FILE = 0;
        private static final int VIEW_FOLDER = 1;

        @Override
        public int getItemCount() {
            return abstractRemoteFiles.size();
        }

        @Override
        public BaseBindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ViewDataBinding binding;
            BaseBindingViewHolder viewHolder;

            switch (viewType) {
                case VIEW_FILE:

                    binding = RemoteFileItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    viewHolder = new FileViewHolder(binding);

                    break;
                case VIEW_FOLDER:

                    binding = RemoteFolderItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    viewHolder = new FolderViewHolder(binding);
                    break;
                default:
                    binding = RemoteFileItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                    viewHolder = new FileViewHolder(binding);
            }


            return viewHolder;
        }

        @Override
        public void onBindViewHolder(BaseBindingViewHolder holder, int position) {

            holder.getViewDataBinding().setVariable(BR.file, abstractRemoteFiles.get(position));

            holder.refreshView(position);

            holder.getViewDataBinding().executePendingBindings();

        }

        @Override
        public int getItemViewType(int position) {

            return abstractRemoteFiles.get(position).isFolder() ? VIEW_FOLDER : VIEW_FILE;

        }

    }

    class FolderViewHolder extends BaseBindingViewHolder {

        LinearLayout folderItemLayout;

        RelativeLayout contentLayout;

        private RemoteFolderItemLayoutBinding binding;

        FolderViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (RemoteFolderItemLayoutBinding) viewDataBinding;

            contentLayout = binding.contentLayout;

            folderItemLayout = binding.remoteFolderItemLayout;

        }

        @Override
        public void refreshView(int position) {

            if (position == 0) {

                Util.setMargin(contentLayout, 0, Util.dip2px(activity, 8), Util.dip2px(activity, 16), 0);

            }

            final RemoteFolder abstractRemoteFile = (RemoteFolder) abstractRemoteFiles.get(position);

            FileItemViewModel fileItemViewModel = binding.getFileItemViewModel();

            if (fileItemViewModel == null)
                fileItemViewModel = new FileItemViewModel();

            fileItemViewModel.selectMode.set(selectMode);

            folderItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (selectMode)
                        return;

                    currentFolderUUID = abstractRemoteFile.getUuid();

                    retrievedFolderUUIDList.add(currentFolderUUID);

                    currentFolderName = abstractRemoteFile.getName();

                    retrievedFolderNameList.add(currentFolderName);

                    if (abstractRemoteFile instanceof RemotePrivateDrive)
                        driveRootUUID = currentFolderUUID;
                    else if (abstractRemoteFile instanceof RemotePublicDrive)
                        driveRootUUID = currentFolderUUID;

                    getFile();

                    handleFileListOperateCallback.handleFileListOperate(currentFolderName);
                }
            });

            binding.setFileItemViewModel(fileItemViewModel);

        }
    }


    private class FileViewHolder extends BaseBindingViewHolder {

        LinearLayout remoteFileItemLayout;

        RelativeLayout contentLayout;

        ImageButton itemMenu;

        TextView fileSizeTv;

        private RemoteFileItemLayoutBinding binding;

        FileViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            binding = (RemoteFileItemLayoutBinding) viewDataBinding;

            contentLayout = binding.contentLayout;
            remoteFileItemLayout = binding.remoteFileItemLayout;
            itemMenu = binding.itemMenu;
            fileSizeTv = binding.fileSize;

        }

        @Override
        public void refreshView(int position) {

            if (position == 0) {

                Util.setMargin(contentLayout, 0, Util.dip2px(activity, 8), 0, 0);

            }

            final RemoteFile abstractRemoteFile = (RemoteFile) abstractRemoteFiles.get(position);

            fileSizeTv.setText(Formatter.formatFileSize(activity, Long.valueOf(abstractRemoteFile.getSize())));

            FileItemViewModel fileItemViewModel = binding.getFileItemViewModel();

            if (fileItemViewModel == null)
                fileItemViewModel = new FileItemViewModel();

            fileItemViewModel.selectMode.set(selectMode);

            if (selectMode) {

                toggleFileIconBgResource(fileItemViewModel, abstractRemoteFile);

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        toggleFileInSelectedFile(abstractRemoteFile);
                        toggleFileIconBgResource(binding.getFileItemViewModel(), abstractRemoteFile);

                        fileListSelectModeListener.onFileSelectItemClick(selectedFiles.size());

                    }
                });

                remoteFileItemLayout.setOnLongClickListener(null);

            } else {

                fileItemViewModel.showFileIcon.set(true);

                itemMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

                        BottomMenuItem menuItem;
                        if (fileTaskManager.checkIsDownloaded(abstractRemoteFile.getUuid())) {

                            bottomMenuItems.add(new BottomMenuItem(R.drawable.download, activity.getString(R.string.re_download_the_item), new ReDownloadCommand(abstractRemoteFile)));

                            menuItem = new BottomMenuItem(R.drawable.file_icon, activity.getString(R.string.open_the_item), new OpenFileCommand(activity, abstractRemoteFile.getName()));

                        } else {
                            AbstractCommand macroCommand = new MacroCommand();
                            AbstractCommand downloadFileCommand = new DownloadFileCommand(fileTaskManager,
                                    abstractRemoteFile, stationFileRepository, currentUserUUID, driveRootUUID);
                            macroCommand.addCommand(downloadFileCommand);
                            macroCommand.addCommand(new ChangeToDownloadPageCommand(changeToDownloadPageCallback));

                            menuItem = new BottomMenuItem(R.drawable.download, activity.getString(R.string.download_the_item), macroCommand);
                        }
                        bottomMenuItems.add(menuItem);

                        BottomMenuItem cancelMenuItem = new BottomMenuItem(R.drawable.close, activity.getString(R.string.cancel), nullCommand);
                        bottomMenuItems.add(cancelMenuItem);

                        getBottomSheetDialog(bottomMenuItems).show();
                    }
                });

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (fileTaskManager.checkIsDownloaded(abstractRemoteFile.getUuid())) {

                            if (!abstractRemoteFile.openAbstractRemoteFile(activity, driveRootUUID)) {
                                Toast.makeText(activity, activity.getString(R.string.open_file_failed), Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            if (FileUtil.checkFileIsVideo(abstractRemoteFile.getName())) {

                                PlayVideoActivity.startPlayVideoActivity(activity, driveRootUUID, abstractRemoteFile);

                                return;
                            }

                            selectedFiles.clear();
                            selectedFiles.add(abstractRemoteFile);

                            checkWriteExternalStoragePermission();
                        }


                    }
                });

                remoteFileItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        fileListSelectModeListener.onFileItemLongClick();

                        selectMode = true;
                        refreshSelectMode(selectMode, abstractRemoteFile);

                        return true;
                    }
                });

            }

            binding.setFileItemViewModel(fileItemViewModel);

        }

        private void toggleFileIconBgResource(FileItemViewModel fileItemViewModel, AbstractRemoteFile abstractRemoteFile) {
            if (selectedFiles.contains(abstractRemoteFile)) {

                fileItemViewModel.fileIconBg.set(R.drawable.check_circle_selected);
                fileItemViewModel.showFileIcon.set(false);


            } else {

                fileItemViewModel.fileIconBg.set(R.drawable.round_circle);
                fileItemViewModel.showFileIcon.set(true);

            }
        }

        private void toggleFileInSelectedFile(AbstractRemoteFile abstractRemoteFile) {
            if (selectedFiles.contains(abstractRemoteFile)) {
                selectedFiles.remove(abstractRemoteFile);
            } else {
                selectedFiles.add(abstractRemoteFile);
            }
        }

    }

}
