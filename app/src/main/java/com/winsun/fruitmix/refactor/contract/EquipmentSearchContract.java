package com.winsun.fruitmix.refactor.contract;

import android.view.View;
import android.widget.ExpandableListView;

import com.github.druk.rxdnssd.RxDnssd;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface EquipmentSearchContract {

    interface EquipmentSearchView extends BaseView {

        void showEquipmentsAndUsers(List<Equipment> equipments, List<List<User>> users);

        void collapseGroup(int position);

        void expandGroup(int position);

        boolean isGroupExpanded(int position);

        void login(String gateway, String userGroupName, User user);

        int getGroupCount();

        void finishActivity();

    }

    interface EquipmentSearchPresenter extends BasePresenter<EquipmentSearchView> {

        boolean onEquipmentListViewGroupClick(ExpandableListView parent, View v, int groupPosition, long id);

        boolean onEquipmentListViewChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id);

        void startDiscovery(RxDnssd rxDnssd);

        void stopDiscovery();

    }

}
