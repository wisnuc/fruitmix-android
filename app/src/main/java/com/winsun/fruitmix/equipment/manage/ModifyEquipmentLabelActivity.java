package com.winsun.fruitmix.equipment.manage;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ActivityModifyEquipmentLabelBinding;
import com.winsun.fruitmix.equipment.manage.data.EquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.data.InjectEquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.viewmodel.ModifyEquipmentLabelViewModel;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.user.OperateUserViewModel;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class ModifyEquipmentLabelActivity extends BaseActivity {

    public static final String EQUIPMENT_LABEL_KEY = "equipment_label_key";

    private EquipmentInfoDataSource equipmentInfoDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityModifyEquipmentLabelBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_modify_equipment_label);

        equipmentInfoDataSource = InjectEquipmentInfoDataSource.provideInstance(this);

        final ModifyEquipmentLabelViewModel modifyEquipmentLabelViewModel = new ModifyEquipmentLabelViewModel();

        final String equipmentLabel = getIntent().getStringExtra(EQUIPMENT_LABEL_KEY);

        modifyEquipmentLabelViewModel.setEquipmentLabel(equipmentLabel);

        binding.setModifyEquipmentLabelViewModel(modifyEquipmentLabelViewModel);

        ToolbarViewModel toolbarViewModel = initToolBar(binding,binding.toolbarLayout,getString(R.string.modify_equipment_label));

        toolbarViewModel.showSelect.set(true);

        toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(this, R.color.eighty_seven_percent_white));

        toolbarViewModel.selectTextResID.set(R.string.finish_text);

        toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
            @Override
            public void onClick() {

                handleModifyEquipmentLabel(equipmentLabel, modifyEquipmentLabelViewModel);

            }
        });

    }


    private void handleModifyEquipmentLabel(String originalEquipmentLabel, ModifyEquipmentLabelViewModel modifyEquipmentLabelViewModel) {

        Util.hideSoftInput(this);

        final String modifiedEquipmentLabel = modifyEquipmentLabelViewModel.getEquipmentLabel();

        if (modifiedEquipmentLabel.equals(originalEquipmentLabel)) {

            finish();

            return;

        }

        showProgressDialog(getString(R.string.operating_title, getString(R.string.modify_equipment_label)));

        equipmentInfoDataSource.modifyEquipmentLabel(modifiedEquipmentLabel, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                dismissDialog();

                Intent intent = new Intent();
                intent.putExtra(EquipmentInfoActivity.NEW_EQUIPMENT_LABEL_KEY, modifiedEquipmentLabel);

                setResult(RESULT_OK, intent);

                showToast(getString(R.string.success, getString(R.string.modify_equipment_label)));

                finish();
            }

            @Override
            public void onFail(OperationResult operationResult) {

                dismissDialog();

                showToast(operationResult.getResultMessage(ModifyEquipmentLabelActivity.this));

            }
        });

    }


}
