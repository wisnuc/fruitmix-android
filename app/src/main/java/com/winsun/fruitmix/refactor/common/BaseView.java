package com.winsun.fruitmix.refactor.common;

/**
 * Created by Administrator on 2017/1/24.
 */

public interface BaseView {

    void registerPresenter(BasePresenter presenter);

    void unregisterPresenter(BasePresenter presenter);

    void showNoNetwork();

}
