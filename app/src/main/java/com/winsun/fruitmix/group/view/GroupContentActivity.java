package com.winsun.fruitmix.group.view;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityGroupContentBinding;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.group.data.viewmodel.GroupContentViewModel;
import com.winsun.fruitmix.group.presenter.GroupContentPresenter;
import com.winsun.fruitmix.group.presenter.InputChatMenuUseCase;
import com.winsun.fruitmix.group.view.customview.CustomArrowToggleButton;
import com.winsun.fruitmix.group.view.customview.InputChatLayout;
import com.winsun.fruitmix.http.ImageGifLoaderInstance;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.mediaModule.NewPicChooseActivity;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class GroupContentActivity extends BaseActivity implements GroupContentView, View.OnClickListener
        , InputChatLayout.ChatLayoutOnClickListener, InputChatLayout.EditTextFocusChangeListener, InputChatLayout.SendTextChatListener
        , InputChatMenuUseCase, InputChatLayout.AddBtnOnClickListener {

    public static final int REQUEST_NEW_PIC_CHOOSE_ACTIVITY = 0x1001;

    public static final int REQUEST_CREATE_PING_ACTIVITY = 0x1002;

    public static final int REQUEST_PIN_CONTENT = 0x1003;

    private RecyclerView chatRecyclerView;

    private RecyclerView pingRecyclerView;

    private CustomArrowToggleButton toggleButton;

    private GroupContentPresenter groupContentPresenter;

    public static final String GROUP_UUID = "group_uuid";
    public static final String GROUP_NAME = "group_name";

    private String groupUUID;

    private InputChatLayout inputChatLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityGroupContentBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_group_content);

        initInputChatLayout(binding);

        groupUUID = getIntent().getStringExtra(GROUP_UUID);

        final GroupContentViewModel groupContentViewModel = new GroupContentViewModel();

        binding.setGroupContentViewModel(groupContentViewModel);

        initPresenter(groupUUID, groupContentViewModel);

        initToolbarViewModel(binding);

        binding.setPingToggleListener(groupContentPresenter);

        chatRecyclerView = binding.chatRecyclerview;

        pingRecyclerView = binding.pingRecyclerview;

        initChatRecyclerView();

        initPingRecyclerView();

        groupContentPresenter.refreshView();

        toggleButton = binding.groupContentToolbar.toggle;

        LinearLayout toggleLayout = binding.groupContentToolbar.toggleLayout;

        toggleLayout.setOnClickListener(this);

        binding.chatContentLayout.setOnClickListener(this);

    }

    private void initInputChatLayout(ActivityGroupContentBinding binding) {
        inputChatLayout = binding.inputChatLayout;

        inputChatLayout.setEditTextFocusChangeListener(this);
        inputChatLayout.setSendTextChatListener(this);
        inputChatLayout.setChatLayoutOnClickListener(this);
        inputChatLayout.setInputChatMenuUseCase(this);
        inputChatLayout.setAddBtnOnClickListener(this);
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

    private void initToolbarViewModel(ActivityGroupContentBinding binding) {
        final ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.setBaseView(this);

        String groupName = getIntent().getStringExtra(GROUP_NAME);

        toolbarViewModel.titleText.set(groupName);

        binding.setToolbarViewModel(toolbarViewModel);
    }

    private void initPresenter(String groupUUID, GroupContentViewModel groupContentViewModel) {
        GroupRepository groupRepository = InjectGroupDataSource.provideGroupRepository();

        LoggedInUserDataSource loggedInUserDataSource = InjectLoggedInUser.provideLoggedInUserRepository(this);

        groupContentPresenter = new GroupContentPresenter(this, groupUUID, loggedInUserDataSource,
                groupRepository, groupContentViewModel, ImageGifLoaderInstance.getInstance().getImageLoader(this));
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

        }

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

        if (requestCode == REQUEST_NEW_PIC_CHOOSE_ACTIVITY && resultCode == RESULT_OK)
            groupContentPresenter.refreshView();
        else if (requestCode == REQUEST_CREATE_PING_ACTIVITY && resultCode == RESULT_OK)
            groupContentPresenter.refreshView();
        else if (requestCode == REQUEST_PIN_CONTENT && resultCode == RESULT_OK) {
            groupContentPresenter.refreshPin();
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
}
