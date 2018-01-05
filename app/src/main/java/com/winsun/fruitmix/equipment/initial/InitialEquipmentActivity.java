package com.winsun.fruitmix.equipment.initial;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.ActivityInitialEquipmentBinding;
import com.winsun.fruitmix.equipment.initial.data.InitialEquipmentRepository;
import com.winsun.fruitmix.equipment.initial.data.InjectInitialEquipmentRepository;
import com.winsun.fruitmix.equipment.initial.fragment.FifthFragment;
import com.winsun.fruitmix.equipment.initial.fragment.FirstInitialFragment;
import com.winsun.fruitmix.equipment.initial.fragment.FourthFragment;
import com.winsun.fruitmix.equipment.initial.fragment.SecondInitialFragment;
import com.winsun.fruitmix.equipment.initial.fragment.ThirdInitialFragment;
import com.winsun.fruitmix.equipment.initial.viewmodel.DiskVolumeViewModel;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.person.info.BindWeChatUserPresenter;
import com.winsun.fruitmix.person.info.InjectPersonInfoDataSource;
import com.winsun.fruitmix.person.info.PersonInfoView;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.token.InjectTokenRemoteDataSource;
import com.winsun.fruitmix.token.LoadTokenParam;
import com.winsun.fruitmix.user.OperateUserViewModel;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.util.ArrayList;
import java.util.List;

import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;
import me.drozdzynski.library.steppers.interfaces.OnCancelAction;
import me.drozdzynski.library.steppers.interfaces.OnClickContinue;
import me.drozdzynski.library.steppers.interfaces.OnFinishAction;
import me.drozdzynski.library.steppers.interfaces.OnSkipStepAction;

public class InitialEquipmentActivity extends BaseActivity implements PersonInfoView, FirstInitialFragment.OnFirstInitialFragmentInteractionListener {

    public static final int REQUEST_CODE = 0x1100;

    public static final String EQUIPMENT_IP_KEY = "ip_key";
    public static final String EQUIPMENT_NAME_KEY = "name_key";

    public static final String INITIAL_EQUIPMENT_TITLE_KEY = "initial_equipment_title_key";

    private String ip;
    private String equipmentName;

    private FirstInitialFragment mFirstInitialFragment;
    private SecondInitialFragment mSecondInitialFragment;

    private SteppersView steppersView;

    private List<SteppersItem> steppersItems;

    private ThirdInitialFragment mThirdInitialFragment;

    private InitialEquipmentRepository mInitialEquipmentRepository;

    private String userName;
    private String userPwd;
    private String mMode;
    private List<DiskVolumeViewModel> mDiskVolumeViewModels;

    private OperateUserViewModel mOperateUserViewModel;

    private User firstUser;

    private BindWeChatUserPresenter mBindWeChatUserPresenter;
    private LoginUseCase mLoginUseCase;

    public static Intent getIntentForStart(String ip, String equipmentName, String title, Activity activity) {
        Intent intent = new Intent(activity, InitialEquipmentActivity.class);
        intent.putExtra(InitialEquipmentActivity.EQUIPMENT_IP_KEY, ip);
        intent.putExtra(InitialEquipmentActivity.EQUIPMENT_NAME_KEY, equipmentName);
        intent.putExtra(INITIAL_EQUIPMENT_TITLE_KEY, title);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityInitialEquipmentBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_initial_equipment);

        mInitialEquipmentRepository = InjectInitialEquipmentRepository.provideInstance(this);

        mLoginUseCase = InjectLoginUseCase.provideLoginUseCase(this);

        mBindWeChatUserPresenter = new BindWeChatUserPresenter(InjectUser.provideRepository(this),
                InjectSystemSettingDataSource.provideSystemSettingDataSource(this),
                this, InjectPersonInfoDataSource.provideInstance(this), InjectTokenRemoteDataSource.provideTokenDataSource(this));

        String title = getIntent().getStringExtra(INITIAL_EQUIPMENT_TITLE_KEY);

        if (title == null || title.isEmpty())
            title = getString(R.string.initial_title);

        initToolBar(binding, binding.toolbarLayout, title);

        ip = getIntent().getStringExtra(EQUIPMENT_IP_KEY);
        equipmentName = getIntent().getStringExtra(EQUIPMENT_NAME_KEY);

