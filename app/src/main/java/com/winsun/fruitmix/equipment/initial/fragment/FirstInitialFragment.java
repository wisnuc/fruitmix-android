package com.winsun.fruitmix.equipment.initial.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.databinding.DiskDetailBinding;
import com.winsun.fruitmix.databinding.DiskItemBinding;
import com.winsun.fruitmix.databinding.FragmentFirstInitialBinding;
import com.winsun.fruitmix.equipment.initial.data.EquipmentDiskVolume;
import com.winsun.fruitmix.equipment.initial.data.InitialEquipmentRepository;
import com.winsun.fruitmix.equipment.initial.data.InjectInitialEquipmentRepository;
import com.winsun.fruitmix.equipment.initial.data.ShowFirstInitialEquipmentInfoListener;
import com.winsun.fruitmix.equipment.initial.viewmodel.DiskDetailViewModel;
import com.winsun.fruitmix.equipment.initial.viewmodel.DiskVolumeViewModel;
import com.winsun.fruitmix.equipment.initial.viewmodel.FirstInitialFragmentViewModel;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class FirstInitialFragment extends Fragment implements ShowFirstInitialEquipmentInfoListener {

    private String mEquipmentIP;
    private String mParam2;

    private OnFirstInitialFragmentInteractionListener mListener;

    private InitialEquipmentRepository mInitialEquipmentRepository;

    private FirstInitialFragmentViewModel mFirstInitialFragmentViewModel;

    private ImageButton selectDiskModeIV;

    public static final int SINGLE_MODE = 1;
    public static final int RAID0_MODE = 2;
    public static final int RAID1_MODE = 3;

    private int currentSelectDiskMode;

    private List<DiskVolumeViewModel> selectedDiskVolumeViewModels;

    private DiskRecyclerViewAdapter diskRecyclerViewAdapter;

    public FirstInitialFragment() {
        // Required empty public constructor

        mInitialEquipmentRepository = InjectInitialEquipmentRepository.provideInstance(getContext());

    }

    public void setEquipmentIP(String equipmentIP) {
        mEquipmentIP = equipmentIP;
    }

    public void setSelectedDiskVolumeViewModels(List<DiskVolumeViewModel> selectedDiskVolumeViewModels) {
        this.selectedDiskVolumeViewModels = selectedDiskVolumeViewModels;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public int getCurrentSelectDiskMode() {
        return currentSelectDiskMode;
    }

    public List<DiskVolumeViewModel> getSelectDisk() {

        List<DiskVolumeViewModel> result = new ArrayList<>();

        List<DiskVolumeViewModel> diskVolumeViewModels = diskRecyclerViewAdapter.getDiskVolumeViewModels();

        for (DiskVolumeViewModel diskVolumeViewModel : diskVolumeViewModels) {
            if (diskVolumeViewModel.isSelected())
                result.add(diskVolumeViewModel);
        }

        return result;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        FragmentFirstInitialBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_first_initial, container, false);

        selectDiskModeIV = binding.selectDiskMode;

        mFirstInitialFragmentViewModel = new FirstInitialFragmentViewModel();

        mFirstInitialFragmentViewModel.installDiskMode.set(getString(R.string.not_set_disk_mode));

        binding.setViewModel(mFirstInitialFragmentViewModel);

        RecyclerView recyclerView = binding.diskRecyclerView;

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        diskRecyclerViewAdapter = new DiskRecyclerViewAdapter();

        mInitialEquipmentRepository.getStorageInfo(mEquipmentIP, new BaseLoadDataCallback<EquipmentDiskVolume>() {
            @Override
            public void onSucceed(List<EquipmentDiskVolume> data, OperationResult operationResult) {

                List<DiskVolumeViewModel> diskVolumeViewModels = new ArrayList<>(data.size());

                for (EquipmentDiskVolume equipmentDiskVolume : data)
                    diskVolumeViewModels.add(new DiskVolumeViewModel(equipmentDiskVolume));

                if (selectedDiskVolumeViewModels != null) {

                    for (DiskVolumeViewModel selectedDiskVolumeViewModel : selectedDiskVolumeViewModels) {

                        for (DiskVolumeViewModel diskVolumeViewModel : diskVolumeViewModels) {
                            if (diskVolumeViewModel.getName().equals(selectedDiskVolumeViewModel.getName()))
                                diskVolumeViewModel.setSelected(true);
                        }

                    }

                }

                refreshViewAfterSelectDisk(diskVolumeViewModels);

                diskRecyclerViewAdapter.setDiskVolumeViewModels(diskVolumeViewModels);
                diskRecyclerViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFail(OperationResult operationResult) {

            }
        });

        recyclerView.setAdapter(diskRecyclerViewAdapter);

        return binding.getRoot();

    }

    @Override
    public void showEquipmentInDialog(@NotNull DiskVolumeViewModel diskVolumeViewModel) {

        Context context = getContext();

        if (context == null)
            return;

        DiskDetailBinding binding = DiskDetailBinding.inflate(LayoutInflater.from(context), null, false);

        binding.setDiskDetailViewModel(new DiskDetailViewModel(diskVolumeViewModel));

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.disk_detail))
                .setView(binding.getRoot())
                .create().show();

    }

    private class DiskRecyclerViewAdapter extends RecyclerView.Adapter<DiskViewHolder> {

        private List<DiskVolumeViewModel> mDiskVolumeViewModels;

        public DiskRecyclerViewAdapter() {
            mDiskVolumeViewModels = new ArrayList<>();
        }

        public void setDiskVolumeViewModels(List<DiskVolumeViewModel> diskVolumeViewModels) {
            mDiskVolumeViewModels.clear();
            mDiskVolumeViewModels.addAll(diskVolumeViewModels);
        }

        public List<DiskVolumeViewModel> getDiskVolumeViewModels() {
            return mDiskVolumeViewModels;
        }

        @Override
        public DiskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            DiskItemBinding binding = DiskItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent, false);

            return new DiskViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(DiskViewHolder holder, int position) {

            holder.getViewDataBinding().setVariable(BR.diskVolumeViewModel, mDiskVolumeViewModels.get(position));

            holder.getViewDataBinding().setVariable(BR.showEquipmentInfo, FirstInitialFragment.this);

            holder.getViewDataBinding().executePendingBindings();

            holder.refreshView(mDiskVolumeViewModels, position);
        }

        @Override
        public int getItemCount() {
            return mDiskVolumeViewModels.size();
        }
    }

    private class DiskViewHolder extends BindingViewHolder {

        private CheckBox mCheckBox;

        public DiskViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            DiskItemBinding binding = (DiskItemBinding) viewDataBinding;
            mCheckBox = binding.checkbox;
        }

        void refreshView(final List<DiskVolumeViewModel> diskVolumeViewModels, final int position) {

            final DiskVolumeViewModel diskVolumeViewModel = diskVolumeViewModels.get(position);

            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    diskVolumeViewModel.setSelected(isChecked);

                    refreshViewAfterSelectDisk(diskVolumeViewModels);

                }
            });

        }

    }

    private void refreshViewAfterSelectDisk(List<DiskVolumeViewModel> diskVolumeViewModels) {

        int selectCount = 0;

        for (DiskVolumeViewModel diskVolumeViewModel : diskVolumeViewModels) {

            if (diskVolumeViewModel.isSelected())
                selectCount++;

        }

        if (selectCount == 0) {

            mFirstInitialFragmentViewModel.installDiskMode.set(getString(R.string.not_set_disk_mode));

            currentSelectDiskMode = 0;

            selectDiskModeIV.setOnClickListener(null);

            selectDiskModeIV.setEnabled(false);

        } else if (selectCount == 1) {

            mFirstInitialFragmentViewModel.installDiskMode.set(getString(R.string.single_mode));

            currentSelectDiskMode = 1;

            selectDiskModeIV.setOnClickListener(null);

            selectDiskModeIV.setEnabled(false);

        } else {

            mFirstInitialFragmentViewModel.installDiskMode.set(getString(R.string.not_set_disk_mode));

            //fix bug:choose two disk but not set mode,user still can enter next step
            currentSelectDiskMode = 0;

            selectDiskModeIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSelectDiskModeDialog();
                }
            });

            selectDiskModeIV.setEnabled(true);

        }

        mListener.onSelectDiskCountChange(currentSelectDiskMode);


    }

    private int temporarySelectMode = -1;

    private void showSelectDiskModeDialog() {

        Context context = getContext();

        if (context == null)
            return;

        String[] modes = {getString(R.string.single_mode), getString(R.string.raid0_mode), getString(R.string.raid1_mode)};

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.select_disk_mode))
                .setSingleChoiceItems(modes, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        temporarySelectMode = which + 1;
                    }
                })
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        currentSelectDiskMode = temporarySelectMode;

                        setInstallDiskModelText();

                        mListener.onSelectDiskCountChange(currentSelectDiskMode);

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        temporarySelectMode = -1;
                    }
                })
                .create().show();

    }

    private void setInstallDiskModelText() {

        switch (currentSelectDiskMode) {
            case SINGLE_MODE:
                mFirstInitialFragmentViewModel.installDiskMode.set(getString(R.string.single_mode));
                break;
            case RAID0_MODE:
                mFirstInitialFragmentViewModel.installDiskMode.set(getString(R.string.raid0_mode));
                break;
            case RAID1_MODE:
                mFirstInitialFragmentViewModel.installDiskMode.set(getString(R.string.raid1_mode));
                break;
        }

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFirstInitialFragmentInteractionListener) {
            mListener = (OnFirstInitialFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFirstInitialFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFirstInitialFragmentInteractionListener {
        void onSelectDiskCountChange(int currentSelectMode);
    }
}
