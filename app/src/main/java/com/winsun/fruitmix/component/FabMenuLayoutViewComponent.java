package com.winsun.fruitmix.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.anim.AnimatorBuilder;
import com.winsun.fruitmix.anim.SharpCurveInterpolator;
import com.winsun.fruitmix.command.AbstractCommand;
import com.winsun.fruitmix.databinding.FabMenuLayoutBinding;
import com.winsun.fruitmix.dialog.ShareMenuBottomDialogFactory;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2018/2/25.
 */

public class FabMenuLayoutViewComponent implements View.OnClickListener {

    public static final String TAG = FabMenuLayoutViewComponent.class.getSimpleName();

    private FabMenuLayoutBinding mFabMenuLayoutBinding;

    private FloatingActionButton fab;

    private ImageView systemShareBtn;

    private FloatingActionButton downloadFileBtn;

    private boolean sMenuUnfolding = false;

    private Context mContext;

    private boolean showDownloadFileBtn;

    private FabMenuItemOnClickListener mFabMenuItemOnClickListener;

    public FabMenuLayoutViewComponent(FabMenuLayoutBinding fabMenuLayoutBinding, boolean showDownloadFileBtn,
                                      FabMenuItemOnClickListener fabMenuItemOnClickListener) {
        mFabMenuLayoutBinding = fabMenuLayoutBinding;

        fab = fabMenuLayoutBinding.fab;
        systemShareBtn = fabMenuLayoutBinding.systemShare;
        downloadFileBtn = fabMenuLayoutBinding.downloadFileBtn;

        mContext = fab.getContext();

        fab.setOnClickListener(this);
        systemShareBtn.setOnClickListener(this);
        downloadFileBtn.setOnClickListener(this);

        this.showDownloadFileBtn = showDownloadFileBtn;
        mFabMenuItemOnClickListener = fabMenuItemOnClickListener;

    }

    public void setShowDownloadFileBtn(boolean showDownloadFileBtn) {
        this.showDownloadFileBtn = showDownloadFileBtn;
    }

    public void hideFabMenuItem(){

        fab.setVisibility(View.GONE);
        systemShareBtn.setVisibility(View.GONE);
        downloadFileBtn.setVisibility(View.GONE);

    }

    public interface FabMenuItemOnClickListener {

        void systemShareBtnOnClick();

        void downloadFileBtnOnClick();

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.system_share:

                mFabMenuItemOnClickListener.systemShareBtnOnClick();

                break;

            case R.id.download_file_btn:

                mFabMenuItemOnClickListener.downloadFileBtnOnClick();

                break;

            case R.id.fab:
                refreshFabState();
                break;

            default:
                Log.e(TAG, "onClick: fab menu onClick enter default case");
        }

    }

    private void refreshFabState() {
        if (sMenuUnfolding) {
            sMenuUnfolding = false;
            collapseFabAnimation();
        } else {
            sMenuUnfolding = true;
            extendFabAnimation();
        }
    }

    public void collapseFab() {
        if (sMenuUnfolding) {
            sMenuUnfolding = false;
            collapseFabAnimation();
        }
    }


    public void showFab() {

        if (fab.getVisibility() == View.VISIBLE)
            return;

        new AnimatorBuilder(mContext, R.animator.fab_translation, fab).addAdapter(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                fab.setVisibility(View.VISIBLE);
            }
        }).setInterpolator(new LinearOutSlowInInterpolator()).startAnimator();

    }

    public void dismissFab() {

        if (fab.getVisibility() == View.GONE)
            return;

        new AnimatorBuilder(mContext, R.animator.fab_translation_restore, fab).addAdapter(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                fab.setVisibility(View.GONE);
            }
        }).setInterpolator(SharpCurveInterpolator.getSharpCurveInterpolator()).startAnimator();

    }


    private void collapseFabAnimation() {

        new AnimatorBuilder(mContext, R.animator.fab_remote_restore, fab).startAnimator();

        if (systemShareBtn.getVisibility() == View.VISIBLE) {
            new AnimatorBuilder(mContext, R.animator.first_btn_above_fab_translation_restore, systemShareBtn).addAdapter(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    systemShareBtn.setVisibility(View.GONE);

                }
            }).startAnimator();

            if (downloadFileBtn.getVisibility() == View.VISIBLE) {

                new AnimatorBuilder(mContext, R.animator.second_btn_above_fab_translation_restore, downloadFileBtn).addAdapter(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        downloadFileBtn.setVisibility(View.GONE);

                    }
                }).startAnimator();
            }
        } else {

            if (downloadFileBtn.getVisibility() == View.VISIBLE) {

                new AnimatorBuilder(mContext, R.animator.first_btn_above_fab_translation_restore, downloadFileBtn).addAdapter(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        downloadFileBtn.setVisibility(View.GONE);

                    }
                }).startAnimator();
            }

        }


    }

    private void extendFabAnimation() {

        new AnimatorBuilder(mContext, R.animator.fab_remote, fab).startAnimator();

        if (!showDownloadFileBtn) {

            downloadFileBtn.setVisibility(View.GONE);

            systemShareBtn.setVisibility(View.VISIBLE);

            new AnimatorBuilder(mContext, R.animator.first_btn_above_fab_translation, systemShareBtn).startAnimator();

        } else {

/*            systemShareBtn.setVisibility(View.GONE);

            downloadFileBtn.setVisibility(View.VISIBLE);

            if (systemShareBtn.getVisibility() == View.VISIBLE) {

                new AnimatorBuilder(getContext(), R.animator.first_btn_above_fab_translation, systemShareBtn).startAnimator();

                if (downloadFileBtn.getVisibility() == View.VISIBLE)
                    new AnimatorBuilder(getContext(), R.animator.second_btn_above_fab_translation, downloadFileBtn).startAnimator();

            } else {

                if (downloadFileBtn.getVisibility() == View.VISIBLE)
                    new AnimatorBuilder(getContext(), R.animator.first_btn_above_fab_translation, downloadFileBtn).startAnimator();

            }*/

            systemShareBtn.setVisibility(View.VISIBLE);

            downloadFileBtn.setVisibility(View.VISIBLE);

            new AnimatorBuilder(mContext, R.animator.first_btn_above_fab_translation, systemShareBtn).startAnimator();

            if (downloadFileBtn.getVisibility() == View.VISIBLE)
                new AnimatorBuilder(mContext, R.animator.second_btn_above_fab_translation, downloadFileBtn).startAnimator();

        }

    }


}
