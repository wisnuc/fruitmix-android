package com.winsun.fruitmix.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.interfaces.BaseView;

/**
 * Created by Administrator on 2017/6/29.
 */

public class RevealToolbarViewModel {

    public final ObservableBoolean showRevealToolbar = new ObservableBoolean(false);

    public final ObservableField<String> selectCountTitleText = new ObservableField<>();

    public final ObservableInt enterSelectModeVisibility = new ObservableInt(View.GONE);

    private BaseView baseView;

    public void setBaseView(BaseView baseView) {
        this.baseView = baseView;
    }

    public interface RevealToolbarNavigationOnClickListener {
        void onClick();
    }

    private RevealToolbarNavigationOnClickListener revealToolbarNavigationOnClickListener;

    public void setRevealToolbarNavigationOnClickListener(RevealToolbarNavigationOnClickListener revealToolbarNavigationOnClickListener) {
        this.revealToolbarNavigationOnClickListener = revealToolbarNavigationOnClickListener;
    }

    public void navigationOnClick() {

        if (baseView != null) {
            baseView.finishView();
        } else {
            revealToolbarNavigationOnClickListener.onClick();
        }

    }
}
