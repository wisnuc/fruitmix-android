package com.winsun.fruitmix.file.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.file.interfaces.OnFragmentInteractionListener;
import com.winsun.fruitmix.file.model.AbstractRemoteFile;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.OperationResult;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.file_recyclerview)
    RecyclerView fileRecyclerView;

    private FileRecyclerViewAdapter fileRecyclerViewAdapter;

    private List<AbstractRemoteFile> abstractRemoteFiles;

    private boolean remoteFileLoaded = false;

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

        //TODO:add get file information function;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        if(!remoteFileLoaded){
            User user = LocalCache.RemoteUserMapKeyIsUUID.get(FNAS.userUUID);

            FNAS.retrieveRemoteFile(getActivity(), user.getHome());
        }

    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleOperationResult(OperationEvent operationEvent){

        String action = operationEvent.getAction();
        if(action.equals(Util.REMOTE_FILE_RETRIEVED)){

            OperationResult result = operationEvent.getOperationResult();
            switch (result){
                case SUCCEED:

                    remoteFileLoaded = true;

                    fillAbstractFileList();
                    fileRecyclerViewAdapter.notifyDataSetChanged();

                    break;
                case FAIL:
                    break;
            }

        }

    }

    private void fillAbstractFileList(){

        abstractRemoteFiles.addAll(LocalCache.RemoteFileMapKeyIsUUID.values());

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    class FileRecyclerViewAdapter extends RecyclerView.Adapter<FileViewHolder> {
        @Override
        public int getItemCount() {
            return abstractRemoteFiles.size();
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

        @BindView(R.id.file_icon)
        ImageView fileIcon;
        @BindView(R.id.file_name)
        TextView fileName;
        @BindView(R.id.file_time)
        TextView fileTime;


        FileViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        void refreshView(int position) {
            AbstractRemoteFile abstractRemoteFile = abstractRemoteFiles.get(position);

            if (abstractRemoteFile.isFolder()) {
                fileIcon.setImageResource(R.drawable.folder_icon);
            } else {
                fileIcon.setImageResource(R.drawable.file_icon);
                fileTime.setText(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss上传").format(new Date(Long.parseLong(abstractRemoteFile.getTime()))));
            }
            fileName.setText(abstractRemoteFile.getName());
        }

    }
}
