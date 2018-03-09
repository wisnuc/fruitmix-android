package com.winsun.fruitmix.equipment.maintenance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.ActiveView;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackWrapper;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ActivityMaintenanceBinding;
import com.winsun.fruitmix.databinding.SystemManageItemBinding;
import com.winsun.fruitmix.databinding.VolumeStateItemBinding;
import com.winsun.fruitmix.equipment.initial.InitialEquipmentActivity;
import com.winsun.fruitmix.equipment.maintenance.data.InjectMaintenanceDataSource;
import com.winsun.fruitmix.equipment.maintenance.data.MaintenanceDataSource;
import com.winsun.fruitmix.equipment.maintenance.data.VolumeState;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.recyclerview.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.List;

import static com.winsun.fruitmix.equipment.initial.InitialEquipmentActivity.EQUIPMENT_IP_KEY;
import static com.winsun.fruitmix.equipment.initial.InitialEquipmentActivity.EQUIPMENT_NAME_KEY;

public class MaintenanceActivity extends BaseActivity implements MaintenanceView, ActiveView {

    public static final int REQUEST_CODE = 1;

    public static final int RESULT_INITIAL_EQUIPMENT = 2;
    public static final int RESULT_START_SYSTEM = 3;

    private MaintenanceDataSource mMaintenanceDataSource;

    private MaintenanceAdapter mMaintenanceAdapter;

    private LoadingViewModel loadingViewModel;

    private NoContentViewModel noContentViewModel;

    public static final VolumeState VOLUME_SYSTEM_MANAGE = new VolumeState(0, "", "", "", false, false, false, false,
            false);

    private boolean onDestroy = false;

    private String ip;
    private String equipmentName;

    public static void startActivity(String ip, String equipmentName, Activity activity) {
        Intent intent = new Intent(activity, MaintenanceActivity.class);
        intent.putExtra(InitialEquipmentActivity.EQUIPMENT_IP_KEY, ip);
        intent.putExtra(InitialEquipmentActivity.EQUIPMENT_NAME_KEY, equipmentName);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMaintenanceBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_maintenance);

        initToolBar(binding, binding.toolbarLayout, getString(R.string.maintenance));

        ip = getIntent().getStringExtra(EQUIPMENT_IP_KEY);
        equipmentName = getIntent().getStringExtra(EQUIPMENT_NAME_KEY);

        loadingViewModel = new LoadingViewModel();
        binding.setLoadingViewModel(loadingViewModel);

        noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentText(getString(R.string.equipment_system_error));

        binding.setNoContentViewModel(noContentViewModel);

        mMaintenanceDataSource = InjectMaintenanceDataSource.provideInstance(this);

        RecyclerView recyclerView = binding.diskRecyclerView;

        mMaintenanceAdapter = new MaintenanceAdapter();

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mMaintenanceAdapter);

        refreshDisk();

    }

    private void refreshDisk() {

        mMaintenanceDataSource.getDiskState(ip, new BaseLoadDataCallbackWrapper<>(new BaseLoadDataCallback<VolumeState>() {
            @Override
            public void onSucceed(List<VolumeState> data, OperationResult operationResult) {

                loadingViewModel.showLoading.set(false);
                noContentViewModel.showNoContent.set(false);

                data.add(VOLUME_SYSTEM_MANAGE);

                mMaintenanceAdapter.setVolumeStates(data);
                mMaintenanceAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                loadingViewModel.showLoading.set(false);
                noContentViewModel.showNoContent.set(true);

            }
        }, this));

    }


    @Override
    public void enterInitialSystem() {

        Intent intent = InitialEquipmentActivity.getIntentForStart(ip, equipmentName, getString(R.string.reinstall_title), this);

        startActivityForResult(intent, InitialEquipmentActivity.REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == InitialEquipmentActivity.REQUEST_CODE && resultCode == RESULT_OK) {

            setResult(RESULT_INITIAL_EQUIPMENT);

            finishView();

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        onDestroy = true;
    }

    @Override
    public boolean isActive() {
        return !onDestroy;
    }

    private class MaintenanceAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        static final int TYPE_VOLUME = 1;
        static final int TYPE_SYSTEM_MANAGE = 2;

        private List<VolumeState> mVolumeStates;

        MaintenanceAdapter() {
            mVolumeStates = new ArrayList<>();
        }

        void setVolumeStates(List<VolumeState> volumeStates) {
            mVolumeStates.clear();
            mVolumeStates.addAll(volumeStates);
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            BindingViewHolder bindingViewHolder;

            switch (viewType) {
                case TYPE_SYSTEM_MANAGE:

                    SystemManageItemBinding systemManageItemBinding = SystemManageItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);

                    bindingViewHolder = new MaintenanceSystemManageViewHolder(systemManageItemBinding);

                    break;
                case TYPE_VOLUME:

                    VolumeStateItemBinding binding = VolumeStateItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false);

                    bindingViewHolder = new MaintenanceDiskViewHolder(binding);

                    break;
                default:
                    bindingViewHolder = null;
            }

            return bindingViewHolder;
        }


        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            if (holder instanceof MaintenanceDiskViewHolder)
                ((MaintenanceDiskViewHolder) holder).refreshView(mVolumeStates.get(position));
            else if (holder instanceof MaintenanceSystemManageViewHolder)
                ((MaintenanceSystemManageViewHolder) holder).refreshView();

        }


        @Override
        public int getItemCount() {
            return mVolumeStates.size();
        }

        @Override
        public int getItemViewType(int position) {

            if (position == getItemCount() - 1) {
                return TYPE_SYSTEM_MANAGE;
            } else
                return TYPE_VOLUME;

        }
    }

    private class MaintenanceDiskViewHolder extends BindingViewHolder {

        MaintenanceDiskViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        public void refreshView(final VolumeState volumeState) {

            final Context context = getViewDataBinding().getRoot().getContext();

            VolumeStateViewModel volumeStateViewModel = new VolumeStateViewModel(volumeState, context);

            getViewDataBinding().setVariable(BR.volumeStateViewModel, volumeStateViewModel);

            getViewDataBinding().executePendingBindings();

            VolumeStateItemBinding binding = (VolumeStateItemBinding) getViewDataBinding();

            binding.startSystem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startSystem(volumeState.getUuid(), context);

                }
            });

        }

    }

    private void startSystem(String volumeUUID, final Context context) {

        showProgressDialog(getString(R.string.operating_title, getString(R.string.start)));

        mMaintenanceDataSource.startSystem(ip, volumeUUID, new BaseOperateDataCallback<Void>() {
            @Override
            public void onSucceed(Void data, OperationResult result) {

                dismissDialog();

                showToast(getString(R.string.success, getString(R.string.start)));

                setResult(RESULT_START_SYSTEM);
                finishView();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                dismissDialog();

                showToast(operationResult.getResultMessage(context));

            }
        });

    }

    private class MaintenanceSystemManageViewHolder extends BindingViewHolder {

        MaintenanceSystemManageViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        public void refreshView() {

            getViewDataBinding().setVariable(BR.maintenanceView, MaintenanceActivity.this);

            getViewDataBinding().executePendingBindings();

        }

    }


}
