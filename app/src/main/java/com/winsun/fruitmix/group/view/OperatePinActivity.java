package com.winsun.fruitmix.group.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.winsun.fruitmix.BaseToolbarActivity;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ActivityOperatePinBinding;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.group.data.viewmodel.OperatePinViewModel;
import com.winsun.fruitmix.group.presenter.CreatePinPresenter;
import com.winsun.fruitmix.group.presenter.ModifyPinPresenter;
import com.winsun.fruitmix.group.presenter.OperatePinListener;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.Util;

public class OperatePinActivity extends BaseToolbarActivity implements CreatePinPresenter, OperatePinListener, ModifyPinPresenter {

    private ActivityOperatePinBinding binding;

    private GroupRepository groupRepository;

    private String groupUUID;

    public static final String CREATE_PIN = "create_pin";

    public static final String KEY_PIN_UUID = "key_pin_uuid";

    private boolean createPin = false;

    private String pinUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createPin = getIntent().getBooleanExtra(CREATE_PIN, true);

        if (createPin) {

            String createPin = "创建置顶";

            toolbarViewModel.titleText.set(createPin);
            binding.operatePinBtn.setText(createPin);
        } else {

            String modifyPin = "编辑置顶";

            toolbarViewModel.titleText.set(modifyPin);
            binding.operatePinBtn.setText(modifyPin);
        }

        groupUUID = getIntent().getStringExtra(Util.KEY_GROUP_UUID);

        groupRepository = InjectGroupDataSource.provideGroupRepository(this);

        OperatePinViewModel operatePinViewModel = new OperatePinViewModel();

        binding.setOperatePinViewModel(operatePinViewModel);

        binding.setOperatePinListener(this);

        if (!createPin) {
            pinUUID = getIntent().getStringExtra(KEY_PIN_UUID);
        }

    }

    @Override
    protected View generateContent() {

        binding = ActivityOperatePinBinding.inflate(LayoutInflater.from(this), null, false);

        return binding.getRoot();
    }

    @Override
    protected String getToolbarTitle() {

        return "创建置顶";

    }

    @Override
    public void createPin(OperatePinViewModel operatePinViewModel) {

        Pin pin = new Pin(Util.createLocalUUid(), operatePinViewModel.getPingName());

        showProgressDialog("正在创建置顶");

        groupRepository.insertPin(groupUUID, pin, new BaseOperateDataCallback<Pin>() {
            @Override
            public void onSucceed(Pin data, OperationResult result) {
                dismissDialog();

                setResult(RESULT_OK);

                finishView();

            }

            @Override
            public void onFail(OperationResult result) {
                dismissDialog();

                showToast("创建失败");

                OperatePinActivity.this.setResult(RESULT_CANCELED);

                finishView();
            }
        });

    }


    @Override
    public void operatePin(OperatePinViewModel operatePinViewModel) {

        if (createPin)
            createPin(operatePinViewModel);
        else
            modifyPin(operatePinViewModel);

    }

    @Override
    public void modifyPin(OperatePinViewModel operatePinViewModel) {

        showProgressDialog("正在编辑置顶");

        groupRepository.modifyPin(groupUUID, operatePinViewModel.getPingName(), pinUUID, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                dismissDialog();

                setResult(RESULT_OK);

                finishView();

            }

            @Override
            public void onFail(OperationResult result) {
                dismissDialog();

                showToast("编辑失败");

                OperatePinActivity.this.setResult(RESULT_CANCELED);

                finishView();
            }
        });


    }
}
