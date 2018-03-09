package com.winsun.fruitmix.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.component.GroupShareMenuLayout;
import com.winsun.fruitmix.databinding.GroupShareMenuLayoutBinding;
import com.winsun.fruitmix.group.data.model.PrivateGroup;

/**
 * Created by Administrator on 2017/3/29.
 */

public class ShareMenuBottomDialogFactory implements DialogFactory, View.OnClickListener {

    private AbstractCommand mShareInAppCommand;
    private AbstractCommand mShareToOtherAppCommand;

    private Dialog dialog;

    public ShareMenuBottomDialogFactory(AbstractCommand mShareInAppCommand, AbstractCommand mShareToOtherAppCommand) {
        this.mShareInAppCommand = mShareInAppCommand;
        this.mShareToOtherAppCommand = mShareToOtherAppCommand;
    }

    private GroupShareMenuLayout.GroupShareMenuItemOnClickListener mGroupShareMenuItemOnClickListener;

    public ShareMenuBottomDialogFactory(AbstractCommand shareInAppCommand, AbstractCommand shareToOtherAppCommand,
                                        GroupShareMenuLayout.GroupShareMenuItemOnClickListener groupShareMenuItemOnClickListener) {
        mShareInAppCommand = shareInAppCommand;
        mShareToOtherAppCommand = shareToOtherAppCommand;
        mGroupShareMenuItemOnClickListener = groupShareMenuItemOnClickListener;
    }

    @Override
    public Dialog createDialog(Context context) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        View view = createView(context);

        bottomSheetDialog.setContentView(view);

        View parent = (View) view.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        dialog = bottomSheetDialog;

        return bottomSheetDialog;
    }

    private View createView(Context context) {

        View view = View.inflate(context, R.layout.share_menu_bottom_dialog_layout, null);

        ViewGroup shareInAppLayout = (ViewGroup) view.findViewById(R.id.share_in_app_layout);
        ViewGroup shareToOtherLayout = (ViewGroup) view.findViewById(R.id.share_to_other_app_layout);

        if (mGroupShareMenuItemOnClickListener != null) {

            ViewGroup groupShareMenuContainer = view.findViewById(R.id.group_share_menu_container);

            GroupShareMenuLayout groupShareMenuLayout = new GroupShareMenuLayout(context, groupShareMenuContainer,
                    new GroupShareMenuLayout.GroupShareMenuItemOnClickListener() {
                        @Override
                        public void onClick(PrivateGroup item) {

                            mGroupShareMenuItemOnClickListener.onClick(item);

                        }
                    });

            groupShareMenuContainer.addView(groupShareMenuLayout.getView());

            groupShareMenuLayout.initView();

        }

        shareInAppLayout.setOnClickListener(this);
        shareToOtherLayout.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_in_app_layout:

                mShareInAppCommand.execute();

                break;
            case R.id.share_to_other_app_layout:

                mShareToOtherAppCommand.execute();

                break;
        }

        dialog.dismiss();
    }
}
