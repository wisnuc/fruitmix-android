package com.winsun.fruitmix.dialog;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.BottomSheetDialogItemBinding;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.recyclerview.BindingViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/18.
 */

public class BottomMenuListDialogFactory extends BaseBottomMenuDialogFactory {

    public BottomMenuListDialogFactory(@NotNull List<? extends BottomMenuItem> bottomMenuItems) {
        super(bottomMenuItems);
    }

    @NotNull
    @Override
    public View createBottomSheetView(@NotNull Context context, @NotNull List<? extends BottomMenuItem> bottomMenuItems) {

        View bottomSheetView = View.inflate(context, R.layout.list_dialog_layout, null);

        RecyclerView bottomSheetRecyclerView = bottomSheetView.findViewById(R.id.recyclerview);

        BottomSheetRecyclerViewAdapter bottomSheetRecyclerViewAdapter = new BottomSheetRecyclerViewAdapter(context);

        bottomSheetRecyclerView.setAdapter(bottomSheetRecyclerViewAdapter);

        bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        bottomSheetRecyclerViewAdapter.setBottomMenuItems(bottomMenuItems);
        bottomSheetRecyclerViewAdapter.notifyDataSetChanged();

        return bottomSheetView;

    }

    private class BottomSheetRecyclerViewAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<BottomMenuItem> bottomMenuItems;
        private Context context;

        BottomSheetRecyclerViewAdapter(Context context) {
            bottomMenuItems = new ArrayList<>();
            this.context = context;
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            BottomSheetDialogItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_dialog_item, parent, false);

            return new BindingViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {
            holder.getViewDataBinding().setVariable(BR.bottomMenuItem, bottomMenuItems.get(position));
            holder.getViewDataBinding().executePendingBindings();
        }

        @Override
        public int getItemCount() {
            return bottomMenuItems.size();
        }

        void setBottomMenuItems(List<? extends BottomMenuItem> bottomMenuItems) {
            this.bottomMenuItems.clear();
            this.bottomMenuItems.addAll(bottomMenuItems);
        }
    }

}
