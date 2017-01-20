package com.winsun.fruitmix.fileModule.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveFileOperationEvent;
import com.winsun.fruitmix.fileModule.interfaces.OnFileInteractionListener;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
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
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;

    private List<AbstractRemoteFile> abstractRemoteFiles;
    private FileShareRecyclerAdapter fileShareRecyclerAdapter;

    private boolean remoteFileShareLoaded = false;

    private String currentFolderUUID;
    private String currentFolderName;

    private List<String> retrievedFolderUUIDList;
    private List<String> retrievedFolderNameList;

    private OnFileInteractionListener onFileInteractionListener;

    public FileShareFragment() {
        // Required empty public constructor
    }

    public void setOnFileInteractionListener(OnFileInteractionListener onFileInteractionListener) {
        this.onFileInteractionListener = onFileInteractionListener;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileShareFragment.
     */
    public static FileShareFragment newInstance(OnFileInteractionListener onFileInteractionListener) {
        FileShareFragment fragment = new FileShareFragment();
        fragment.setOnFileInteractionListener(onFileInteractionListener);
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
        retrievedFolderNameList = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_share, container, false);

        ButterKnife.bind(this, view);

        fileShareRecyclerView.setAdapter(fileShareRecyclerAdapter);
        fileShareRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        noContentImageView.setImageResource(R.drawable.no_file);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!remoteFileShareLoaded && !isHidden()) {

            if (FNAS.userUUID == null)
                currentFolderUUID = LocalCache.getUserUUID(getContext());
            else
                currentFolderUUID = FNAS.userUUID;

            if (!retrievedFolderUUIDList.contains(currentFolderUUID)) {
                retrievedFolderUUIDList.add(currentFolderUUID);
                retrievedFolderNameList.add(getString(R.string.file));
            }

            FNAS.retrieveRemoteFileShare();
        }

    }

    public void handleTitle() {
        if (handleBackPressedOrNot()) {

            onFileInteractionListener.setToolbarTitle(currentFolderName);
            onFileInteractionListener.setNavigationIcon(R.drawable.ic_back);
            onFileInteractionListener.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

        } else {
            onFileInteractionListener.setToolbarTitle(getString(R.string.file));
            onFileInteractionListener.setNavigationIcon(R.drawable.menu);
            onFileInteractionListener.setDefaultNavigationOnClickListener();
        }
    }

    public void refreshUser() {

        fileShareRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        remoteFileShareLoaded = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void handleOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        OperationResultType operationResultType = operationEvent.getOperationResult().getOperationResultType();

        loadingLayout.setVisibility(View.INVISIBLE);

        if (action.equals(Util.REMOTE_FILE_SHARE_RETRIEVED)) {

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

            switch (operationResultType) {
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

        if (loadingLayout.getVisibility() == View.VISIBLE) {
            return;
        }

        loadingLayout.setVisibility(View.VISIBLE);

        retrievedFolderUUIDList.remove(retrievedFolderUUIDList.size() - 1);

        currentFolderUUID = retrievedFolderUUIDList.get(retrievedFolderUUIDList.size() - 1);

        retrievedFolderNameList.remove(retrievedFolderNameList.size() - 1);
        currentFolderName = retrievedFolderNameList.get(retrievedFolderNameList.size() - 1);

        if (currentFolderUUID.equals(FNAS.userUUID)) {
            FNAS.retrieveRemoteFileShare();
        } else {
            FNAS.retrieveRemoteFile(getActivity(), currentFolderUUID);
        }

        handleTitle();

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
        @BindView(R.id.content_layout)
        RelativeLayout contentLayout;
        @BindView(R.id.owner)
        TextView ownerTextView;

        FolderShareRecyclerAdapterViewHolder(View itemView) {
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

            setOwner(abstractRemoteFile, ownerTextView);

            remoteFolderItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentFolderUUID = abstractRemoteFile.getUuid();

                    retrievedFolderUUIDList.add(currentFolderUUID);

                    currentFolderName = abstractRemoteFile.getName();
                    retrievedFolderNameList.add(currentFolderName);

                    loadingLayout.setVisibility(View.VISIBLE);

                    abstractRemoteFile.openAbstractRemoteFile(getActivity());

                    handleTitle();
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
        @BindView(R.id.content_layout)
        RelativeLayout contentLayout;
        @BindView(R.id.owner)
        TextView ownerTextView;

        FileShareRecyclerAdapterViewHolder(View itemView) {
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

            AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(position);

            itemMenu.setVisibility(View.GONE);

            setOwner(abstractRemoteFile, ownerTextView);

            fileName.setText(abstractRemoteFile.getName());
            fileTime.setText(abstractRemoteFile.getTimeDateText());

        }

    }

    private void setOwner(AbstractRemoteFile abstractRemoteFile, TextView ownerTextView) {
        List<String> owners = abstractRemoteFile.getOwners();
        if (!owners.isEmpty()) {
            String owner = owners.get(0);

            if (LocalCache.RemoteUserMapKeyIsUUID.containsKey(owner)) {

                ownerTextView.setVisibility(View.VISIBLE);
                ownerTextView.setText(LocalCache.RemoteUserMapKeyIsUUID.get(owner).getUserName());
            }
        }
    }

}
