package com.winsun.fruitmix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.winsun.fruitmix.databinding.ActivityEquipmentSearchBinding;

import com.winsun.fruitmix.equipment.initial.InitialEquipmentActivity;
import com.winsun.fruitmix.equipment.maintenance.MaintenanceActivity;
import com.winsun.fruitmix.equipment.search.EquipmentPresenter;
import com.winsun.fruitmix.equipment.search.EquipmentSearchView;
import com.winsun.fruitmix.equipment.search.EquipmentSearchViewModel;
import com.winsun.fruitmix.equipment.search.WeChatLoginListener;
import com.winsun.fruitmix.equipment.search.data.EquipmentDataSource;

import com.winsun.fruitmix.equipment.search.data.InjectEquipment;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.login.InjectLoginUseCase;
import com.winsun.fruitmix.login.LoginUseCase;
import com.winsun.fruitmix.equipment.search.data.Equipment;
import com.winsun.fruitmix.equipment.search.data.EquipmentSearchManager;
import com.winsun.fruitmix.retrieve.file.from.other.app.RetrieveFilePresenter;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.ToastUtil;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.wxapi.MiniProgram;
import com.winsun.fruitmix.wxapi.WXEntryActivity;

import me.relex.circleindicator.CircleIndicator;

public class EquipmentSearchActivity extends BaseActivity implements View.OnClickListener, EquipmentSearchView, WeChatLoginListener {

    public static final String TAG = "EquipmentSearchActivity";

    private Context mContext;

    private ViewPager equipmentViewPager;
    private RecyclerView equipmentUserRecyclerView;

    private ViewGroup viewPagerLayout;
    private Toolbar toolbar;

    private EquipmentPresenter equipmentPresenter;

    private String uploadFilePath = null;

    public static void gotoEquipmentActivity(Activity activity, boolean shouldStopService) {
        Intent intent = new Intent(activity, EquipmentSearchActivity.class);
        intent.putExtra(Util.KEY_SHOULD_STOP_SERVICE, shouldStopService);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEquipmentSearchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_equipment_search);

        binding.setEquipmentSearchView(this);

        toolbar = binding.toolbar;

        viewPagerLayout = binding.viewpagerLayout;

        equipmentViewPager = binding.equipmentViewpager;

        equipmentUserRecyclerView = binding.equipmentUserRecyclerview;

        mContext = this;

        uploadFilePath = getIntent().getStringExtra(TestReceiveActivity.UPLOAD_FILE_PATH);

        setBackgroundColor(R.color.equipment_ui_blue);

        binding.addIpBtn.setOnClickListener(this);

        LoadingViewModel loadingViewModel = new LoadingViewModel(this);

        binding.setLoadingViewModel(loadingViewModel);

        EquipmentSearchViewModel equipmentSearchViewModel = new EquipmentSearchViewModel();

        binding.setEquipmentSearchViewModel(equipmentSearchViewModel);

        setSupportActionBar(binding.toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);

        EquipmentDataSource mEquipmentDataSource = InjectEquipment.provideEquipmentDataSource(mContext);

        LoginUseCase loginUseCase = InjectLoginUseCase.provideLoginUseCase(mContext);

        equipmentPresenter = new EquipmentPresenter(loadingViewModel, equipmentSearchViewModel, this,
                mEquipmentDataSource, loginUseCase,
                InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this));

        binding.setWechatLoginListener(this);

        equipmentViewPager.setAdapter(equipmentPresenter.getEquipmentViewPagerAdapter());

        CircleIndicator circleIndicator = binding.viewpagerIndicator;

        circleIndicator.setViewPager(equipmentViewPager);

        PagerAdapter pagerAdapter = equipmentViewPager.getAdapter();

        if (pagerAdapter != null)
            pagerAdapter.registerDataSetObserver(circleIndicator.getDataSetObserver());

        equipmentUserRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        equipmentUserRecyclerView.setItemAnimator(new DefaultItemAnimator());
        equipmentUserRecyclerView.setAdapter(equipmentPresenter.getEquipmentUserRecyclerViewAdapter());

        equipmentViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                equipmentPresenter.refreshEquipment(position);
            }
        });

        final TextView equipmentUserTitle = binding.equipmentUserTitle;

        equipmentUserRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    ViewCompat.setElevation(equipmentUserTitle, Util.dip2px(mContext, 0f));
                else
                    ViewCompat.setElevation(equipmentUserTitle, Util.dip2px(mContext, 2f));

            }
        });

