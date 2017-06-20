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

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.eventbus.RetrieveFileOperationEvent;
import com.winsun.fruitmix.fileModule.interfaces.OnFileInteractionListener;
import com.winsun.fruitmix.fileModule.model.AbstractRemoteFile;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.FileUtil;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.model.OperationResultType;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewholder.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class FileShareFragment extends Fragment implements IShowHideFragmentListener {

    public static final String TAG = FileShareFragment.class.getSimpleName();

    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;
    @BindView(R.id.no_content_textview)
    TextView noContentTextView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_share, container, false);

        ButterKnife.bind(this, view);

        noContentImageView.setImageResource(R.drawable.no_file);

        noContentTextView.setText(getString(R.string.no_file_shares));

        return view;
    }

    public void handleTitle() {

        onFileInteractionListener.setToolbarTitle(getString(R.string.file));
        onFileInteractionListener.setNavigationIcon(R.drawable.menu_black);
        onFileInteractionListener.setDefaultNavigationOnClickListener();

    }

    @Override
    public void show() {
        MobclickAgent.onPageStart("FileShareFragment");
    }

    @Override
    public void hide() {
        MobclickAgent.onPageEnd("FileShareFragment");
    }

    public boolean handleBackPressedOrNot() {
        return false;
    }

}
