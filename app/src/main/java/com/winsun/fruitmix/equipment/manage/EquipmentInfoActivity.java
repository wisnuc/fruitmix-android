package com.winsun.fruitmix.equipment.manage;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityEquipmentInfoBinding;
import com.winsun.fruitmix.equipment.manage.data.EquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.data.InjectEquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.presenter.BaseEquipmentInfoPresenter;
import com.winsun.fruitmix.equipment.manage.presenter.EquipmentInfoPresenter;
import com.winsun.fruitmix.equipment.manage.presenter.EquipmentNetworkPresenter;
import com.winsun.fruitmix.equipment.manage.presenter.EquipmentTimePresenter;
import com.winsun.fruitmix.equipment.manage.view.EquipmentInfoView;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.system.setting.SystemSettingDataSource;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class EquipmentInfoActivity extends BaseActivity implements EquipmentInfoView {

    public static final String TAG = EquipmentInfoActivity.class.getSimpleName();

    public static final int EQUIPMENT_INFO = 0x1000;
    public static final int NETWORK_INFO = 0x1001;
    public static final int TIME_INFO = 0x1002;

    public static final String INFO_TYPE_KEY = "info_type_key";

    private RecyclerView equipmentInfoRecyclerView;

    private BaseEquipmentInfoPresenter equipmentInfoPresenter;

    public static final int ENTER_MODIFY_EQUIPMENT_LABEL_REQUEST_CODE = 0x1004;

    public static final String NEW_EQUIPMENT_LABEL_KEY = "new_equipment_label_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEquipmentInfoBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_equipment_info);

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        binding.setLoadingViewModel(loadingViewModel);

        NoContentViewModel noContentViewModel = new NoContentViewModel();
        noContentViewModel.setNoContentImgResId(R.drawable.no_file);
        noContentViewModel.setNoContentText(getString(R.string.no_equipment_info));

        binding.setNoContentViewModel(noContentViewModel);

        int infoType = getIntent().getIntExtra(INFO_TYPE_KEY, EQUIPMENT_INFO);

        EquipmentInfoDataSource equipmentInfoDataSource = InjectEquipmentInfoDataSource.provideInstance(this);

        String title;

        switch (infoType) {
            case EQUIPMENT_INFO:
                equipmentInfoPresenter = new EquipmentInfoPresenter(equipmentInfoDataSource, this, loadingViewModel, noContentViewModel);

                title = getString(R.string.equipment_info);

                break;
            case NETWORK_INFO:

                SystemSettingDataSource systemSettingDataSource = InjectSystemSettingDataSource.provideSystemSettingDataSource(this);

                equipmentInfoPresenter = new EquipmentNetworkPresenter(equipmentInfoDataSource, this,
                        loadingViewModel, noContentViewModel,systemSettingDataSource);

                title = getString(R.string.network_info);

                break;
            case TIME_INFO:
                equipmentInfoPresenter = new EquipmentTimePresenter(equipmentInfoDataSource, this, loadingViewModel, noContentViewModel);

                title = getString(R.string.time_info);

                break;
            default:
                equipmentInfoPresenter = new EquipmentInfoPresenter(equipmentInfoDataSource, this, loadingViewModel, noContentViewModel);

                title = getString(R.string.equipment_info);

                Log.d(TAG, "refreshView: should not enter default case");
        }

        initToolBar(binding,binding.toolbarLayout,title);

        equipmentInfoRecyclerView = binding.equipmentInfoRecyclerview;

        equipmentInfoRecyclerView.setItemAnimator(new DefaultItemAnimator());
        equipmentInfoRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        equipmentInfoRecyclerView.setAdapter(equipmentInfoPresenter.getEquipmentInfoRecyclerViewAdapter());

        equipmentInfoPresenter.refreshEquipmentInfoItem();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        equipmentInfoPresenter.onDestroy();
    }

    @Override
    public void showEquipmentInfoRecyclerView() {
        equipmentInfoRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissEquipmentInfoRecyclerView() {
        equipmentInfoRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void enterModifyEquipmentLabelActivity(String equipmentLabel) {

        Intent intent = new Intent(this,ModifyEquipmentLabelActivity.class);
        intent.putExtra(ModifyEquipmentLabelActivity.EQUIPMENT_LABEL_KEY,equipmentLabel);

        startActivityForResult(intent,ENTER_MODIFY_EQUIPMENT_LABEL_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ENTER_MODIFY_EQUIPMENT_LABEL_REQUEST_CODE && resultCode == RESULT_OK){

            String modifiedEquipmentLabel = data.getStringExtra(NEW_EQUIPMENT_LABEL_KEY);

            ((EquipmentInfoPresenter)equipmentInfoPresenter).refreshEquipmentLabel(modifiedEquipmentLabel);

        }

    }
}