        SteppersView.Config config = initialStepperViewConfig();

        steppersItems = new ArrayList<>();

        SteppersItem steppersItem = createFirstSteppersItem();

        steppersItems.add(steppersItem);

        steppersItems.add(createSecondSteppersItem());

        steppersItems.add(createThirdSteppersItem());

        steppersItems.add(createFourthSteppersItem());

        steppersItems.add(createFifthSteppersItem());

        steppersView = binding.steppersView;
        steppersView.setConfig(config);
        steppersView.setItems(steppersItems);
        steppersView.build();

    }

    private void initToolbar(ActivityInitialEquipmentBinding binding) {
        Toolbar mToolbar = binding.toolbarLayout.toolbar;

        binding.toolbarLayout.title.setTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_white));

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.login_ui_blue));

        Util.setStatusBarColor(this, R.color.login_ui_blue);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        toolbarViewModel.titleText.set(getString(R.string.initial_title));

        toolbarViewModel.navigationIconResId.set(R.drawable.ic_back);

        toolbarViewModel.setBaseView(this);

        binding.setToolbarViewModel(toolbarViewModel);

    }

    private SteppersView.Config initialStepperViewConfig() {

        SteppersView.Config config = new SteppersView.Config();

        config.setNextStepText(getString(R.string.next_step));
        config.setPreStepText(getString(R.string.pre_step));
        config.setSkipStepText(getString(R.string.skip_step));
        config.setFinishStepText(getString(R.string.finish_step));

        config.setFragmentManager(getSupportFragmentManager());

        config.setOnFinishAction(new OnFinishAction() {
            @Override
            public void onFinish() {

                setResult(RESULT_OK);
                finish();

            }
        });

        return config;

    }

    private SteppersItem createFirstSteppersItem() {

        final SteppersItem steppersItem = new SteppersItem();
        steppersItem.setLabel(getString(R.string.create_disk_title));
        steppersItem.setLabelTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_black));
        steppersItem.setSubLabel(getString(R.string.create_disk_subtitle));
        steppersItem.setSubLabelTextColor(ContextCompat.getColor(this, R.color.logout_btn_bg));

        mFirstInitialFragment = new FirstInitialFragment();
        mFirstInitialFragment.setEquipmentIP(ip);

        steppersItem.setFragment(mFirstInitialFragment);

        steppersItem.setPositiveButtonEnable(false);

        steppersItem.setOnClickContinue(new OnClickContinue() {
            @Override
            public void onClick() {

                if (steppersView.getCurrentStep() == 0 && mFirstInitialFragment.getCurrentSelectDiskMode() != 0) {

                    steppersView.nextStep();

                    saveFirstFragmentData();

                }


            }
        });

        return steppersItem;

    }

    private void saveFirstFragmentData() {
        mDiskVolumeViewModels = mFirstInitialFragment.getSelectDisk();

        int mode = mFirstInitialFragment.getCurrentSelectDiskMode();

        mMode = convertMode(mode);
    }

    @Override
    public void onSelectDiskCountChange(int currentSelectMode) {

        SteppersItem steppersItem = steppersItems.get(0);

        steppersItem.setPositiveButtonEnable(currentSelectMode > 0);

    }

    private SteppersItem createSecondSteppersItem() {

        final SteppersItem steppersItem = new SteppersItem();

        steppersItem.setLabel(getString(R.string.create_first_user_title));
        steppersItem.setLabelTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_black));

        steppersItem.setSubLabel(getString(R.string.create_first_user_subtitle));
        steppersItem.setSubLabelTextColor(ContextCompat.getColor(this, R.color.fifty_four_percent_black));

        mSecondInitialFragment = new SecondInitialFragment();
        mSecondInitialFragment.setIP(ip);

        steppersItem.setFragment(mSecondInitialFragment);

        steppersItem.setOnClickContinue(new OnClickContinue() {
            @Override
            public void onClick() {

                if(steppersView.getCurrentStep() != 1)
                    return;

                Util.hideSoftInput(InitialEquipmentActivity.this);

                if (mSecondInitialFragment.checkUserNameAndPassword()) {

                    refreshThirdFragment();

                    steppersView.nextStep();

                }

            }
        });

        steppersItem.setOnCancelAction(new OnCancelAction() {
            @Override
            public void onCancel() {

                mFirstInitialFragment.setSelectedDiskVolumeViewModels(mDiskVolumeViewModels);

            }
        });

        return steppersItem;

    }

    private SteppersItem createThirdSteppersItem() {

        final SteppersItem steppersItem = new SteppersItem();

        steppersItem.setLabel(getString(R.string.confirm_install));
        steppersItem.setLabelTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_black));

        steppersItem.setSubLabel("");

        mThirdInitialFragment = new ThirdInitialFragment();

        steppersItem.setFragment(mThirdInitialFragment);

        steppersItem.setNextBtnText(getString(R.string.create));

        steppersItem.setOnClickContinue(new OnClickContinue() {
            @Override
            public void onClick() {

                installSystem();

            }
        });


        steppersItem.setOnCancelAction(new OnCancelAction() {
            @Override
            public void onCancel() {

                mSecondInitialFragment.setOperateUserViewModel(mOperateUserViewModel);

            }
        });

        return steppersItem;

    }

    private void installSystem() {

        showProgressDialog(getString(R.string.operating_title, getString(R.string.create)));

        mInitialEquipmentRepository.installSystem(ip, userName, userPwd, mMode,
                mDiskVolumeViewModels, new BaseOperateDataCallback<User>() {
                    @Override
                    public void onSucceed(User data, OperationResult result) {

                        firstUser = data;

                        login();

                    }

                    @Override
                    public void onFail(OperationResult operationResult) {

                        dismissDialog();

                        showToast(operationResult.getResultMessage(InitialEquipmentActivity.this));

                    }
                });


    }

    private void login() {

        LoadTokenParam loadTokenParam = new LoadTokenParam(ip, firstUser.getUuid(), userPwd, equipmentName);

        mLoginUseCase.loginWithLoadTokenParam(loadTokenParam, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                dismissDialog();

                showToast(getString(R.string.success, getString(R.string.create)));

                steppersView.nextStep();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                dismissDialog();

                showToast(operationResult.getResultMessage(InitialEquipmentActivity.this));

            }
        });


    }


    private void refreshThirdFragment() {

        mOperateUserViewModel = mSecondInitialFragment.getOperateUserViewModel();

        userName = mOperateUserViewModel.getUserName();
        userPwd = mOperateUserViewModel.getUserPassword();

        mThirdInitialFragment.refreshView(userName, mMode, mDiskVolumeViewModels);

    }

    private String convertMode(int mode) {

        switch (mode) {

            case FirstInitialFragment.SINGLE_MODE:
                return "single";
            case FirstInitialFragment.RAID0_MODE:
                return "raid0";
            case FirstInitialFragment.RAID1_MODE:
                return "raid1";

        }

        return "";

    }


    private SteppersItem createFourthSteppersItem() {

        SteppersItem steppersItem = new SteppersItem();

        steppersItem.setLabel(getString(R.string.bind_wechat_user));
        steppersItem.setLabelTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_black));

        steppersItem.setSubLabel("");

        steppersItem.setPreStepable(false);

        steppersItem.setNextBtnText(getString(R.string.bind));

        steppersItem.setOnClickContinue(new OnClickContinue() {
            @Override
            public void onClick() {

                if(steppersView.getCurrentStep() != 3)
                    return;

                mBindWeChatUserPresenter.bindWeChatUser();

            }
        });

        steppersItem.setFragment(new FourthFragment());

        steppersItem.setSkippable(true, new OnSkipStepAction() {
            @Override
            public void onSkipStep() {

                steppersItems.get(steppersItems.size() - 1).setPreStepable(true);

            }
        });

        return steppersItem;


    }

    @Override
    public void handleBindSucceed() {

        SteppersItem steppersItem = steppersItems.get(steppersItems.size() - 1);

        steppersItem.setPreStepable(false);

        steppersView.nextStep();

    }

    private SteppersItem createFifthSteppersItem() {

        SteppersItem steppersItem = new SteppersItem();

        steppersItem.setLabel(getString(R.string.enter_system));

        steppersItem.setLabelTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_black));

        steppersItem.setSubLabel("");

        steppersItem.setFragment(new FifthFragment());

        return steppersItem;

    }


    @Override
    public Context getContext() {
        return this;
    }


}
