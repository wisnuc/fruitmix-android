package com.winsun.fruitmix.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.BottomSheetDialogItemBinding;
import com.winsun.fruitmix.model.BottomMenuItem;
import com.winsun.fruitmix.viewholder.BindingViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/11/18.
 */

public class BottomMenuDialogFactory implements DialogFactory {

    private List<BottomMenuItem> bottomMenuItems;

    public BottomMenuDialogFactory(List<BottomMenuItem> bottomMenuItems) {
        this.bottomMenuItems = bottomMenuItems;
    }

    @Override
    public Dialog createDialog(Context context) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        View bottomSheetView = createBottomSheetView(context, bottomMenuItems);

        bottomSheetDialog.setContentView(bottomSheetView);

        View parent = (View) bottomSheetView.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        return bottomSheetDialog;
    }

    private View createBottomSheetView(Context context, List<BottomMenuItem> bottomMenuItems) {

        View bottomSheetView = View.inflate(context, R.layout.list_dialog_layout, null);

        RecyclerView bottomSheetRecyclerView = (RecyclerView) bottomSheetView.findViewById(R.id.recyclerview);

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

        void setBottomMenuItems(List<BottomMenuItem> bottomMenuItems) {
            this.bottomMenuItems.clear();
            this.bottomMenuItems.addAll(bottomMenuItems);
        }
    }

}
