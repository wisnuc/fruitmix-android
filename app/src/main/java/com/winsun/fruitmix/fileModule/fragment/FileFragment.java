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
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
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

    private FileRecyclerViewAdapter fileRecyclerViewAdapter;

    private List<AbstractRemoteFile> abstractRemoteFiles;

    private boolean remoteFileLoaded = false;

    private String currentFolderUUID;

    private List<String> retrievedFolderUUIDList;

    private boolean selectMode = false;

    private List<String> selectedFileUUIDs;

    private ProgressDialog dialog;

    private AbstractRemoteFile currentDownloadFile;

    public FileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileFragment newInstance() {
        FileFragment fragment = new FileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        abstractRemoteFiles = new ArrayList<>();

        retrievedFolderUUIDList = new ArrayList<>();

        selectedFileUUIDs = new ArrayList<>();
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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!remoteFileLoaded && !isHidden()) {
            User user = LocalCache.RemoteUserMapKeyIsUUID.get(FNAS.userUUID);

            currentFolderUUID = user.getHome();

            retrievedFolderUUIDList.add(currentFolderUUID);

            FNAS.retrieveRemoteFile(getActivity(), currentFolderUUID);
        }

    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationResult(OperationEvent operationEvent) {

        String action = operationEvent.getAction();
        if (action.equals(Util.REMOTE_FILE_RETRIEVED)) {

            OperationResult result = operationEvent.getOperationResult();
            switch (result) {
                case SUCCEED:

                    remoteFileLoaded = true;

                    abstractRemoteFiles = LocalCache.RemoteFileMapKeyIsUUID.get(((RetrieveFileOperationEvent)operationEvent).getFolderUUID()).listChildAbstractRemoteFileList();
                    fileRecyclerViewAdapter.notifyDataSetChanged();

                    break;
                case FAIL:
                    break;
            }

        } else if (action.equals(Util.REMOTE_FILE_DOWNLOAD_STATE_CHANGED)) {

            dialog.dismiss();
            OperationResult result = operationEvent.getOperationResult();
            switch (result) {
                case SUCCEED:
                    Toast.makeText(getActivity(), getString(R.string.download_file_succeed), Toast.LENGTH_SHORT).show();
                    
                    currentDownloadFile.openAbstractRemoteFile(getActivity());
                    break;
                case FAIL:
                    Toast.makeText(getActivity(), getString(R.string.download_file_failed), Toast.LENGTH_SHORT).show();
                    break;
            }

        }

    }

    public String getCurrentFolderUUID() {
        return currentFolderUUID;
    }

    public void onBackPressed() {

        retrievedFolderUUIDList.remove(retrievedFolderUUIDList.size() - 1);

        currentFolderUUID = retrievedFolderUUIDList.get(retrievedFolderUUIDList.size() - 1);

        FNAS.retrieveRemoteFile(getActivity(), currentFolderUUID);

    }

    public void refreshSelectMode(boolean selectMode) {

        this.selectMode = selectMode;

        if (!selectMode) {
            selectedFileUUIDs.clear();
        }

        fileRecyclerViewAdapter.notifyDataSetChanged();

    }



    private void checkWriteExternalStoragePermission() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);

        } else {

            currentDownloadFile.downloadFile(getActivity());

            dialog = ProgressDialog.show(getActivity(), getString(R.string.downloading), getString(R.string.loading_message), true, false);

        }

    }


    public void requestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case Util.WRITE_EXTERNAL_STORAGE_REQUEST_CODE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    currentDownloadFile.downloadFile(getActivity());

                    dialog = ProgressDialog.show(getActivity(), getString(R.string.downloading), getString(R.string.loading_message), true, false);

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

                fileIconBg.setVisibility(View.VISIBLE);

                toggleFileIconBgResource(abstractRemoteFile.getUuid());

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (abstractRemoteFile.isFolder()) {

                            currentFolderUUID = abstractRemoteFile.getUuid();

                            retrievedFolderUUIDList.add(currentFolderUUID);

                            abstractRemoteFile.openAbstractRemoteFile(getActivity());

                        } else {
                            toggleFileInSelectedFile(abstractRemoteFile.getUuid());
                            toggleFileIconBgResource(abstractRemoteFile.getUuid());
                        }
                    }
                });


            } else {

                fileIconBg.setVisibility(View.INVISIBLE);
                fileIcon.setVisibility(View.VISIBLE);

                remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (abstractRemoteFile.isFolder()) {
                            currentFolderUUID = abstractRemoteFile.getUuid();

                            retrievedFolderUUIDList.add(currentFolderUUID);
                        }

                        if (abstractRemoteFile.checkIsDownloaded()) {

                            if (!abstractRemoteFile.openAbstractRemoteFile(getActivity())) {
                                Toast.makeText(getActivity(), getString(R.string.open_file_failed), Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            currentDownloadFile = abstractRemoteFile;

                            checkWriteExternalStoragePermission();

                        }


                    }
                });

            }

        }

        private void toggleFileIconBgResource(String fileUUID) {
            if (selectedFileUUIDs.contains(fileUUID)) {
                fileIconBg.setBackgroundResource(R.drawable.check_circle_selected);
                fileIcon.setVisibility(View.INVISIBLE);
            } else {
                fileIconBg.setBackgroundResource(R.drawable.round_circle);
                fileIcon.setVisibility(View.VISIBLE);
            }
        }

        private void toggleFileInSelectedFile(String fileUUID) {
            if (selectedFileUUIDs.contains(fileUUID)) {
                selectedFileUUIDs.remove(fileUUID);
            } else {
                selectedFileUUIDs.add(fileUUID);
            }
        }

    }
}
