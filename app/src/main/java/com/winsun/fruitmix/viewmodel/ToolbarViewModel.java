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

    public final ObservableField<Integer> selectTextResID = new ObservableField<>(R.string.choose_text);

    public final ObservableBoolean showSelect = new ObservableBoolean(false);

    public final ObservableBoolean showMenu = new ObservableBoolean(false);

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
            baseView.onBackPressed();
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


    public interface ToolbarMenuBtnOnClickListener {
        void onClick();
    }

    private ToolbarMenuBtnOnClickListener toolbarMenuBtnOnClickListener;

    public void setToolbarMenuBtnOnClickListener(ToolbarMenuBtnOnClickListener toolbarMenuBtnOnClickListener) {
        this.toolbarMenuBtnOnClickListener = toolbarMenuBtnOnClickListener;
    }

    public void toolbarMenuBtnOnClick() {
        toolbarMenuBtnOnClickListener.onClick();
    }

}
