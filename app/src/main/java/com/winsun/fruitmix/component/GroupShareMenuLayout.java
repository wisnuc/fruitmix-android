package com.winsun.fruitmix.component;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.databinding.GroupShareMenuItemLayoutBinding;
import com.winsun.fruitmix.databinding.GroupShareMenuLayoutBinding;
import com.winsun.fruitmix.databinding.NewLoadingLayoutBinding;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.recyclerview.BaseRecyclerViewAdapter;
import com.winsun.fruitmix.recyclerview.BindingViewHolder;
import com.winsun.fruitmix.util.FilterRule;
import com.winsun.fruitmix.util.ItemFilterKt;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;

import java.util.List;

/**
 * Created by Administrator on 2018/3/9.
 */

public class GroupShareMenuLayout {

    private GroupRepository mGroupRepository;

    private LoadingViewModel mLoadingViewModel;

    private GroupShareMenuRecyclerViewAdapter mGroupShareMenuRecyclerViewAdapter;

    private GroupShareMenuItemOnClickListener mGroupShareMenuItemOnClickListener;

    private View mView;

    public GroupShareMenuLayout(Context context, ViewGroup root, GroupShareMenuItemOnClickListener groupShareMenuItemOnClickListener) {

        GroupShareMenuLayoutBinding binding = GroupShareMenuLayoutBinding
                .inflate(LayoutInflater.from(context), root, false);

        mLoadingViewModel = new LoadingViewModel(context,android.R.color.transparent);

        binding.setLoadingViewModel(mLoadingViewModel);

        RecyclerView groupShareMenuRecyclerView = binding.groupMenuRecyclerView;

        groupShareMenuRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        groupShareMenuRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mGroupShareMenuRecyclerViewAdapter = new GroupShareMenuRecyclerViewAdapter();
        groupShareMenuRecyclerView.setAdapter(mGroupShareMenuRecyclerViewAdapter);

        mGroupRepository = InjectGroupDataSource.provideGroupRepository(context);

        mGroupShareMenuItemOnClickListener = groupShareMenuItemOnClickListener;

        mView = binding.getRoot();

    }

    public View getView() {
        return mView;
    }

    public void initView() {

        mGroupRepository.getGroupList(new BaseLoadDataCallback<PrivateGroup>() {
            @Override
            public void onSucceed(List<PrivateGroup> data, OperationResult operationResult) {

                mLoadingViewModel.showLoading.set(false);

                List<PrivateGroup> result =
                        ItemFilterKt.filterItem(data, new FilterRule<PrivateGroup>() {
                            @Override
                            public boolean isFiltered(PrivateGroup item) {
                                return item.isStationOnline();
                            }
                        });

                mGroupShareMenuRecyclerViewAdapter.setItemList(result);
                mGroupShareMenuRecyclerViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                mLoadingViewModel.showLoading.set(false);

            }
        });

    }

    private class GroupShareMenuRecyclerViewAdapter extends BaseRecyclerViewAdapter<GroupShareMenuRecyclerViewHolder, PrivateGroup> {


        @Override
        public GroupShareMenuRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            GroupShareMenuItemLayoutBinding binding =
                    GroupShareMenuItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            return new GroupShareMenuRecyclerViewHolder(binding);

        }

        @Override
        public void onBindViewHolder(GroupShareMenuRecyclerViewHolder holder, int position) {

            holder.refreshView(mItemList.get(position));

        }


    }


    private class GroupShareMenuRecyclerViewHolder extends BindingViewHolder {

        private GroupShareMenuItemLayoutBinding mGroupShareMenuItemLayoutBinding;

        GroupShareMenuRecyclerViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            mGroupShareMenuItemLayoutBinding = (GroupShareMenuItemLayoutBinding) viewDataBinding;
        }

        void refreshView(final PrivateGroup group) {

            mGroupShareMenuItemLayoutBinding.setGroup(group);

            Util.fillGroupUserAvatar(group, mGroupShareMenuItemLayoutBinding.groupUserAvatar);

            mGroupShareMenuItemLayoutBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mGroupShareMenuItemOnClickListener.onClick(group);
                }
            });

        }

    }

    public interface GroupShareMenuItemOnClickListener {

        void onClick(PrivateGroup item);

    }


}
