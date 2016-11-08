package com.winsun.fruitmix.fileModule.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.fileModule.interfaces.OnFileFragmentInteractionListener;

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

    public FileShareFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_file_share, container, false);

        ButterKnife.bind(this,view);

        return view;
    }

    public void onBackPressed(){

    }


}
