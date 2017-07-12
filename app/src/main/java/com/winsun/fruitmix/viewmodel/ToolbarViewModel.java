package com.winsun.fruitmix.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.interfaces.BaseView;

/**
 * Created by Administrator on 2017/6/29.
 */

public class ToolbarViewModel {

    public final ObservableInt navigationIconResId = new ObservableInt(R.drawable.ic_back_black);

    public final ObservableField<String> titleText = new ObservableField<>();

    public final ObservableBoolean showSelect = new ObservableBoolean(false);

    public final ObservableBoolean showFileMainMenu = new ObservableBoolean(false);

    public final ObservableBoolean showToolbar = new ObservableBoolean(true);

    private BaseView baseView;

    public void setBaseView(BaseView baseView) {
        this.baseView = baseView;
    }

    public interface ToolbarNavigationOnClickListener {
        void onClick();
    }

    private ToolbarNavigationOnClickListener toolbarNavigationOnClickListener;

    public void setToolbarNavigationOnClickListener(ToolbarNavigationOnClickListener toolbarNavigationOnClickListener) {
        this.toolbarNavigationOnClickListener = toolbarNavigationOnClickListener;
    }

    public void toolbarNavigationOnClick() {
        if (baseView != null)
            baseView.finishView();
        else
            toolbarNavigationOnClickListener.onClick();
    }

    public interface ToolbarSelectBtnOnClickListener {
        void onClick();
    }

    private ToolbarSelectBtnOnClickListener toolbarSelectBtnOnClickListener;

    public void setToolbarSelectBtnOnClickListener(ToolbarSelectBtnOnClickListener toolbarSelectBtnOnClickListener) {
        this.toolbarSelectBtnOnClickListener = toolbarSelectBtnOnClickListener;
    }

    public void toolbarSelectBtnOnClick() {
        toolbarSelectBtnOnClickListener.onClick();
    }


    public interface ToolbarFileMainMenuBtnOnClickListener {
        void onClick();
    }

    private ToolbarFileMainMenuBtnOnClickListener toolbarFileMainMenuBtnOnClickListener;

    public void setToolbarFileMainMenuBtnOnClickListener(ToolbarFileMainMenuBtnOnClickListener toolbarFileMainMenuBtnOnClickListener) {
        this.toolbarFileMainMenuBtnOnClickListener = toolbarFileMainMenuBtnOnClickListener;
    }

    public void toolbarFileMainBtnOnClick() {
        toolbarFileMainMenuBtnOnClickListener.onClick();
    }

}
