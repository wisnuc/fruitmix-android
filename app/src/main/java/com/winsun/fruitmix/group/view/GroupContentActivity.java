package com.winsun.fruitmix.group.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.AnimatorBuilder;
import com.winsun.fruitmix.databinding.ActivityGroupContentBinding;
import com.winsun.fruitmix.eventbus.MqttMessageEvent;
import com.winsun.fruitmix.eventbus.OperationEvent;
import com.winsun.fruitmix.group.TestMqttActivity;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.group.data.viewmodel.GroupContentViewModel;
import com.winsun.fruitmix.group.presenter.GroupContentPresenter;
import com.winsun.fruitmix.group.setting.GroupSettingActivity;
import com.winsun.fruitmix.group.usecase.InputChatMenuUseCase;
import com.winsun.fruitmix.group.usecase.PlayAudioUseCaseImpl;
import com.winsun.fruitmix.group.view.customview.CustomArrowToggleButton;
import com.winsun.fruitmix.group.view.customview.InputChatLayout;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.mediaModule.NewPicChooseActivity;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mqtt.MqttUseCase;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.user.datasource.UserDataRepository;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class GroupContentActivity extends BaseActivity implements GroupContentView, View.OnClickListener
        , InputChatLayout.ChatLayoutOnClickListener, InputChatLayout.EditTextFocusChangeListener, InputChatLayout.SendTextChatListener
        , InputChatMenuUseCase, InputChatLayout.AddBtnOnClickListener, InputChatLayout.SendAudioChatListener {

    public static final int REQUEST_NEW_PIC_CHOOSE_ACTIVITY = 0x1001;

    public static final int REQUEST_CREATE_PING_ACTIVITY = 0x1002;

    public static final int REQUEST_PIN_CONTENT = 0x1003;

    public static final int REQUEST_GROUP_SETTING_ACTIVITY = 0x3100;

    private RecyclerView chatRecyclerView;

    private RecyclerView pingRecyclerView;

    private CustomArrowToggleButton toggleButton;

    private GroupContentPresenter groupContentPresenter;

    public static final String GROUP_UUID = "group_uuid";
    public static final String GROUP_NAME = "group_name";

    private String groupUUID;

    private InputChatLayout inputChatLayout;

    private FloatingActionButton mFabBtn;
    private FloatingActionButton mSendFileBtn;
    private ImageView mSendMediaBtn;

    private TextView mSendFileTextView;
    private TextView mSendMediaTextView;

    private ImageView mMaskLayout;

    private ActivityGroupContentBinding mActivityGroupContentBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityGroupContentBinding = DataBindingUtil.setContentView(this, R.layout.activity_group_content);

        initInputChatLayout(mActivityGroupContentBinding);

        groupUUID = getIntent().getStringExtra(GROUP_UUID);

        final GroupContentViewModel groupContentViewModel = new GroupContentViewModel();

        mActivityGroupContentBinding.setGroupContentViewModel(groupContentViewModel);

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        mActivityGroupContentBinding.setLoadingViewModel(loadingViewModel);

        ToolbarViewModel toolbarViewModel = new ToolbarViewModel();

        initToolbarViewModel(toolbarViewModel, mActivityGroupContentBinding);

        initPresenter(groupUUID, loadingViewModel, toolbarViewModel, groupContentViewModel);

        mActivityGroupContentBinding.setPingToggleListener(groupContentPresenter);

        chatRecyclerView = mActivityGroupContentBinding.chatRecyclerview;

        pingRecyclerView = mActivityGroupContentBinding.pingRecyclerview;

        initChatRecyclerView();

        initPingRecyclerView();

        groupContentPresenter.refreshView();

        toggleButton = mActivityGroupContentBinding.groupContentToolbar.toggle;

        LinearLayout toggleLayout = mActivityGroupContentBinding.groupContentToolbar.toggleLayout;

        toggleLayout.setOnClickListener(this);

        mActivityGroupContentBinding.chatContentLayout.setOnClickListener(this);

        mFabBtn = mActivityGroupContentBinding.fab;
        mSendFileBtn = mActivityGroupContentBinding.sendFileBtn;
        mSendMediaBtn = mActivityGroupContentBinding.sendMedia;

        mSendFileTextView = mActivityGroupContentBinding.sendFileHint;
        mSendMediaTextView = mActivityGroupContentBinding.sendMediaHint;

        mFabBtn.setOnClickListener(this);
        mSendFileBtn.setOnClickListener(this);
        mSendMediaBtn.setOnClickListener(this);

        mMaskLayout = mActivityGroupContentBinding.maskLayout;
        mMaskLayout.setOnClickListener(this);

    }

    private void initInputChatLayout(ActivityGroupContentBinding binding) {
        inputChatLayout = binding.inputChatLayout;

        inputChatLayout.setEditTextFocusChangeListener(this);
        inputChatLayout.setSendTextChatListener(this);
        inputChatLayout.setChatLayoutOnClickListener(this);
        inputChatLayout.setInputChatMenuUseCase(this);
        inputChatLayout.setAddBtnOnClickListener(this);
        inputChatLayout.setSendAudioChatListener(this);
    }

    private void initChatRecyclerView() {
        LinearLayoutManager chatLayoutManager = new LinearLayoutManager(this);

        chatRecyclerView.setLayoutManager(chatLayoutManager);

        chatRecyclerView.setItemAnimator(new DefaultItemAnimator());

        chatRecyclerView.setAdapter(groupContentPresenter.getGroupContentAdapter());

        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                if (bottom != oldBottom) {
                    groupContentPresenter.smoothToChatListEnd();
                }

            }
        });

    }

    private void initPingRecyclerView() {

        LinearLayoutManager pingLayoutManager = new LinearLayoutManager(this);
        pingLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        pingRecyclerView.setLayoutManager(pingLayoutManager);

        pingRecyclerView.setItemAnimator(new DefaultItemAnimator());

        pingRecyclerView.setAdapter(groupContentPresenter.getPingViewPageAdapter());
    }

    private void initToolbarViewModel(ToolbarViewModel toolbarViewModel, ActivityGroupContentBinding binding) {

        toolbarViewModel.setBaseView(this);

        toolbarViewModel.titleTextColorResID.set(ContextCompat.getColor(this, R.color.eighty_seven_percent_black));

        toolbarViewModel.showSelect.set(true);
        toolbarViewModel.selectTextResID.set(R.string.setting_text);
        toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(this, R.color.eighty_seven_percent_black));

        toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
            @Override
            public void onClick() {

                Intent intent = new Intent(GroupContentActivity.this, GroupSettingActivity.class);
                intent.putExtra(Util.KEY_GROUP_UUID, groupUUID);
                startActivityForResult(intent, REQUEST_GROUP_SETTING_ACTIVITY);

//                Util.startActivity(GroupContentActivity.this, TestMqttActivity.class);

            }
        });

        binding.setToolbarViewModel(toolbarViewModel);
    }

    private void initPresenter(String groupUUID, LoadingViewModel loadingViewModel, ToolbarViewModel toolbarViewModel, GroupContentViewModel groupContentViewModel) {
        GroupRepository groupRepository = InjectGroupDataSource.provideGroupRepository(this);

        UserDataRepository userDataRepository = InjectUser.provideRepository(this);

        groupContentPresenter = new GroupContentPresenter(this, groupUUID, userDataRepository, InjectSystemSettingDataSource.provideSystemSettingDataSource(this),
                groupRepository, groupContentViewModel, loadingViewModel, toolbarViewModel,
                InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this), PlayAudioUseCaseImpl.getInstance());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        groupContentPresenter.onDestroy();
    }

    @Override
    public void smoothToChatListPosition(int position) {

        chatRecyclerView.scrollToPosition(position);

    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.chat_content_layout:

                collapsePing();

                break;

            case R.id.toggle_layout:
                toggleButton.onclick();
                break;

            case R.id.fab:
                refreshFabState();
                break;

            case R.id.send_file_btn:

                sendFileChat();

                collapseFab();

                break;
            case R.id.send_media:

                sendPhotoChat();

                collapseFab();

                break;

            case R.id.mask_layout:

                collapseFab();

                break;
        }

    }

    private boolean sMenuUnfolding = false;

    private void refreshFabState() {
        if (sMenuUnfolding) {
            sMenuUnfolding = false;
            collapseFabAnimation();
        } else {
            sMenuUnfolding = true;
            extendFabAnimation();
        }
    }

    private void collapseFab() {
        if (sMenuUnfolding) {
            sMenuUnfolding = false;
            collapseFabAnimation();
        }
    }

    private void collapseFabAnimation() {

        mMaskLayout.setVisibility(View.GONE);

        new AnimatorBuilder(getContext(), R.animator.fab_remote_restore, mFabBtn).startAnimator();

        mSendMediaTextView.setVisibility(View.GONE);
        mSendFileTextView.setVisibility(View.GONE);

        new AnimatorBuilder(getContext(), R.animator.first_btn_above_fab_translation_restore, mSendMediaBtn).addAdapter(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mSendMediaBtn.setVisibility(View.GONE);

            }
        }).startAnimator();

        new AnimatorBuilder(getContext(), R.animator.second_btn_above_fab_translation_restore, mSendFileBtn).addAdapter(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mSendFileBtn.setVisibility(View.GONE);

            }
        }).startAnimator();


    }

    private void extendFabAnimation() {

        mMaskLayout.setVisibility(View.VISIBLE);

        new AnimatorBuilder(getContext(), R.animator.fab_remote, mFabBtn).startAnimator();

        mSendMediaBtn.setVisibility(View.VISIBLE);

        mSendFileBtn.setVisibility(View.VISIBLE);

        new AnimatorBuilder(getContext(), R.animator.first_btn_above_fab_translation, mSendMediaBtn).addAdapter(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mSendMediaTextView.setVisibility(View.VISIBLE);

            }
        }).startAnimator();

        new AnimatorBuilder(getContext(), R.animator.second_btn_above_fab_translation, mSendFileBtn).addAdapter(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                mSendFileTextView.setVisibility(View.VISIBLE);

            }
        }).startAnimator();


    }

    @Override
    public void onBackPressed() {

        if (!inputChatLayout.handleOnBackPressed())
            super.onBackPressed();

    }

    private void collapsePing() {
        if (toggleButton.isExpandPing())
            toggleButton.collapsePing();
    }

    @Override
    public void onFocusChanged(boolean hasFocus) {
        collapsePing();

    }

    @Override
    public boolean onSendTextChat(String text) {

        groupContentPresenter.sendTxt(text);

        return true;
    }

    @Override
    public void onChatLayoutClick() {

        collapsePing();
    }

    @Override
    public void sendPhotoChat() {

        Intent intent = new Intent(this, NewPicChooseActivity.class);
        intent.putExtra(Util.KEY_GROUP_UUID, groupUUID);
        intent.putExtra(NewPicChooseActivity.KEY_SHOW_MEDIA, true);

        startActivityForResult(intent, REQUEST_NEW_PIC_CHOOSE_ACTIVITY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_PIC_CHOOSE_ACTIVITY && resultCode == RESULT_OK) {

//            groupContentPresenter.refreshView();

        } else if (requestCode == REQUEST_CREATE_PING_ACTIVITY && resultCode == RESULT_OK)
            groupContentPresenter.refreshView();
        else if (requestCode == REQUEST_PIN_CONTENT && resultCode == RESULT_OK) {
            groupContentPresenter.refreshPin();
        } else if (requestCode == REQUEST_GROUP_SETTING_ACTIVITY) {

            setResult(GroupSettingActivity.RESULT_MODIFY_GROUP_INFO);

            if (resultCode == GroupSettingActivity.RESULT_MODIFY_GROUP_INFO) {

                groupContentPresenter.refreshTitle();

            } else if (resultCode == GroupSettingActivity.RESULT_DELETE_OR_QUIT_GROUP) {

                finishView();

            }

        }

    }


    @Override
    public void sendFileChat() {

//        Intent intent = new Intent(this, AddFriendActivity.class);
//        intent.putExtra(Util.KEY_GROUP_UUID, groupUUID);
//        startActivity(intent);

        Intent intent = new Intent(this, NewPicChooseActivity.class);
        intent.putExtra(Util.KEY_GROUP_UUID, groupUUID);
        intent.putExtra(NewPicChooseActivity.KEY_SHOW_MEDIA, false);

        startActivityForResult(intent, REQUEST_NEW_PIC_CHOOSE_ACTIVITY);

    }

    @Override
    public void onAddBtnClick() {

        Util.hideSoftInput(this);

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public View getToolbar() {
        return mActivityGroupContentBinding.groupContentToolbar.toolbar;
    }

    @Override
    public void showCreatePing() {

        Intent intent = new Intent(this, OperatePinActivity.class);
        intent.putExtra(Util.KEY_GROUP_UUID, groupUUID);
        startActivityForResult(intent, REQUEST_CREATE_PING_ACTIVITY);

    }

    @Override
    public void showPinContent(String groupUUID, String pinUUID) {

        Intent intent = new Intent(this, PinContentActivity.class);
        intent.putExtra(PinContentActivity.KEY_GROUP_UUID, groupUUID);
        intent.putExtra(PinContentActivity.KEY_PIN_UUID, pinUUID);
        startActivityForResult(intent, REQUEST_PIN_CONTENT);

    }

    @Override
    public boolean onSendAudioChat(String filePath, long audioRecordTime) {

        groupContentPresenter.sendAudio(filePath, audioRecordTime);

        return true;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void handleStickyOperationEvent(OperationEvent operationEvent) {

        String action = operationEvent.getAction();

        Log.i(TAG, "handleOperationEvent: action:" + action);

        if (action.equals(MqttUseCase.MQTT_MESSAGE)) {

            Log.d(TAG, "handleStickyOperationEvent: mqtt message retrieved");

            EventBus.getDefault().removeStickyEvent(operationEvent);

            groupContentPresenter.handleMqttMessage((MqttMessageEvent) operationEvent);

        }

    }

}
