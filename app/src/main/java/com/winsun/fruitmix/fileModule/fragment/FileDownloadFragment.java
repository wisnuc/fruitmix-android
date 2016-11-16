package com.winsun.fruitmix.fileModule.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.DownloadStateChangedEvent;
import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;
import com.winsun.fruitmix.fileModule.model.BottomMenuItem;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFileFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FileDownloadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileDownloadFragment extends Fragment {

    public static final String TAG = FileDownloadFragment.class.getSimpleName();

    @BindView(R.id.downloading_recyclerview)
    RecyclerView fileDownloadingRecyclerView;
    @BindView(R.id.downloaded_recyclerview)
    RecyclerView fileDownloadedRecyclerView;

    private DownloadingFileAdapter downloadingFileAdapter;
    private DownloadedFileAdapter downloadedFileAdapter;

    private List<FileDownloadItem> downloadingItems;
    private List<FileDownloadItem> downloadedItems;

    private CustomHandler customHandler;

    public static final int DOWNLOAD_STATE_CHANGED = 0x0010;

    public static final int DELAY_TIME_MILLISECOND = 0;

    private boolean selectMode = false;

    private List<String> selectDownloadedItemUUID;

    private OnFileFragmentInteractionListener onFileFragmentInteractionListener;

    private FileDownloadManager fileDownloadManager;

    public FileDownloadFragment() {
        // Required empty public constructor
    }

    public void setOnFileFragmentInteractionListener(OnFileFragmentInteractionListener onFileFragmentInteractionListener) {
        this.onFileFragmentInteractionListener = onFileFragmentInteractionListener;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileDownloadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileDownloadFragment newInstance(OnFileFragmentInteractionListener onFileFragmentInteractionListener) {
        FileDownloadFragment fragment = new FileDownloadFragment();
        fragment.setOnFileFragmentInteractionListener(onFileFragmentInteractionListener);

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        downloadingFileAdapter = new DownloadingFileAdapter();
        downloadedFileAdapter = new DownloadedFileAdapter();

        downloadingItems = new ArrayList<>();
        downloadedItems = new ArrayList<>();

        customHandler = new CustomHandler(this);

        selectDownloadedItemUUID = new ArrayList<>();

        fileDownloadManager = FileDownloadManager.INSTANCE;

        Log.i(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_download, container, false);

        ButterKnife.bind(this, view);

        fileDownloadingRecyclerView.setAdapter(downloadingFileAdapter);
        fileDownloadingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        fileDownloadedRecyclerView.setAdapter(downloadedFileAdapter);
        fileDownloadedRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Log.i(TAG, "onCreateView: ");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        FNAS.retrieveDownloadedFile();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(DownloadStateChangedEvent downloadStateChangedEvent) {

        Log.i(TAG, "handleEvent: downloadState:" + downloadStateChangedEvent.getDownloadState());

        DownloadState downloadState = downloadStateChangedEvent.getDownloadState();

        if (downloadState == DownloadState.FINISHED) {
            customHandler.sendEmptyMessageDelayed(DOWNLOAD_STATE_CHANGED, DELAY_TIME_MILLISECOND);
        } else if (downloadState == DownloadState.NO_ENOUGH_SPACE) {

            Toast.makeText(getActivity(), getString(R.string.no_enough_space), Toast.LENGTH_SHORT).show();

        } else {
            refreshView();
        }

    }

    private static class CustomHandler extends Handler {

        WeakReference<FileDownloadFragment> weakReference = null;

        CustomHandler(FileDownloadFragment fileDownloadFragment) {
            weakReference = new WeakReference<>(fileDownloadFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            weakReference.get().refreshView();
        }
    }

    private void refreshView() {
        filterFileDownloadItems(fileDownloadManager.getFileDownloadItems());

        downloadingFileAdapter.notifyDataSetChanged();
        downloadedFileAdapter.notifyDataSetChanged();
    }

    private void filterFileDownloadItems(List<FileDownloadItem> fileDownloadItems) {

        downloadingItems.clear();
        downloadedItems.clear();

        for (FileDownloadItem fileDownloadItem : fileDownloadItems) {

            DownloadState downloadState = fileDownloadItem.getDownloadState();

            if (downloadState.equals(DownloadState.FINISHED) || downloadState.equals(DownloadState.ERROR)) {
                downloadedItems.add(fileDownloadItem);
            } else {
                downloadingItems.add(fileDownloadItem);
            }

        }

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

            BottomMenuItem deleteSelectItem = new BottomMenuItem(getString(R.string.delete_text)) {
                @Override
                public void handleOnClickEvent() {

                    fileDownloadManager.deleteFileDownloadItem(selectDownloadedItemUUID);

                    refreshView();

                    selectMode = false;
                    refreshSelectMode(selectMode);
                }
            };

            bottomMenuItems.add(deleteSelectItem);

        } else {

            BottomMenuItem selectItem = new BottomMenuItem(getString(R.string.choose_text)) {
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

    private void refreshSelectMode(boolean selectMode) {
        this.selectMode = selectMode;

        if (!selectMode) {
            selectDownloadedItemUUID.clear();
        }

        downloadingFileAdapter.notifyDataSetChanged();
        downloadedFileAdapter.notifyDataSetChanged();
    }

    class DownloadingFileAdapter extends RecyclerView.Adapter<DownloadingFileAdapterViewHolder> {

        @Override
        public DownloadingFileAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.downloading_file_item, parent, false);

            return new DownloadingFileAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DownloadingFileAdapterViewHolder holder, int position) {
            holder.refreshView(position);
        }

        @Override
        public int getItemCount() {
            return downloadingItems.size();
        }
    }


    class DownloadingFileAdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.downloading_progressbar)
        ProgressBar downloadingProgressBar;

        DownloadingFileAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            downloadingProgressBar.setMax(100);
        }

        void refreshView(int position) {

            FileDownloadItem fileDownloadItem = downloadingItems.get(position);

            fileName.setText(fileDownloadItem.getFileName());

            Log.i(TAG, "refreshView: currentDownloadSize:" + fileDownloadItem.getFileCurrentDownloadSize() + " fileSize:" + fileDownloadItem.getFileSize());

            float currentProgress = fileDownloadItem.getFileCurrentDownloadSize() * 100 / fileDownloadItem.getFileSize();

            Log.i(TAG, "refreshView: currentProgress:" + currentProgress);

            downloadingProgressBar.setProgress((int) currentProgress);

        }

    }

    class DownloadedFileAdapter extends RecyclerView.Adapter<DownloadedFileAdapterViewHolder> {

        @Override
        public DownloadedFileAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.downloaded_file_item, parent, false);

            return new DownloadedFileAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DownloadedFileAdapterViewHolder holder, int position) {
            holder.refreshView(position);
        }

        @Override
        public int getItemCount() {
            return downloadedItems.size();
        }
    }

    class DownloadedFileAdapterViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.file_icon_bg)
        ImageView fileIconBg;
        @BindView(R.id.file_icon)
        ImageView fileIcon;
        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.file_size)
        TextView fileSize;
        @BindView(R.id.remote_file_item_layout)
        LinearLayout downloadedItemLayout;

        DownloadedFileAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        void refreshView(int position) {

            final FileDownloadItem fileDownloadItem = downloadedItems.get(position);

            final DownloadState downloadState = fileDownloadItem.getDownloadState();

            fileName.setText(fileDownloadItem.getFileName());

            if (downloadState.equals(DownloadState.FINISHED)) {
                fileSize.setText(FileUtil.formatFileSize(fileDownloadItem.getFileSize()));
            } else {
                fileSize.setText(getString(R.string.download_failed));
            }

            fileIcon.setVisibility(View.VISIBLE); 

            if (selectMode) {
                fileIconBg.setVisibility(View.VISIBLE);

                downloadedItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toggleFileInSelectedFile(fileDownloadItem.getFileUUID());
                        toggleFileIconBgResource(fileDownloadItem.getFileUUID());
                    }
                });


            } else {
                fileIconBg.setVisibility(View.INVISIBLE);

                downloadedItemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (downloadState.equals(DownloadState.FINISHED)) {
                            FileUtil.openAbstractRemoteFile(getActivity(), fileDownloadItem.getFileName());
                        }

                    }
                });

            }

        }

        private void toggleFileIconBgResource(String fileUUID) {
            if (selectDownloadedItemUUID.contains(fileUUID)) {
                fileIconBg.setBackgroundResource(R.drawable.check_circle_selected);
                fileIcon.setVisibility(View.INVISIBLE);
            } else {
                fileIconBg.setBackgroundResource(R.drawable.round_circle);
                fileIcon.setVisibility(View.VISIBLE);
            }
        }

        private void toggleFileInSelectedFile(String fileUUID) {
            if (selectDownloadedItemUUID.contains(fileUUID)) {
                selectDownloadedItemUUID.remove(fileUUID);
            } else {
                selectDownloadedItemUUID.add(fileUUID);
            }
        }
    }

}
