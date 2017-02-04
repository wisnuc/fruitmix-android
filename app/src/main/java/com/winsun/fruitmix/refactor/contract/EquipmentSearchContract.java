package com.winsun.fruitmix.refactor.contract;

import android.view.View;
import android.widget.ExpandableListView;

import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface EquipmentSearchContract {

    interface EquipmentSearchView extends BaseView {

        void showEquipments(List<Equipment> equipments);

        void collapseGroup(int position);

        void expandGroup(int position);

        void isGroupExpanded(int position);

    }

    interface EquipmentSearchPresenter extends BasePresenter<EquipmentSearchView> {

        boolean onEquipmentListViewGroupClick(ExpandableListView parent, View v, int groupPosition, long id);

        boolean onEquipmentListViewChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id);

        void startDiscovery();

        void stopDiscovery();

    }

}
