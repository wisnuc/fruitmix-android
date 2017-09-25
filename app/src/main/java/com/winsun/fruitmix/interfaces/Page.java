package com.winsun.fruitmix.interfaces;

import android.content.Intent;
import android.view.View;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/2.
 */
public interface Page {

    View getView();

    void refreshView();

    void refreshViewForce();

    void onActivityReenter(int resultCode, Intent data);

    void onMapSharedElements(List<String> names, Map<String, View> sharedElements);

    void onDestroy();

    boolean canEnterSelectMode();

}
