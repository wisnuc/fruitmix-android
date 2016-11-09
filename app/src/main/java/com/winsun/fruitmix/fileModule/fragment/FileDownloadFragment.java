package com.winsun.fruitmix.fileModule.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.DownloadStateChangedEvent;
import com.winsun.fruitmix.fileModule.download.DownloadState;
import com.winsun.fruitmix.fileModule.download.FileDownloadItem;
import com.winsun.fruitmix.fileModule.download.FileDownloadManager;
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;
import com.winsun.fruitmix.util.FileUtil;

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

    public FileDownloadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileDownloadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileDownloadFragment newInstance() {
        FileDownloadFragment fragment = new FileDownloadFragment();
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
    }

    @Override
    public void onResume() {
        super.onResume();

        downloadedFileAdapter.notifyDataSetChanged();
        downloadingFileAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(DownloadStateChangedEvent downloadStateChangedEvent) {

        FileDownloadManager fileDownloadManager = FileDownloadManager.INSTANCE;

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

    class DownloadedFileAdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.file_size)
        TextView fileSize;

        DownloadedFileAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        void refreshView(int position) {

            FileDownloadItem fileDownloadItem = downloadedItems.get(position);

            DownloadState downloadState = fileDownloadItem.getDownloadState();

            fileName.setText(fileDownloadItem.getFileName());

            if (downloadState.equals(DownloadState.FINISHED)) {
                fileSize.setText(FileUtil.formatFileSize(fileDownloadItem.getFileSize()));
            } else {
                fileSize.setText(getString(R.string.download_failed));
            }

        }
    }

}
