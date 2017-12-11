package com.winsun.fruitmix.equipment.initial.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.FragmentSecondInitialBinding;
import com.winsun.fruitmix.equipment.initial.presenter.SecondInitialPresenter;
import com.winsun.fruitmix.user.OperateUserViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SecondInitialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SecondInitialFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mIP;
    private String mParam2;

    private SecondInitialPresenter mSecondInitialPresenter;

    private OperateUserViewModel operateUserViewModel;

    public SecondInitialFragment() {
        // Required empty public constructor

        mSecondInitialPresenter = new SecondInitialPresenter();
    }


    public void setIP(String IP) {
        mIP = IP;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentSecondInitialBinding binding = FragmentSecondInitialBinding.inflate(inflater, container, false);

        operateUserViewModel = new OperateUserViewModel();

        binding.setCreateUserViewModel(operateUserViewModel);

        return binding.getRoot();

    }

    public OperateUserViewModel getOperateUserViewModel() {
        return operateUserViewModel;
    }

    public boolean checkUserNameAndPassword(){

        return mSecondInitialPresenter.checkUserNameAndPassword(getContext(),operateUserViewModel);

    }




}