//        Equipment equipment_blue = new Equipment("", Collections.singletonList("10.10.9.126"), 3000);
//        equipment_blue.setModel("");
//        equipment_blue.setSerialNumber("");
//        getUserList(equipment_blue);

        equipmentPresenter.onCreate();

    }

    @Override
    public void setBackgroundColor(int color) {

        Util.setStatusBarColor(this, color);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, color));
        viewPagerLayout.setBackgroundColor(ContextCompat.getColor(this, color));

    }

    @Override
    public void handleLoginWithUserSucceed(boolean autoUpload) {

        if (!autoUpload)
            ToastUtil.showToast(mContext, getString(R.string.photo_auto_upload_already_close));

        setResult(RESULT_OK);

        handleStartActivityAfterLoginSucceed();

    }

    @Override
    public void handleLoginWithUserFail(Equipment equipment, User user) {

        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(Util.GATEWAY, Util.HTTP + equipment.getHosts().get(0));

        String label = equipment.getEquipmentTypeInfo().getFormatLabel(this);

//        if (label.isEmpty())
//            label = equipment.getEquipmentName();

        intent.putExtra(Util.USER_GROUP_NAME, label);
        intent.putExtra(Util.USER_NAME, user.getUserName());
        intent.putExtra(Util.USER_UUID, user.getUuid());
        intent.putExtra(Util.USER_BG_COLOR, user.getDefaultAvatarBgColor());

        startActivityForResult(intent, Util.KEY_LOGIN_REQUEST_CODE);
    }

    @Override
    public int getCurrentViewPagerItem() {
        return equipmentViewPager.getCurrentItem();
    }

    @Override
    protected void onResume() {
        super.onResume();

        equipmentPresenter.onResume();

//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        equipmentPresenter.onPause();

//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        equipmentPresenter.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == Util.KEY_LOGIN_REQUEST_CODE || requestCode == InitialEquipmentActivity.REQUEST_CODE) && resultCode == RESULT_OK) {
            setResult(RESULT_OK);

            handleStartActivityAfterLoginSucceed();

        } else if (requestCode == Util.KEY_MANUAL_INPUT_IP_REQUEST_CODE && resultCode == RESULT_OK) {

            String ip = data.getStringExtra(Util.KEY_MANUAL_INPUT_IP);
            equipmentPresenter.handleInputIpbyByUser(ip);
        } else if (requestCode == MaintenanceActivity.REQUEST_CODE) {

            if (resultCode == MaintenanceActivity.RESULT_INITIAL_EQUIPMENT) {

                setResult(RESULT_OK);
                handleStartActivityAfterLoginSucceed();

            } else if (resultCode == MaintenanceActivity.RESULT_START_SYSTEM) {

                equipmentPresenter.refreshEquipmentUsers(equipmentViewPager.getCurrentItem());

            }

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_ip_btn:
                Intent intent = new Intent(mContext, CreateNewEquipmentActivity.class);
                startActivityForResult(intent, Util.KEY_MANUAL_INPUT_IP_REQUEST_CODE);
                break;
            default:
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (getIntent().getBooleanExtra(Util.KEY_SHOULD_STOP_SERVICE, true))
            ButlerService.stopButlerService(mContext);

    }

    @Override
    public void wechatLogin() {

        WXEntryActivity.setWxEntryGetWeChatCodeCallback(new WXEntryActivity.WXEntryGetWeChatCodeCallback() {
            @Override
            public void succeed(String code) {

                Log.d(TAG, "get code and start login");

                equipmentPresenter.loginInThread(code);

            }

            @Override
            public void fail(int resID) {

                Log.d(TAG, "login with wechat code fail");

                showToast(getString(resID));
            }
        });

        IWXAPI iwxapi = MiniProgram.registerToWX(this);

        MiniProgram.sendAuthRequest(iwxapi);

    }

    @Override
    public void handleStartActivityAfterLoginSucceed() {

        Log.d(TAG, "handleStartActivityAfterLoginSucceed: uploadFilePath: " + uploadFilePath);

        if (uploadFilePath == null) {
            startActivity(new Intent(mContext, NavPagerActivity.class));

            finishView();
        } else {

            RetrieveFilePresenter retrieveFilePresenter = new RetrieveFilePresenter();

            retrieveFilePresenter.handleUploadFilePath(uploadFilePath, this);

        }

    }

    @Override
    public EquipmentSearchManager getEquipmentSearchManager() {
        return InjectEquipment.provideEquipmentSearchManager(mContext);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void gotoActivity(EquipmentSearchViewModel equipmentSearchViewModel) {

        if (equipmentSearchViewModel.getEquipmentStateCode() == EquipmentDataSource.EQUIPMENT_UNINITIALIZED) {

            Intent intent = InitialEquipmentActivity.getIntentForStart(equipmentSearchViewModel.getIp(), equipmentSearchViewModel.getEquipmentName(),
                    getString(R.string.initial_title), this);

            startActivityForResult(intent, InitialEquipmentActivity.REQUEST_CODE);

        } else if (equipmentSearchViewModel.getEquipmentStateCode() == EquipmentDataSource.EQUIPMENT_MAINTENANCE) {

            MaintenanceActivity.startActivity(equipmentSearchViewModel.getIp(), equipmentSearchViewModel.getEquipmentName(), this);

        }


    }


}
