package com.winsun.fruitmix.fileModule.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.command.ChangeToDownloadPageCommand;
import com.winsun.fruitmix.command.NullCommand;
import com.winsun.fruitmix.command.DownloadFileCommand;
import com.winsun.fruitmix.command.MacroCommand;
import com.winsun.fruitmix.command.OpenFileCommand;
import com.winsun.fruitmix.command.ShowSelectModeViewCommand;
import com.winsun.fruitmix.command.ShowUnSelectModeViewCommand;
import com.winsun.fruitmix.databinding.FragmentFileBinding;
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.eventbus.DownloadStateChangedEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveFileOperationEvent;
import com.winsun.fruitmix.fileModule.FileDownloadActivity;
import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.fileModule.interfaces.HandleTitleCallback;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.mediaModule.interfaces.Page;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.OnViewSelectListener;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FileFragment implements Page, OnViewSelectListener, IShowHideFragmentListener {

    public static final String TAG = FileFragment.class.getSimpleName();

    RecyclerView fileRecyclerView;

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

    private AbstractCommand macroCommand;

    private AbstractCommand nullCommand;

    private String rootUUID;

    private ProgressDialog progressDialog;

    private int progressMax = 100;

    private DownloadFileCommand mCurrentDownloadFileCommand;

    private boolean cancelDownload = false;

    private NoContentViewModel noContentViewModel;
    private LoadingViewModel loadingViewModel;

    private Activity activity;

    private View view;

    private HandleTitleCallback handleTitleCallback;

    private ChangeToDownloadPageCommand.ChangeToDownloadPageCallback changeToDownloadPageCallback;


    public FileFragment(final Activity activity, HandleTitleCallback handleTitleCallback) {

        this.activity = activity;

        this.handleTitleCallback = handleTitleCallback;

        abstractRemoteFiles = new ArrayList<>();

        retrievedFolderUUIDList = new ArrayList<>();
        retrievedFolderNameList = new ArrayList<>();

        selectedFiles = new ArrayList<>();

        showUnSelectModeViewCommand = new ShowUnSelectModeViewCommand(this);

        showSelectModeViewCommand = new ShowSelectModeViewCommand(this);

        nullCommand = new NullCommand();

        view = onCreateView();

        changeToDownloadPageCallback = new ChangeToDownloadPageCommand.ChangeToDownloadPageCallback() {
            @Override
            public void changeToDownloadPage() {
                activity.startActivity(new Intent(activity, FileDownloadActivity.class));
            }
        };

        refreshView();

    }

    private View onCreateView() {
        // Inflate the layout for this fragment

        FragmentFileBinding binding = FragmentFileBinding.inflate(LayoutInflater.from(activity.getApplicationContext()), null, false);

        noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentText(activity.getString(R.string.no_files));
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);

        loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);
        binding.setNoContentViewModel(noContentViewModel);

        fileRecyclerView = binding.fileRecyclerview;

        fileRecyclerViewAdapter = new FileRecyclerViewAdapter();
        fileRecyclerView.setAdapter(fileRecyclerViewAdapter);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

        return binding.getRoot();
    }

    @Override
    public void refreshView() {

        if (!remoteFileLoaded) {

            String userHome = LocalCache.getUserHome(activity);

            currentFolderUUID = userHome;
            currentFolderName = activity.getString(R.string.file);

            rootUUID = userHome;

            if (!retrievedFolderUUIDList.contains(currentFolderUUID)) {
                retrievedFolderUUIDList.add(currentFolderUUID);
                retrievedFolderNameList.add(currentFolderName);
            }

            FNAS.retrieveRemoteFile(activity, currentFolderUUID, rootUUID);
        }

    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

    }

    @Override
    public void onDestroy() {
        remoteFileLoaded = false;

        activity = null;
    }

    @Override
    public void show() {
        MobclickAgent.onPageStart("FileFragment");
    }

    @Override
    public void hide() {
        MobclickAgent.onPageEnd("FileFragment");
    }

    public void handleEvent(DownloadStateChangedEvent downloadStateChangedEvent) {

        if (mCurrentDownloadFileCommand == null)
            return;

        DownloadState downloadState = downloadStateChangedEvent.getDownloadState();

        FileDownloadItem fileDownloadItem = mCurrentDownloadFileCommand.getFileDownloadItem();

        switch (downloadState) {
            case START_DOWNLOAD:
            case PENDING:
                break;
            case DOWNLOADING:

                progressDialog.setProgress(fileDownloadItem.getCurrentProgress(progressMax));

                break;
            case FINISHED:

                mCurrentDownloadFileCommand = null;

                progressDialog.dismiss();

                OpenFileCommand openFileCommand = new OpenFileCommand(activity, fileDownloadItem.getFileName());
                openFileCommand.execute();

                break;
            case ERROR:

                mCurrentDownloadFileCommand = null;

                progressDialog.dismiss();

                if (cancelDownload)
                    cancelDownload = false;
                else
                    Toast.makeText(activity, activity.getText(R.string.download_failed), Toast.LENGTH_SHORT).show();

                break;
            case NO_ENOUGH_SPACE:

                mCurrentDownloadFileCommand = null;

                progressDialog.dismiss();

                Toast.makeText(activity, activity.getString(R.string.no_enough_space), Toast.LENGTH_SHORT).show();
                break;
        }

    }

    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();
        if (action.equals(Util.REMOTE_FILE_RETRIEVED)) {

            loadingViewModel.showLoading.set(false);

            OperationResultType result = operationEvent.getOperationResult().getOperationResultType();
            switch (result) {
                case SUCCEED:

                    remoteFileLoaded = true;

                    List<AbstractRemoteFile> abstractRemoteFileList = LocalCache.RemoteFileMapKeyIsUUID.get(((RetrieveFileOperationEvent) operationEvent).getFolderUUID()).listChildAbstractRemoteFileList();

                    if (abstractRemoteFileList.size() == 0) {

                        noContentViewModel.showNoContent.set(true);

                        fileRecyclerView.setVisibility(View.GONE);
                    } else {

                        noContentViewModel.showNoContent.set(false);

                        fileRecyclerView.setVisibility(View.VISIBLE);

                        abstractRemoteFiles.clear();
                        abstractRemoteFiles.addAll(abstractRemoteFileList);

                        sortFile(abstractRemoteFiles);

                        fileRecyclerViewAdapter.notifyDataSetChanged();
                    }

                    break;
                default:

                    noContentViewModel.showNoContent.set(true);

                    break;
            }

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

        if (notRootFolder()) {

            if (loadingViewModel.showLoading.get() && !noContentViewModel.showNoContent.get())
                return;

            noContentViewModel.showNoContent.set(false);

            loadingViewModel.showLoading.set(true);

            retrievedFolderUUIDList.remove(retrievedFolderUUIDList.size() - 1);
            currentFolderUUID = retrievedFolderUUIDList.get(retrievedFolderUUIDList.size() - 1);

            retrievedFolderNameList.remove(retrievedFolderNameList.size() - 1);
            currentFolderName = retrievedFolderNameList.get(retrievedFolderNameList.size() - 1);

            FNAS.retrieveRemoteFile(activity, currentFolderUUID, rootUUID);

        } else {
            selectMode = false;
            refreshSelectMode(selectMode, null);
        }

        handleTitleCallback.handleTitle(currentFolderName);

    }

    private void refreshSelectMode(boolean selectMode, AbstractRemoteFile selectFile) {

        this.selectMode = selectMode;

        selectedFiles.clear();
        if (selectFile != null)
            selectedFiles.add(selectFile);

        fileRecyclerViewAdapter.notifyDataSetChanged();

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

            BottomMenuItem clearSelectItem = new BottomMenuItem(R.drawable.cancel,activity.getString(R.string.clear_select_item), showUnSelectModeViewCommand);

            bottomMenuItems.add(clearSelectItem);

            macroCommand = new MacroCommand();

            addSelectFilesToMacroCommand();

            macroCommand.addCommand(showUnSelectModeViewCommand);

            macroCommand.addCommand(new ChangeToDownloadPageCommand(changeToDownloadPageCallback));

            BottomMenuItem downloadSelectItem = new BottomMenuItem(R.drawable.download,activity.getString(R.string.download_select_item), macroCommand);

            bottomMenuItems.add(downloadSelectItem);

        } else {

            BottomMenuItem selectItem = new BottomMenuItem(R.drawable.check,activity.getString(R.string.select_file), showSelectModeViewCommand);

            if (abstractRemoteFiles.isEmpty())
                selectItem.setDisable(true);

            bottomMenuItems.add(selectItem);
        }

        BottomMenuItem cancelMenuItem = new BottomMenuItem(R.drawable.close,activity.getString(R.string.cancel), nullCommand);

        bottomMenuItems.add(cancelMenuItem);

        return bottomMenuItems;

    }

    private void addSelectFilesToMacroCommand() {
        for (AbstractRemoteFile abstractRemoteFile : selectedFiles) {

            AbstractCommand abstractCommand = new DownloadFileCommand(abstractRemoteFile);
            macroCommand.addCommand(abstractCommand);

        }
    }

    private void checkWriteExternalStoragePermission() {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);

        } else {
            openFileWhenOnClick();
        }

    }

    private void openFileWhenOnClick() {

        mCurrentDownloadFileCommand = new DownloadFileCommand(selectedFiles.get(0));

        mCurrentDownloadFileCommand.execute();

        progressDialog = new ProgressDialog(activity);

        progressDialog.setTitle(activity.getString(R.string.downloading));

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCurrentDownloadFileCommand.unExecute();

                mCurrentDownloadFileCommand = null;

                cancelDownload = true;

                progressDialog.dismiss();
            }
        });

        progressDialog.setMax(progressMax);

        progressDialog.setCancelable(false);

        progressDialog.show();
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

    class FileRecyclerViewAdapter extends RecyclerView.Adapter<BaseRecyclerViewHolder> {

        private static final int VIEW_FILE = 0;
        private static final int VIEW_FOLDER = 1;

        @Override
        public int getItemCount() {
            return abstractRemoteFiles == null ? 0 : abstractRemoteFiles.size();
        }

        @Override
        public BaseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            BaseRecyclerViewHolder viewHolder;

            switch (viewType) {
                case VIEW_FILE:
                    view = LayoutInflater.from(activity).inflate(R.layout.remote_file_item_layout, parent, false);
                    viewHolder = new FileViewHolder(view);
                    break;
                case VIEW_FOLDER:
                    view = LayoutInflater.from(activity).inflate(R.layout.remote_folder_item_layout, parent, false);
                    viewHolder = new FolderViewHolder(view);
                    break;
                default:
                    view = LayoutInflater.from(activity).inflate(R.layout.remote_file_item_layout, parent, false);
                    viewHolder = new FileViewHolder(view);
            }


            return viewHolder;
        }

        @Override
        public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
            holder.refreshView(position);
        }

        @Override
        public int getItemViewType(int position) {

            return abstractRemoteFiles.get(position).isFolder() ? VIEW_FOLDER : VIEW_FILE;

        }

    }

    class FolderViewHolder extends BaseRecyclerViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.folder_icon_bg)
        ImageView folderIconBg;
        @BindView(R.id.remote_folder_item_layout)
        LinearLayout folderItemLayout;
        @BindView(R.id.content_layout)
        RelativeLayout contentLayout;

        FolderViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void refreshView(int position) {

            if (position == 0) {

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
                layoutParams.setMargins(0, Util.dip2px(activity, 8), Util.dip2px(activity, 16), 0);
                contentLayout.setLayoutParams(layoutParams);

            }

            final AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(position);

            fileName.setText(abstractRemoteFile.getName());

            if (selectMode) {
                folderIconBg.setVisibility(View.VISIBLE);
            } else {
                folderIconBg.setVisibility(View.INVISIBLE);
            }

            folderItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentFolderUUID = abstractRemoteFile.getUuid();

                    retrievedFolderUUIDList.add(currentFolderUUID);

                    currentFolderName = abstractRemoteFile.getName();

                    retrievedFolderNameList.add(currentFolderName);

                    loadingViewModel.showLoading.set(true);

                    abstractRemoteFile.openAbstractRemoteFile(activity, rootUUID);

                    handleTitleCallback.handleTitle(currentFolderName);
                }
            });

        }
    }


    class FileViewHolder extends BaseRecyclerViewHolder {

        @BindView(R.id.select_file_icon_bg)
        ImageView fileIconBg;
        @BindView(R.id.file_icon)
        ImageView fileIcon;
        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.file_time)
        TextView fileTime;
        @BindView(R.id.remote_file_item_layout)
        LinearLayout remoteFileItemLayout;
        @BindView(R.id.content_layout)
        RelativeLayout contentLayout;
        @BindView(R.id.item_menu)
        ImageButton itemMenu;

        FileViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        @Override
        public void refreshView(int position) {

            if (position == 0) {

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
                layoutParams.setMargins(0, Util.dip2px(activity, 8), 0, 0);
                contentLayout.setLayoutParams(layoutParams);

            }

            final AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(position);

            fileTime.setText(abstractRemoteFile.getTimeDateText());
            fileName.setText(abstractRemoteFile.getName());

            fileIcon.setImageResource(abstractRemoteFile.getFileTypeResID());

            if (selectMode) {

                itemMenu.setVisibility(View.GONE);
                fileIconBg.setVisibility(View.VISIBLE);

                toggleFileIconBgResource(abstractRemoteFile);

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        toggleFileInSelectedFile(abstractRemoteFile);
                        toggleFileIconBgResource(abstractRemoteFile);

                        if (selectedFiles.isEmpty())
                            unSelectMode();

                    }
                });

                remoteFileItemLayout.setOnLongClickListener(null);

            } else {

                itemMenu.setVisibility(View.VISIBLE);
                fileIconBg.setVisibility(View.INVISIBLE);
                fileIcon.setVisibility(View.VISIBLE);

                itemMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

                        BottomMenuItem menuItem;
                        if (FileDownloadManager.INSTANCE.checkIsDownloaded(abstractRemoteFile.getUuid())) {
                            menuItem = new BottomMenuItem(R.drawable.file_icon,activity.getString(R.string.open_the_item), new OpenFileCommand(activity, abstractRemoteFile.getName()));
                        } else {
                            AbstractCommand macroCommand = new MacroCommand();
                            AbstractCommand downloadFileCommand = new DownloadFileCommand(abstractRemoteFile);
                            macroCommand.addCommand(downloadFileCommand);
                            macroCommand.addCommand(new ChangeToDownloadPageCommand(changeToDownloadPageCallback));
                            menuItem = new BottomMenuItem(R.drawable.download,activity.getString(R.string.download_the_item), macroCommand);
                        }
                        bottomMenuItems.add(menuItem);

                        BottomMenuItem cancelMenuItem = new BottomMenuItem(R.drawable.close,activity.getString(R.string.cancel), nullCommand);
                        bottomMenuItems.add(cancelMenuItem);

                        getBottomSheetDialog(bottomMenuItems).show();
                    }
                });

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        FileDownloadManager fileDownloadManager = FileDownloadManager.INSTANCE;
                        if (fileDownloadManager.checkIsDownloaded(abstractRemoteFile.getUuid())) {

                            if (!abstractRemoteFile.openAbstractRemoteFile(activity, rootUUID)) {
                                Toast.makeText(activity, activity.getString(R.string.open_file_failed), Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            selectedFiles.clear();
                            selectedFiles.add(abstractRemoteFile);

                            checkWriteExternalStoragePermission();
                        }


                    }
                });

                remoteFileItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        selectMode = true;
                        refreshSelectMode(selectMode, abstractRemoteFile);

                        handleTitleCallback.handleTitle(currentFolderName);

                        return true;
                    }
                });

            }

        }

        private void toggleFileIconBgResource(AbstractRemoteFile abstractRemoteFile) {
            if (selectedFiles.contains(abstractRemoteFile)) {
                fileIconBg.setBackgroundResource(R.drawable.check_circle_selected);
                fileIcon.setVisibility(View.INVISIBLE);
            } else {
                fileIconBg.setBackgroundResource(R.drawable.round_circle);
                fileIcon.setVisibility(View.VISIBLE);
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
