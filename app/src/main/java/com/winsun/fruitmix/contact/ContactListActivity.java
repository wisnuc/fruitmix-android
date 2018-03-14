package com.winsun.fruitmix.contact;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.BaseToolbarActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.contact.data.InjectContactDataSource;
import com.winsun.fruitmix.databinding.ActivityContactListBinding;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import static com.winsun.fruitmix.group.view.GroupListPage.CREATE_GROUP_REQUEST_CODE;

public class ContactListActivity extends BaseToolbarActivity implements ContactListView {

    private ActivityContactListBinding mActivityContactListBinding;

    private RecyclerView mRecyclerView;

    private ContactListPresenter mContactListPresenter;

    private Context mContext;

    public static final int ADD_USER = 0;
    public static final int DELETE_USER = 1;
    public static final int CREATE_GROUP = 2;

    public static final String KEY_PURPOSE = "key_purpose";

    private int purpose = -1;

    public static void startForCreateGroup(Activity activity) {

        Intent intent = new Intent(activity, ContactListActivity.class);
        intent.putExtra(KEY_PURPOSE, CREATE_GROUP);

        activity.startActivityForResult(intent, CREATE_GROUP_REQUEST_CODE);

    }

    public static final int MODIFY_GROUP_USERS_REQUEST_CODE = 0x1002;

    public static void start(Activity activity, int purpose, String groupUUID) {

        Intent intent = new Intent(activity, ContactListActivity.class);
        intent.putExtra(KEY_PURPOSE, purpose);
        intent.putExtra(Util.KEY_GROUP_UUID, groupUUID);

        activity.startActivityForResult(intent, MODIFY_GROUP_USERS_REQUEST_CODE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        purpose = getIntent().getIntExtra(KEY_PURPOSE, -1);

        super.onCreate(savedInstanceState);

        mContext = this;

        String groupUUID;
        if (purpose != CREATE_GROUP) {
            groupUUID = getIntent().getStringExtra(Util.KEY_GROUP_UUID);
        } else
            groupUUID = "";

        mRecyclerView = mActivityContactListBinding.contactRecyclerView;

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        LoadingViewModel loadingViewModel = new LoadingViewModel(this);

        mActivityContactListBinding.setLoadingViewModel(loadingViewModel);

        NoContentViewModel noContentViewModel = new NoContentViewModel();

        noContentViewModel.setNoContentImgResId(R.drawable.no_file);
        noContentViewModel.setNoContentText(getString(R.string.no_contact));

        mActivityContactListBinding.setNoContentViewModel(noContentViewModel);

        User currentUser = InjectUser.provideRepository(mContext).
                getUserByUUID(InjectSystemSettingDataSource.provideSystemSettingDataSource(mContext).getCurrentLoginUserUUID());

        mContactListPresenter = new ContactListPresenter(InjectContactDataSource.provideInstance(this),
                InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this),
                loadingViewModel, noContentViewModel, this, InjectGroupDataSource.provideGroupRepository(this),
                currentUser, InjectSystemSettingDataSource.provideSystemSettingDataSource(this), purpose, groupUUID);

        toolbarViewModel.showSelect.set(true);

        if (purpose == CREATE_GROUP)
            toolbarViewModel.selectTextResID.set(R.string.finish_text);
        else if (purpose == ADD_USER)
            toolbarViewModel.selectTextResID.set(R.string.add_text);
        else if (purpose == DELETE_USER)
            toolbarViewModel.selectTextResID.set(R.string.delete_text);

        onSelectItemChanged(0);

        mRecyclerView.setAdapter(mContactListPresenter.getContactRecyclerViewAdapter());

        mContactListPresenter.refreshView();

    }

    @Override
    protected View generateContent(ViewGroup root) {

        mActivityContactListBinding = ActivityContactListBinding
                .inflate(LayoutInflater.from(this), root, false);

        return mActivityContactListBinding.getRoot();
    }

    @Override
    protected String getToolbarTitle() {

        if (purpose == CREATE_GROUP)
            return getString(R.string.create_group);
        else
            return getString(R.string.group_setting);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;

        mContactListPresenter.onDestroy();

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onSelectItemChanged(int selectedItemCount) {

        if (selectedItemCount > 0) {

            toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(mContext, R.color.eighty_seven_percent_black));

            toolbarViewModel.selectTextEnable.set(true);

            toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
                @Override
                public void onClick() {

                    switch (purpose) {
                        case CREATE_GROUP:
                            mContactListPresenter.createGroup();
                            break;
                        case ADD_USER:
                            mContactListPresenter.addUser();
                            break;
                        case DELETE_USER:
                            mContactListPresenter.deleteUser();
                            break;

                        default:
                            Log.e(TAG, "onClick: not create group,add user or delete user,something wrong");

                    }

                }
            });

        } else {

            toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(mContext, R.color.twenty_six_percent_black));

            toolbarViewModel.selectTextEnable.set(false);

        }


    }
}
