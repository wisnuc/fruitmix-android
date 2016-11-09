package com.winsun.fruitmix.fileModule.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveFileOperationEvent;
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
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
 * Use the {@link FileShareFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileShareFragment extends Fragment {

    public static final String TAG = FileShareFragment.class.getSimpleName();

    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;
    @BindView(R.id.file_share_recyclerview)
    RecyclerView fileShareRecyclerView;

    private List<AbstractRemoteFile> abstractRemoteFiles;
    private FileShareRecyclerAdapter fileShareRecyclerAdapter;

    private boolean remoteFileShareLoaded = false;

    private String currentFolderUUID;

    private List<String> retrievedFolderUUIDList;

    public FileShareFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileShareFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileShareFragment newInstance() {
        FileShareFragment fragment = new FileShareFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        abstractRemoteFiles = new ArrayList<>();
        fileShareRecyclerAdapter = new FileShareRecyclerAdapter();

        retrievedFolderUUIDList = new ArrayList<>();

        Log.i(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_share, container, false);

        ButterKnife.bind(this, view);

        fileShareRecyclerView.setAdapter(fileShareRecyclerAdapter);
        fileShareRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Log.i(TAG, "onCreateView: ");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!remoteFileShareLoaded && !isHidden()) {

            currentFolderUUID = FNAS.userUUID;

            if (!retrievedFolderUUIDList.contains(currentFolderUUID)) {
                retrievedFolderUUIDList.add(currentFolderUUID);
            }

            FNAS.retrieveRemoteFileShare();
        }

        Log.i(TAG, "onResume: ");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        remoteFileShareLoaded = false;
    }

    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();
        if (action.equals(Util.REMOTE_FILE_SHARE_RETRIEVED)) {

            loadingLayout.setVisibility(View.GONE);

            OperationResult operationResult = operationEvent.getOperationResult();
            switch (operationResult) {
                case SUCCEED:

                    remoteFileShareLoaded = true;

                    if (LocalCache.RemoteFileShareList.size() == 0) {
                        noContentLayout.setVisibility(View.VISIBLE);
                        fileShareRecyclerView.setVisibility(View.GONE);
                    } else {
                        fileShareRecyclerView.setVisibility(View.VISIBLE);
                        noContentLayout.setVisibility(View.GONE);

                        abstractRemoteFiles.clear();
                        abstractRemoteFiles.addAll(LocalCache.RemoteFileShareList);
                        fileShareRecyclerAdapter.notifyDataSetChanged();
                    }

                    break;
                case FAIL:
                    noContentLayout.setVisibility(View.VISIBLE);
                    break;
            }

        } else if (action.equals(Util.REMOTE_FILE_RETRIEVED)) {

            OperationResult result = operationEvent.getOperationResult();
            switch (result) {
                case SUCCEED:

                    List<AbstractRemoteFile> abstractRemoteFileList = LocalCache.RemoteFileMapKeyIsUUID.get(((RetrieveFileOperationEvent) operationEvent).getFolderUUID()).listChildAbstractRemoteFileList();

                    if (abstractRemoteFileList.size() == 0) {
                        noContentLayout.setVisibility(View.VISIBLE);
                        fileShareRecyclerView.setVisibility(View.GONE);
                    } else {
                        fileShareRecyclerView.setVisibility(View.VISIBLE);
                        noContentLayout.setVisibility(View.GONE);

                        abstractRemoteFiles.clear();
                        abstractRemoteFiles.addAll(LocalCache.RemoteFileMapKeyIsUUID.get(((RetrieveFileOperationEvent) operationEvent).getFolderUUID()).listChildAbstractRemoteFileList());
                        fileShareRecyclerAdapter.notifyDataSetChanged();
                    }

                    break;
                case FAIL:
                    noContentLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }

    }


    public void onBackPressed() {

        retrievedFolderUUIDList.remove(retrievedFolderUUIDList.size() - 1);

        currentFolderUUID = retrievedFolderUUIDList.get(retrievedFolderUUIDList.size() - 1);

        if (currentFolderUUID.equals(FNAS.userUUID)) {
            FNAS.retrieveRemoteFileShare();
        } else {
            FNAS.retrieveRemoteFile(getActivity(), currentFolderUUID);
        }

    }

    public boolean notRootFolder() {

        return !currentFolderUUID.equals(FNAS.userUUID);
    }

    class FileShareRecyclerAdapter extends RecyclerView.Adapter<FileShareRecyclerAdapterViewHolder> {

        @Override
        public FileShareRecyclerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.remote_file_item_layout, parent, false);

            return new FileShareRecyclerAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FileShareRecyclerAdapterViewHolder holder, int position) {
            holder.refreshView(position);
        }

        @Override
        public int getItemCount() {
            return abstractRemoteFiles.size();
        }
    }

    class FileShareRecyclerAdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.file_icon)
        ImageView fileIcon;
        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.file_time)
        TextView fileTime;
        @BindView(R.id.remote_file_item_layout)
        LinearLayout remoteFileItemLayout;

        FileShareRecyclerAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        void refreshView(int position) {

            final AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(position);

            fileIcon.setImageResource(abstractRemoteFile.getImageResource());
            fileName.setText(abstractRemoteFile.getName());
            fileTime.setText(abstractRemoteFile.getTimeDateText());

            remoteFileItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    currentFolderUUID = abstractRemoteFile.getUuid();

                    retrievedFolderUUIDList.add(currentFolderUUID);

                    abstractRemoteFile.openAbstractRemoteFile(getActivity());
                }
            });
        }

    }

}
