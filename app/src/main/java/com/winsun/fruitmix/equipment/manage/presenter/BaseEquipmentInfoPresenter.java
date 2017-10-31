package com.winsun.fruitmix.equipment.manage.presenter;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.databinding.EquipmentInfoDivideBinding;
import com.winsun.fruitmix.databinding.EquipmentInfoItemBinding;
import com.winsun.fruitmix.equipment.manage.data.EquipmentInfoDataSource;
import com.winsun.fruitmix.equipment.manage.viewmodel.EquipmentInfoItem;
import com.winsun.fruitmix.equipment.manage.viewmodel.EquipmentInfoViewModel;
import com.winsun.fruitmix.equipment.manage.view.EquipmentInfoView;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/16.
 */

public abstract class BaseEquipmentInfoPresenter {

    public static final String TAG = BaseEquipmentInfoPresenter.class.getSimpleName();

    private EquipmentInfoRecyclerViewAdapter mEquipmentInfoRecyclerViewAdapter;

    EquipmentInfoView equipmentInfoView;
    private LoadingViewModel loadingViewModel;
    private NoContentViewModel noContentViewModel;

    EquipmentInfoDataSource equipmentInfoDataSource;

    public BaseEquipmentInfoPresenter(EquipmentInfoDataSource equipmentInfoDataSource,EquipmentInfoView equipmentInfoView, LoadingViewModel loadingViewModel, NoContentViewModel noContentViewModel) {

        mEquipmentInfoRecyclerViewAdapter = new EquipmentInfoRecyclerViewAdapter();

        this.equipmentInfoDataSource = equipmentInfoDataSource;
        this.equipmentInfoView = equipmentInfoView;
        this.loadingViewModel = loadingViewModel;
        this.noContentViewModel = noContentViewModel;

    }

    public void onDestroy(){

        equipmentInfoView = null;

    }

    protected abstract void getEquipmentInfoItem(BaseLoadDataCallback<EquipmentInfoItem> callback);

    public void refreshEquipmentInfoItem() {

        getEquipmentInfoItem(new BaseLoadDataCallback<EquipmentInfoItem>() {
            @Override
            public void onSucceed(List<EquipmentInfoItem> data, OperationResult operationResult) {

                loadingViewModel.showLoading.set(false);
                noContentViewModel.showNoContent.set(false);
                equipmentInfoView.showEquipmentInfoRecyclerView();

                mEquipmentInfoRecyclerViewAdapter.setEquipmentInfoItems(data);
                mEquipmentInfoRecyclerViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFail(OperationResult operationResult) {

                if(equipmentInfoView == null)
                    return;

                loadingViewModel.showLoading.set(false);
                noContentViewModel.showNoContent.set(true);
                equipmentInfoView.dismissEquipmentInfoRecyclerView();
            }
        });


    }

    public EquipmentInfoRecyclerViewAdapter getmEquipmentInfoRecyclerViewAdapter() {
        return mEquipmentInfoRecyclerViewAdapter;
    }

    private class EquipmentInfoRecyclerViewAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<EquipmentInfoItem> mEquipmentInfoItems;

        public EquipmentInfoRecyclerViewAdapter() {
            mEquipmentInfoItems = new ArrayList<>();
        }

        public void setEquipmentInfoItems(List<EquipmentInfoItem> equipmentInfoItems) {
            mEquipmentInfoItems.clear();
            mEquipmentInfoItems.addAll(equipmentInfoItems);
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            BindingViewHolder bindingViewHolder = null;

            switch (viewType) {
                case EquipmentInfoItem.TYPE_VIEW_MODEL:

                    EquipmentInfoItemBinding equipmentInfoItemBinding = EquipmentInfoItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);

                    bindingViewHolder = new EquipmentInfoViewHolder(equipmentInfoItemBinding);
                    break;
                case EquipmentInfoItem.TYPE_DIVIDE:

                    EquipmentInfoDivideBinding equipmentInfoDivideBinding = EquipmentInfoDivideBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);

                    bindingViewHolder = new EquipmentDivideViewHolder(equipmentInfoDivideBinding);

                    break;
                default:
                    Log.d(TAG, "onCreateViewHolder: should not enter default case");
            }

            return bindingViewHolder;
        }

        @Override
        public void onBindViewHolder(BindingViewHolder bindingViewHolder, int position) {

            EquipmentInfoItem equipmentInfoItem = mEquipmentInfoItems.get(position);

            if (equipmentInfoItem.getType() == EquipmentInfoItem.TYPE_VIEW_MODEL) {

                ViewDataBinding binding = bindingViewHolder.getViewDataBinding();

                EquipmentInfoViewModel equipmentInfoViewModel = (EquipmentInfoViewModel) equipmentInfoItem;

                binding.setVariable(BR.equipmentInfoViewModel, equipmentInfoViewModel);

                binding.executePendingBindings();

                EquipmentInfoViewHolder equipmentInfoViewHolder = (EquipmentInfoViewHolder) bindingViewHolder;

                equipmentInfoViewHolder.refreshView(equipmentInfoViewModel);

            }

        }

        @Override
        public int getItemCount() {
            return mEquipmentInfoItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mEquipmentInfoItems.get(position).getType();
        }
    }

    private class EquipmentInfoViewHolder extends BindingViewHolder {

        private ImageView equipmentInfoIcon;

        EquipmentInfoViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);

            EquipmentInfoItemBinding binding = (EquipmentInfoItemBinding) viewDataBinding;
            equipmentInfoIcon = binding.infoIcon;
        }

        public void refreshView(EquipmentInfoViewModel equipmentInfoViewModel) {

            if (equipmentInfoViewModel.getIconResID() == 0) {
                equipmentInfoIcon.setVisibility(View.INVISIBLE);
            } else {
                equipmentInfoIcon.setVisibility(View.VISIBLE);
                equipmentInfoIcon.setImageResource(equipmentInfoViewModel.getIconResID());
            }

        }

    }

    private class EquipmentDivideViewHolder extends BindingViewHolder {

        EquipmentDivideViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

    }


}
