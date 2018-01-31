package com.winsun.fruitmix.contact;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.BaseToolbarActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.contact.data.InjectContactDataSource;
import com.winsun.fruitmix.databinding.ActivityContactListBinding;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.group.view.CreateGroupActivity;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.system.setting.InjectSystemSettingDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.InjectUser;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class ContactListActivity extends BaseToolbarActivity implements ContactListView {

    private ActivityContactListBinding mActivityContactListBinding;

    private RecyclerView mRecyclerView;

    private ContactListPresenter mContactListPresenter;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        mRecyclerView = mActivityContactListBinding.contactRecyclerView;

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        LoadingViewModel loadingViewModel = new LoadingViewModel();

        mActivityContactListBinding.setLoadingViewModel(loadingViewModel);

        NoContentViewModel noContentViewModel = new NoContentViewModel();

        noContentViewModel.setNoContentImgResId(R.drawable.no_file);
        noContentViewModel.setNoContentText("没有联系人");

        mActivityContactListBinding.setNoContentViewModel(noContentViewModel);

        User currentUser = InjectUser.provideRepository(mContext).
                getUserByUUID(InjectSystemSettingDataSource.provideSystemSettingDataSource(mContext).getCurrentLoginUserUUID());

        mContactListPresenter = new ContactListPresenter(InjectContactDataSource.provideInstance(this),
                InjectHttp.provideImageGifLoaderInstance(this).getImageLoader(this),
                loadingViewModel, noContentViewModel, this, InjectGroupDataSource.provideGroupRepository(this),
                currentUser);

        toolbarViewModel.showSelect.set(true);

        toolbarViewModel.selectTextResID.set(R.string.finish_text);

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
        return "选择联系人";
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

            toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
                @Override
                public void onClick() {

                    mContactListPresenter.createGroup();

                }
            });

        } else {

            toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(mContext, R.color.twenty_six_percent_black));

            toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
                @Override
                public void onClick() {

                }
            });

        }


    }
}
