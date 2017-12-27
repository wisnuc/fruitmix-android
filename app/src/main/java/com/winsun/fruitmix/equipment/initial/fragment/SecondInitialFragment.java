package com.winsun.fruitmix.equipment.initial.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.FragmentSecondInitialBinding;
import com.winsun.fruitmix.equipment.initial.presenter.SecondInitialPresenter;
import com.winsun.fruitmix.user.OperateUserViewModel;

public class SecondInitialFragment extends Fragment {

    private String mIP;

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

    public void setOperateUserViewModel(OperateUserViewModel operateUserViewModel) {
        this.operateUserViewModel = operateUserViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentSecondInitialBinding binding = FragmentSecondInitialBinding.inflate(inflater, container, false);

        if (operateUserViewModel == null)
            operateUserViewModel = new OperateUserViewModel();

        binding.setCreateUserViewModel(operateUserViewModel);

        return binding.getRoot();

    }

    public OperateUserViewModel getOperateUserViewModel() {
        return operateUserViewModel;
    }

    public boolean checkUserNameAndPassword() {

        return mSecondInitialPresenter.checkUserNameAndPassword(getContext(), operateUserViewModel);

    }


}
