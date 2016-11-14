package com.winsun.fruitmix.fileModule.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveFileOperationEvent;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.fileModule.model.BottomMenuItem;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFileFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileFragment extends Fragment {

    public static final String TAG = FileFragment.class.getSimpleName();

    @BindView(R.id.file_recyclerview)
    RecyclerView fileRecyclerView;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;

    private FileRecyclerViewAdapter fileRecyclerViewAdapter;

    private List<AbstractRemoteFile> abstractRemoteFiles;

    private boolean remoteFileLoaded = false;

    private String currentFolderUUID;

    private List<String> retrievedFolderUUIDList;

    private boolean selectMode = false;

    private List<AbstractRemoteFile> selectedFiles;

    private OnFileFragmentInteractionListener onFileFragmentInteractionListener;

    public FileFragment() {
        // Required empty public constructor
    }

    public void setOnFileFragmentInteractionListener(OnFileFragmentInteractionListener onFileFragmentInteractionListener) {
        this.onFileFragmentInteractionListener = onFileFragmentInteractionListener;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileFragment newInstance(OnFileFragmentInteractionListener onFileFragmentInteractionListener) {
        FileFragment fragment = new FileFragment();
        fragment.setOnFileFragmentInteractionListener(onFileFragmentInteractionListener);

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        abstractRemoteFiles = new ArrayList<>();

        retrievedFolderUUIDList = new ArrayList<>();

        selectedFiles = new ArrayList<>();

        Log.i(TAG, "onCreate: ");
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

        Log.i(TAG, "onCreateView: ");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!remoteFileLoaded && !isHidden()) {
            User user = LocalCache.RemoteUserMapKeyIsUUID.get(FNAS.userUUID);

            currentFolderUUID = user.getHome();

            if (!retrievedFolderUUIDList.contains(currentFolderUUID)) {
                retrievedFolderUUIDList.add(currentFolderUUID);
            }

            FNAS.retrieveRemoteFile(getActivity(), currentFolderUUID);
        }

        Log.i(TAG, "onResume: ");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        remoteFileLoaded = false;
    }

    public void handleOperationResult(OperationEvent operationEvent) {

        String action = operationEvent.getAction();
        if (action.equals(Util.REMOTE_FILE_RETRIEVED)) {

            loadingLayout.setVisibility(View.GONE);

            OperationResult result = operationEvent.getOperationResult();
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
                case FAIL:
                    noContentLayout.setVisibility(View.VISIBLE);
                    break;
            }

        }

    }

    public boolean handleBackPressedOrNot() {
        return selectMode || notRootFolder();
    }

    private boolean notRootFolder() {
        User user = LocalCache.RemoteUserMapKeyIsUUID.get(FNAS.userUUID);
        String homeFolderUUID = user.getHome();

        return !currentFolderUUID.equals(homeFolderUUID);
    }

    public void onBackPressed() {

        if (selectMode) {

            selectMode = false;
            refreshSelectMode(selectMode);
        } else {

            retrievedFolderUUIDList.remove(retrievedFolderUUIDList.size() - 1);

            currentFolderUUID = retrievedFolderUUIDList.get(retrievedFolderUUIDList.size() - 1);

            FNAS.retrieveRemoteFile(getActivity(), currentFolderUUID);

        }

    }

    private void refreshSelectMode(boolean selectMode) {

        this.selectMode = selectMode;

        if (!selectMode) {
            selectedFiles.clear();
        }

        fileRecyclerViewAdapter.notifyDataSetChanged();

    }

    public List<BottomMenuItem> getMainMenuItem() {

        List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

        if (selectMode) {

            BottomMenuItem clearSelectItem = new BottomMenuItem(getString(R.string.clear_select_item)) {
                @Override
                public void handleOnClickEvent() {
                    selectMode = false;
                    refreshSelectMode(selectMode);
                }
            };

            bottomMenuItems.add(clearSelectItem);

            BottomMenuItem downloadSelectItem = new BottomMenuItem(getString(R.string.download_select_item)) {
                @Override
                public void handleOnClickEvent() {

                    checkWriteExternalStoragePermission();

                    selectMode = false;
                    refreshSelectMode(selectMode);

                }
            };

            bottomMenuItems.add(downloadSelectItem);

        } else {

            BottomMenuItem selectItem = new BottomMenuItem(getString(R.string.select_file)) {
                @Override
                public void handleOnClickEvent() {
                    selectMode = true;
                    refreshSelectMode(selectMode);
                }
            };

            bottomMenuItems.add(selectItem);
        }

        BottomMenuItem cancelMenuItem = new BottomMenuItem(getString(R.string.cancel)) {
            @Override
            public void handleOnClickEvent() {
                onFileFragmentInteractionListener.dismissBottomSheetDialog();
            }
        };

        bottomMenuItems.add(cancelMenuItem);

        return bottomMenuItems;

    }

    private void checkWriteExternalStoragePermission() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);

        } else {
            downloadSelectedFiles();
        }

    }

    private void downloadSelectedFiles() {
        for (AbstractRemoteFile abstractRemoteFile : selectedFiles) {

            abstractRemoteFile.downloadFile(getActivity());
        }

        onFileFragmentInteractionListener.changeFilePageToFileDownloadFragment();
    }


    public void requestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    downloadSelectedFiles();

                } else {

                    Toast.makeText(getActivity(), getString(R.string.no_write_external_storage_permission), Toast.LENGTH_SHORT).show();

                }

        }


    }

    class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileViewHolder> {
        @Override
        public int getItemCount() {
            return abstractRemoteFiles == null ? 0 : abstractRemoteFiles.size();
        }

        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.remote_file_item_layout, parent, false);

            return new FileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FileViewHolder holder, int position) {
            holder.refreshView(position);
        }
    }


    class FileViewHolder extends RecyclerView.ViewHolder {

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
        @BindView(R.id.item_menu)
        ImageView itemMenu;


        FileViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        void refreshView(int position) {
            final AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(position);

            fileIcon.setImageResource(abstractRemoteFile.getImageResource());
            fileTime.setText(abstractRemoteFile.getTimeDateText());
            fileName.setText(abstractRemoteFile.getName());

            if (selectMode) {

                itemMenu.setVisibility(View.GONE);
                fileIconBg.setVisibility(View.VISIBLE);

                toggleFileIconBgResource(abstractRemoteFile);

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (abstractRemoteFile.isFolder()) {

                            currentFolderUUID = abstractRemoteFile.getUuid();

                            retrievedFolderUUIDList.add(currentFolderUUID);

                            abstractRemoteFile.openAbstractRemoteFile(getActivity());

                        } else {
                            toggleFileInSelectedFile(abstractRemoteFile);
                            toggleFileIconBgResource(abstractRemoteFile);
                        }
                    }
                });


            } else {

                if (abstractRemoteFile.isFolder()) {
                    itemMenu.setVisibility(View.GONE);
                } else {
                    itemMenu.setVisibility(View.VISIBLE);

                    itemMenu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            List<BottomMenuItem> bottomMenuItems = new ArrayList<>();

                            BottomMenuItem downloadTheItem = new BottomMenuItem(getString(R.string.download_the_item)) {
                                @Override
                                public void handleOnClickEvent() {

                                    selectedFiles.add(abstractRemoteFile);

                                    checkWriteExternalStoragePermission();
                                }
                            };

                            bottomMenuItems.add(downloadTheItem);

                            BottomMenuItem cancelMenuItem = new BottomMenuItem(getString(R.string.cancel)) {
                                @Override
                                public void handleOnClickEvent() {
                                    onFileFragmentInteractionListener.dismissBottomSheetDialog();
                                }
                            };

                            bottomMenuItems.add(cancelMenuItem);

                            onFileFragmentInteractionListener.showBottomSheetDialog(bottomMenuItems);
                        }
                    });

                }

                fileIconBg.setVisibility(View.INVISIBLE);
                fileIcon.setVisibility(View.VISIBLE);

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (abstractRemoteFile.isFolder()) {
                            currentFolderUUID = abstractRemoteFile.getUuid();

                            retrievedFolderUUIDList.add(currentFolderUUID);

                            abstractRemoteFile.openAbstractRemoteFile(getActivity());
                        } else {

                            selectedFiles.add(abstractRemoteFile);

                            checkWriteExternalStoragePermission();

                        }

                    }
                });

                remoteFileItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (!selectMode) {
                            selectMode = true;
                            refreshSelectMode(selectMode);
                        }
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
