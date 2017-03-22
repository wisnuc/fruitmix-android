package com.winsun.fruitmix.contract;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;
import com.winsun.fruitmix.model.LoggedInUser;

import java.util.List;

/**
 * Created by Administrator on 2017/3/21.
 */

public interface AccountManageContract {

    interface AccountManageView extends BaseView{

        void setData(List<String> equipmentNames,List<List<LoggedInUser>> users);

        void setResult(int result);

        void finishActivity();

    }

    interface AccountManagePresenter extends BasePresenter<AccountManageView>{

        void initView();

        void deleteUserOnClick(int groupPosition,int childPosition);
    }

}
