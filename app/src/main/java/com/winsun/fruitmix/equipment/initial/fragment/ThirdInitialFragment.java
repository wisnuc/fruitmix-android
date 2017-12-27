package com.winsun.fruitmix.equipment.initial.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.FragmentThirdInitialBinding;
import com.winsun.fruitmix.databinding.InstallDiskItemBinding;
import com.winsun.fruitmix.equipment.initial.data.EquipmentDiskVolume;
import com.winsun.fruitmix.equipment.initial.viewmodel.DiskVolumeViewModel;
import com.winsun.fruitmix.equipment.initial.viewmodel.ThirdInitialFragmentViewModel;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.List;


public class ThirdInitialFragment extends Fragment {

    private String mUserName;
    private String mMode;

    private List<DiskVolumeViewModel> mSelectDiskVolumeViewModels;

    public ThirdInitialFragment() {
        // Required empty public constructor

        mUserName = "";
        mMode = "";

        mSelectDiskVolumeViewModels = new ArrayList<>();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentThirdInitialBinding binding = FragmentThirdInitialBinding.inflate(inflater, container, false);

        ThirdInitialFragmentViewModel thirdInitialFragmentViewModel = new ThirdInitialFragmentViewModel();

        thirdInitialFragmentViewModel.userName.set(getString(R.string.colon, getString(R.string.mode), mUserName));
        thirdInitialFragmentViewModel.mode.set(getString(R.string.colon, getString(R.string.user_name), mMode));

        binding.setThirdInitialViewModel(thirdInitialFragmentViewModel);

        RecyclerView recyclerView = binding.recyclerview;

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        InstallDiskItemAdapter installDiskItemAdapter = new InstallDiskItemAdapter();

        installDiskItemAdapter.setDiskVolumeViewModels(mSelectDiskVolumeViewModels);
        installDiskItemAdapter.notifyDataSetChanged();

        recyclerView.setAdapter(installDiskItemAdapter);

        return binding.getRoot();

    }

    public void refreshView(String userName, String mode, List<DiskVolumeViewModel> diskVolumeViewModels) {

        mUserName = userName;
        mMode = mode;
        mSelectDiskVolumeViewModels = diskVolumeViewModels;

    }


    private class InstallDiskItemAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<DiskVolumeViewModel> mDiskVolumeViewModels;

        public InstallDiskItemAdapter() {
            mDiskVolumeViewModels = new ArrayList<>();
        }

        public void setDiskVolumeViewModels(List<DiskVolumeViewModel> diskVolumeViewModels) {
            mDiskVolumeViewModels.clear();
            mDiskVolumeViewModels.addAll(diskVolumeViewModels);
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            InstallDiskItemBinding binding = InstallDiskItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent, false);

            return new BindingViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            holder.getViewDataBinding().setVariable(BR.diskVolumeViewModel, mDiskVolumeViewModels.get(position));

        }

        @Override
        public int getItemCount() {
            return mDiskVolumeViewModels.size();
        }
    }


}
