package com.winsun.fruitmix.fileModule.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;

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

    private OnFileFragmentInteractionListener mListener;

    @BindView(R.id.downloading_recyclerview)
    RecyclerView fileDownloadingRecyclerView;
    @BindView(R.id.downloaded_recyclerview)
    RecyclerView fileDownladedRecyclerView;

    private DownloadingFileAdapter downloadingFileAdapter;
    private DownloadedFileAdapter downloadedFileAdapter;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_download, container, false);

        ButterKnife.bind(this, view);

        fileDownloadingRecyclerView.setAdapter(downloadingFileAdapter);
        fileDownloadingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        fileDownladedRecyclerView.setAdapter(downloadedFileAdapter);
        fileDownladedRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }


    class DownloadingFileAdapter extends RecyclerView.Adapter<DownloadingFileAdapterViewHolder> {

        @Override
        public DownloadingFileAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.downloading_file_item, parent, false);

            return new DownloadingFileAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DownloadingFileAdapterViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }


    class DownloadingFileAdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.downloading_progressbar)
        ProgressBar downloadingProgressBar;

        DownloadingFileAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this,itemView);
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

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    class DownloadedFileAdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.file_size)
        TextView fileSize;

        DownloadedFileAdapterViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this,itemView);
        }
    }

}
