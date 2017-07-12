package com.winsun.fruitmix.fileModule.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.interfaces.IShowHideFragmentListener;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FileShareFragment extends Fragment implements IShowHideFragmentListener {

    public static final String TAG = FileShareFragment.class.getSimpleName();

    @BindView(R.id.no_content_layout)
    LinearLayout noContentLayout;
    @BindView(R.id.no_content_imageview)
    ImageView noContentImageView;
    @BindView(R.id.no_content_textview)
    TextView noContentTextView;


    public FileShareFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileShareFragment.
     */
    public static FileShareFragment newInstance() {
        FileShareFragment fragment = new FileShareFragment();

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
