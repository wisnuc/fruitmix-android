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

import com.squareup.leakcanary.RefWatcher;
import com.winsun.fruitmix.CustomApplication;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveFileOperationEvent;
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResultType;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BaseRecyclerViewHolder;

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

    @Override
    public void onDestroy() {
        super.onDestroy();

        RefWatcher refWatcher = CustomApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();
        if (action.equals(Util.REMOTE_FILE_SHARE_RETRIEVED)) {

            loadingLayout.setVisibility(View.GONE);

            OperationResultType operationResultType = operationEvent.getOperationResult().getOperationResultType();
            switch (operationResultType) {
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
                default:
                    noContentLayout.setVisibility(View.VISIBLE);
                    break;
            }

        } else if (action.equals(Util.REMOTE_FILE_RETRIEVED)) {

            OperationResultType result = operationEvent.getOperationResult().getOperationResultType();
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
                default:
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

    public boolean handleBackPressedOrNot() {
        return notRootFolder();
    }

    private boolean notRootFolder() {

        return !currentFolderUUID.equals(FNAS.userUUID);
    }

    class FileShareRecyclerAdapter extends RecyclerView.Adapter<BaseRecyclerViewHolder> {

        private static final int VIEW_FILE = 0;
        private static final int VIEW_FOLDER = 1;

        @Override
        public BaseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            BaseRecyclerViewHolder viewHolder;

            switch (viewType) {
                case VIEW_FILE:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.remote_file_item_layout, parent, false);
                    viewHolder = new FileShareRecyclerAdapterViewHolder(view);
                    break;
                case VIEW_FOLDER:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.remote_folder_item_layout, parent, false);
                    viewHolder = new FolderShareRecyclerAdapterViewHolder(view);
                    break;
                default:
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.remote_file_item_layout, parent, false);
                    viewHolder = new FileShareRecyclerAdapterViewHolder(view);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
            holder.refreshView(position);
        }

        @Override
        public int getItemCount() {
            return abstractRemoteFiles.size();
        }

        @Override
        public int getItemViewType(int position) {
            return abstractRemoteFiles.get(position).isFolder() ? VIEW_FOLDER : VIEW_FILE;
        }
    }

    class FolderShareRecyclerAdapterViewHolder extends BaseRecyclerViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.remote_folder_item_layout)
        LinearLayout remoteFolderItemLayout;
        @BindView(R.id.item_menu)
        ImageView itemMenu;

        FolderShareRecyclerAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void refreshView(int position) {

            final AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(position);

            itemMenu.setVisibility(View.INVISIBLE);

            fileName.setText(abstractRemoteFile.getName());

            remoteFolderItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentFolderUUID = abstractRemoteFile.getUuid();

                    retrievedFolderUUIDList.add(currentFolderUUID);

                    abstractRemoteFile.openAbstractRemoteFile(getActivity());
                }
            });
        }
    }


    class FileShareRecyclerAdapterViewHolder extends BaseRecyclerViewHolder {

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


        FileShareRecyclerAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void refreshView(int position) {

            AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(position);

            itemMenu.setVisibility(View.INVISIBLE);

            fileName.setText(abstractRemoteFile.getName());
            fileTime.setText(abstractRemoteFile.getTimeDateText());

        }

    }

}
