package com.winsun.fruitmix.group.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityGroupContentBinding;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.source.FakeGroupDataSource;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.viewmodel.GroupContentViewModel;
import com.winsun.fruitmix.group.presenter.GroupContentPresenter;
import com.winsun.fruitmix.group.view.customview.CustomArrowToggleButton;
import com.winsun.fruitmix.interfaces.BaseView;
import com.winsun.fruitmix.logged.in.user.InjectLoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class GroupContentActivity extends BaseActivity implements GroupContentView {

    private RecyclerView chatRecyclerView;

    private RecyclerView pingRecyclerView;

    private EditText editText;

    private GroupContentPresenter groupContentPresenter;

    public static final String GROUP_UUID = "group_uuid";
    public static final String GROUP_NAME = "group_name";

    private String inputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityGroupContentBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_group_content);

        chatRecyclerView = binding.chatRecyclerview;

        pingRecyclerView = binding.pingRecyclerview;

        String groupUUID = getIntent().getStringExtra(GROUP_UUID);

        final GroupContentViewModel groupContentViewModel = new GroupContentViewModel();

        binding.setGroupContentViewModel(groupContentViewModel);

        GroupRepository groupRepository = GroupRepository.getInstance(FakeGroupDataSource.getInstance());

        LoggedInUserDataSource loggedInUserDataSource = InjectLoggedInUser.provideLoggedInUserRepository(this);

        groupContentPresenter = new GroupContentPresenter(this, groupUUID, loggedInUserDataSource, groupRepository, groupContentViewModel);

        final ToolbarViewModel toolbarViewModel = new ToolbarViewModel();
        toolbarViewModel.setBaseView(this);

        String groupName = getIntent().getStringExtra(GROUP_NAME);

        toolbarViewModel.titleText.set(groupName);

        binding.setToolbarViewModel(toolbarViewModel);

        binding.setPingToggleListener(groupContentPresenter);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setItemAnimator(new DefaultItemAnimator());

        chatRecyclerView.setAdapter(groupContentPresenter.getGroupContentAdapter());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        pingRecyclerView.setLayoutManager(linearLayoutManager);

        pingRecyclerView.setItemAnimator(new DefaultItemAnimator());

        pingRecyclerView.setAdapter(groupContentPresenter.getPingViewPageAdapter());

        groupContentPresenter.refreshGroup();

        editText = binding.editText;
        editText.clearFocus();

        final CustomArrowToggleButton toggleButton = binding.groupContentToolbar.toggle;

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                toggleButton.onclick();

            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                inputText = editText.getText().toString();

                if (inputText.isEmpty()) {
                    groupContentViewModel.showSendBtn.set(false);
                } else
                    groupContentViewModel.showSendBtn.set(true);

            }
        });

        Button button = binding.sendButton;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                groupContentPresenter.sendTxt(inputText);

            }
        });

        LinearLayout toggleLayout = binding.groupContentToolbar.toggleLayout;
        toggleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toggleButton.onclick();

            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        groupContentPresenter.onDestroy();
    }

    @Override
    public void smoothToChatListPosition(int position) {
        chatRecyclerView.smoothScrollToPosition(position);
    }

    @Override
    public void clearEditText() {
        editText.getText().clear();
    }
}
