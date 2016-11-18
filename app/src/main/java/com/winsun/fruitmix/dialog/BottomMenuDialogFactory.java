package com.winsun.fruitmix.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.fileModule.model.BottomMenuItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/11/18.
 */

public class BottomMenuDialogFactory extends DialogFactory {

    private List<BottomMenuItem> bottomMenuItems;

    public BottomMenuDialogFactory(List<BottomMenuItem> bottomMenuItems) {
        this.bottomMenuItems = bottomMenuItems;
    }

    @Override
    public Dialog createDialog(Context context) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        View bottomSheetView = createBottomSheetView(context,bottomMenuItems);

        bottomSheetDialog.setContentView(bottomSheetView);

        View parent = (View) bottomSheetView.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        return bottomSheetDialog;
    }

    private View createBottomSheetView(Context context, List<BottomMenuItem> bottomMenuItems) {

        View bottomSheetView = View.inflate(context, R.layout.bottom_sheet_dialog_layout, null);

        RecyclerView bottomSheetRecyclerView = (RecyclerView) bottomSheetView.findViewById(R.id.bottom_sheet_recyclerview);

        BottomSheetRecyclerViewAdapter bottomSheetRecyclerViewAdapter = new BottomSheetRecyclerViewAdapter(context);

        bottomSheetRecyclerView.setAdapter(bottomSheetRecyclerViewAdapter);

        bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        bottomSheetRecyclerViewAdapter.setBottomMenuItems(bottomMenuItems);
        bottomSheetRecyclerViewAdapter.notifyDataSetChanged();

        return bottomSheetView;
    }

    private class BottomSheetRecyclerViewAdapter extends RecyclerView.Adapter<BottomSheetRecyclerViewViewHolder> {

        private List<BottomMenuItem> bottomMenuItems;
        private Context context;

        BottomSheetRecyclerViewAdapter(Context context) {
            bottomMenuItems = new ArrayList<>();
            this.context = context;
        }

        @Override
        public BottomSheetRecyclerViewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog_item, parent, false);

            return new BottomMenuDialogFactory.BottomSheetRecyclerViewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BottomSheetRecyclerViewViewHolder holder, int position) {
            holder.refreshView(bottomMenuItems.get(position));
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


    class BottomSheetRecyclerViewViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_text)
        TextView itemTextView;
        @BindView(R.id.item_layout)
        RelativeLayout itemLayout;

        BottomSheetRecyclerViewViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void refreshView(final BottomMenuItem bottomMenuItem) {
            itemTextView.setText(bottomMenuItem.getText());

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomMenuItem.handleOnClickEvent();
                }
            });
        }
    }
}
