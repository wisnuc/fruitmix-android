package com.winsun.fruitmix.refactor.common;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface BaseView {

    boolean isNetworkAlive();

    void showNoNetwork();

    void showLoadingUI();

    void dismissLoadingUI();

    void showNoContentUI();

    void dismissNoContentUI();

    void showContentUI();

    void dismissContentUI();

    void showDialog();

    void dismissDialog();

    void hideSoftInput();

}
