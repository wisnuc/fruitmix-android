package com.winsun.fruitmix.fileModule.fragment;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.winsun.fruitmix.dialog.BottomMenuDialogFactory;
import com.winsun.fruitmix.eventbus.DownloadStateChangedEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveFileOperationEvent;
import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.fileModule.interfaces.OnFileInteractionListener;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.interfaces.OnViewSelectListener;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFileInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileFragment extends Fragment implements OnViewSelectListener, IShowHideFragmentListener {

    public static final String TAG = FileFragment.class.getSimpleName();

    @BindView(R.id.file_recyclerview)
    RecyclerView fileRecyclerView;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;
    @BindView(R.id.no_content_textview)
    TextView noContentTextView;

    private FileRecyclerViewAdapter fileRecyclerViewAdapter;

    private List<AbstractRemoteFile> abstractRemoteFiles;

    private boolean remoteFileLoaded = false;

    private String currentFolderUUID;
    private String currentFolderName;

    private List<String> retrievedFolderUUIDList;
    private List<String> retrievedFolderNameList;

    private boolean selectMode = false;

    private List<AbstractRemoteFile> selectedFiles;

    private OnFileInteractionListener onFileInteractionListener;

    private AbstractCommand showUnSelectModeViewCommand;

    private AbstractCommand showSelectModeViewCommand;

    private AbstractCommand macroCommand;

    private AbstractCommand nullCommand;

    private String rootUUID;

    private ProgressDialog progressDialog;

    private int progressMax = 100;

    private DownloadFileCommand mCurrentDownloadFileCommand;

    private boolean cancelDownload = false;

    public FileFragment() {
        // Required empty public constructor
    }

    public void setOnFileInteractionListener(OnFileInteractionListener onFileInteractionListener) {
        this.onFileInteractionListener = onFileInteractionListener;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileFragment.
     */
    public static FileFragment newInstance(OnFileInteractionListener onFileInteractionListener) {
        FileFragment fragment = new FileFragment();
        fragment.setOnFileInteractionListener(onFileInteractionListener);

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        abstractRemoteFiles = new ArrayList<>();

        retrievedFolderUUIDList = new ArrayList<>();
        retrievedFolderNameList = new ArrayList<>();

        selectedFiles = new ArrayList<>();

        showUnSelectModeViewCommand = new ShowUnSelectModeViewCommand(this);

        showSelectModeViewCommand = new ShowSelectModeViewCommand(this);

        nullCommand = new NullCommand();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file, container, false);

        ButterKnife.bind(this, view);

        fileRecyclerViewAdapter = new FileRecyclerViewAdapter();
        fileRecyclerView.setAdapter(fileRecyclerViewAdapter);
        fileRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        noContentImageView.setImageResource(R.drawable.no_file);

        noContentTextView.setText(getString(R.string.no_files));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!remoteFileLoaded && !isHidden()) {

            String userHome = LocalCache.getUserHome(getContext());

            currentFolderUUID = userHome;

            rootUUID = userHome;

            if (!retrievedFolderUUIDList.contains(currentFolderUUID)) {
                retrievedFolderUUIDList.add(currentFolderUUID);
                retrievedFolderNameList.add(getString(R.string.file));
            }

            FNAS.retrieveRemoteFile(getActivity(), currentFolderUUID, rootUUID);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        remoteFileLoaded = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

        if (downloadState.equals(DownloadState.DOWNLOADING)) {
            progressDialog.setProgress(fileDownloadItem.getCurrentProgress(progressMax));
        } else {

            progressDialog.dismiss();

            if (downloadState.equals(DownloadState.FINISHED)) {
                OpenFileCommand openFileCommand = new OpenFileCommand(getContext(), fileDownloadItem.getFileName());
                openFileCommand.execute();
            } else if (downloadState.equals(DownloadState.ERROR)) {

                if (cancelDownload)
                    cancelDownload = false;
                else
                    Toast.makeText(getContext(), getText(R.string.download_failed), Toast.LENGTH_SHORT).show();
            }

        }

    }

    public void handleTitle() {

        if (handleBackPressedOrNot()) {

            onFileInteractionListener.setToolbarTitle(currentFolderName);
            onFileInteractionListener.setNavigationIcon(R.drawable.ic_back_black);
            onFileInteractionListener.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

        } else {
            onFileInteractionListener.setToolbarTitle(getString(R.string.file));
            onFileInteractionListener.setNavigationIcon(R.drawable.menu_black);
            onFileInteractionListener.setDefaultNavigationOnClickListener();
        }

    }

    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();
        if (action.equals(Util.REMOTE_FILE_RETRIEVED)) {

            loadingLayout.setVisibility(View.GONE);

            OperationResultType result = operationEvent.getOperationResult().getOperationResultType();
            switch (result) {
                case SUCCEED:

                    remoteFileLoaded = true;

                    List<AbstractRemoteFile> abstractRemoteFileList = LocalCache.RemoteFileMapKeyIsUUID.get(((RetrieveFileOperationEvent) operationEvent).getFolderUUID()).listChildAbstractRemoteFileList();

                    if (abstractRemoteFileList.size() == 0) {
                        noContentLayout.setVisibility(View.VISIBLE);
                        fileRecyclerView.setVisibility(View.GONE);
                    } else {
                        fileRecyclerView.setVisibility(View.VISIBLE);
                        noContentLayout.setVisibility(View.GONE);

                        abstractRemoteFiles.clear();
                        abstractRemoteFiles.addAll(abstractRemoteFileList);
                        fileRecyclerViewAdapter.notifyDataSetChanged();
                    }

                    break;
                default:
                    noContentLayout.setVisibility(View.VISIBLE);
                    break;
            }

        }

    }

    public boolean handleBackPressedOrNot() {
        return selectMode || notRootFolder();
    }

    private boolean notRootFolder() {

        String homeFolderUUID = LocalCache.getUserHome(getContext());

        return !currentFolderUUID.equals(homeFolderUUID);
    }

    public void onBackPressed() {

        if (notRootFolder()) {

            if (loadingLayout.getVisibility() == View.VISIBLE)
                return;

            loadingLayout.setVisibility(View.VISIBLE);

            retrievedFolderUUIDList.remove(retrievedFolderUUIDList.size() - 1);

            currentFolderUUID = retrievedFolderUUIDList.get(retrievedFolderUUIDList.size() - 1);

            retrievedFolderNameList.remove(retrievedFolderNameList.size() - 1);
            currentFolderName = retrievedFolderNameList.get(retrievedFolderUUIDList.size() - 1);

            FNAS.retrieveRemoteFile(getActivity(), currentFolderUUID, rootUUID);

        } else {
            selectMode = false;
            refreshSelectMode(selectMode, null);
        }

        handleTitle();

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

        Dialog dialog = new BottomMenuDialogFactory(bottomMenuItems).createDialog(getActivity());

        for (BottomMenuItem bottomMenuItem : bottomMenuItems) {
            bottomMenuItem.setDialog(dialog);
        }

        return dialog;
    }

    public List<BottomMenuItem> getMainMenuItem() {

        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

        if (selectMode) {

            BottomMenuItem clearSelectItem = new BottomMenuItem(getString(R.string.clear_select_item), showUnSelectModeViewCommand);

            bottomMenuItems.add(clearSelectItem);

            macroCommand = new MacroCommand();

            addSelectFilesToMacroCommand();

            macroCommand.addCommand(showUnSelectModeViewCommand);

            macroCommand.addCommand(new ChangeToDownloadPageCommand(onFileInteractionListener));

            BottomMenuItem downloadSelectItem = new BottomMenuItem(getString(R.string.download_select_item), macroCommand);

            bottomMenuItems.add(downloadSelectItem);

        } else {

            BottomMenuItem selectItem = new BottomMenuItem(getString(R.string.select_file), showSelectModeViewCommand);

            if (abstractRemoteFiles.isEmpty())
                selectItem.setDisable(true);

            bottomMenuItems.add(selectItem);
        }

        BottomMenuItem cancelMenuItem = new BottomMenuItem(getString(R.string.cancel), nullCommand);

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

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);

        } else {
            openFileWhenOnClick();
        }

    }

    private void openFileWhenOnClick() {

        mCurrentDownloadFileCommand = new DownloadFileCommand(selectedFiles.get(0));

        mCurrentDownloadFileCommand.execute();

        progressDialog = new ProgressDialog(getContext());

        progressDialog.setTitle(getString(R.string.downloading));

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCurrentDownloadFileCommand.unExecute();

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

                    Toast.makeText(getActivity(), getString(R.string.android_no_write_external_storage_permission), Toast.LENGTH_SHORT).show();

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
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.remote_file_item_layout, parent, false);
                    viewHolder = new FileViewHolder(view);
                    break;
                case VIEW_FOLDER:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.remote_folder_item_layout, parent, false);
                    viewHolder = new FolderViewHolder(view);
                    break;
                default:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.remote_file_item_layout, parent, false);
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
                layoutParams.setMargins(0, Util.dip2px(getActivity(), 8), Util.dip2px(getActivity(), 16), 0);
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

                    loadingLayout.setVisibility(View.VISIBLE);

                    abstractRemoteFile.openAbstractRemoteFile(getActivity(), rootUUID);

                    handleTitle();
                }
            });

        }
    }


    class FileViewHolder extends BaseRecyclerViewHolder {

        @BindView(R.id.file_icon_bg)
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
        @BindView(R.id.item_menu_layout)
        ViewGroup itemMenuLayout;

        FileViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        @Override
        public void refreshView(int position) {

            if (position == 0) {

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentLayout.getLayoutParams();
                layoutParams.setMargins(0, Util.dip2px(getActivity(), 8), Util.dip2px(getActivity(), 16), 0);
                contentLayout.setLayoutParams(layoutParams);

            }

            final AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(position);

            fileTime.setText(abstractRemoteFile.getTimeDateText());
            fileName.setText(abstractRemoteFile.getName());

            if (selectMode) {

                itemMenuLayout.setVisibility(View.GONE);
                fileIconBg.setVisibility(View.VISIBLE);

                toggleFileIconBgResource(abstractRemoteFile);

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        toggleFileInSelectedFile(abstractRemoteFile);
                        toggleFileIconBgResource(abstractRemoteFile);

                    }
                });

                remoteFileItemLayout.setOnLongClickListener(null);

            } else {

                itemMenuLayout.setVisibility(View.VISIBLE);
                fileIconBg.setVisibility(View.INVISIBLE);
                fileIcon.setVisibility(View.VISIBLE);

                itemMenuLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

                        BottomMenuItem menuItem;
                        if (FileDownloadManager.INSTANCE.checkIsDownloaded(abstractRemoteFile.getUuid())) {
                            menuItem = new BottomMenuItem(getString(R.string.open_the_item), new OpenFileCommand(getContext(), abstractRemoteFile.getName()));
                        } else {
                            AbstractCommand macroCommand = new MacroCommand();
                            AbstractCommand downloadFileCommand = new DownloadFileCommand(abstractRemoteFile);
                            macroCommand.addCommand(downloadFileCommand);
                            macroCommand.addCommand(new ChangeToDownloadPageCommand(onFileInteractionListener));
                            menuItem = new BottomMenuItem(getString(R.string.download_the_item), macroCommand);
                        }
                        bottomMenuItems.add(menuItem);

                        BottomMenuItem cancelMenuItem = new BottomMenuItem(getString(R.string.cancel), nullCommand);
                        bottomMenuItems.add(cancelMenuItem);

                        getBottomSheetDialog(bottomMenuItems).show();
                    }
                });

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        FileDownloadManager fileDownloadManager = FileDownloadManager.INSTANCE;
                        if (fileDownloadManager.checkIsDownloaded(abstractRemoteFile.getUuid())) {

                            if (!abstractRemoteFile.openAbstractRemoteFile(getActivity(), rootUUID)) {
                                Toast.makeText(getActivity(), getString(R.string.open_file_failed), Toast.LENGTH_SHORT).show();
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
