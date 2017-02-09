package com.winsun.fruitmix.refactor.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.druk.rxdnssd.BonjourService;
import com.github.druk.rxdnssd.RxDnssd;
import com.winsun.fruitmix.CreateNewEquipmentActivity;
import com.winsun.fruitmix.CustomApplication;
import com.winsun.fruitmix.LoginActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.AnimatedExpandableListView;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.refactor.common.BaseActivity;
import com.winsun.fruitmix.refactor.common.Injection;
import com.winsun.fruitmix.refactor.contract.EquipmentSearchContract;
import com.winsun.fruitmix.refactor.presenter.EquipmentSearchPresenterImpl;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class EquipmentSearchActivity extends BaseActivity implements View.OnClickListener, EquipmentSearchContract.EquipmentSearchView {

    public static final String TAG = EquipmentSearchActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.equipment_expandablelist)
    AnimatedExpandableListView mEquipmentExpandableListView;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;

    private Context mContext;

    private EquipmentExpandableAdapter mAdapter;

    private boolean mStartAnimateArrow = false;

    private RxDnssd mRxDnssd;

    private EquipmentSearchContract.EquipmentSearchPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_search);

        ButterKnife.bind(this);

        mContext = this;

        Util.loginType = LoginType.LOGIN;

        mAdapter = new EquipmentExpandableAdapter();
        mEquipmentExpandableListView.setAdapter(mAdapter);

        mEquipmentExpandableListView.setGroupIndicator(null);

        mEquipmentExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                mPresenter.onEquipmentListViewChildClick(parent, v, groupPosition, childPosition, id);

                return false;
            }
        });

        mEquipmentExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                mStartAnimateArrow = true;

                mPresenter.onEquipmentListViewGroupClick(parent, v, groupPosition, id);

                return true;
            }
        });

        mBack.setOnClickListener(this);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mRxDnssd = CustomApplication.getRxDnssd(mContext);

        mPresenter = new EquipmentSearchPresenterImpl(Injection.injectDataRepository());
        mPresenter.attachView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPresenter.startDiscovery(mRxDnssd);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPresenter.stopDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.detachView();

        mContext = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        mPresenter.handleOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:

                ButlerService.stopButlerService(mContext);

                finish();

                mPresenter.handleBackEvent();

                break;
            case R.id.fab:
                Intent intent = new Intent(mContext, CreateNewEquipmentActivity.class);
                startActivityForResult(intent, Util.KEY_MANUAL_INPUT_IP_REQUEST_CODE);
                break;
            default:
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        ButlerService.stopButlerService(mContext);

        mPresenter.handleBackEvent();
    }

    @Override
    public void showEquipmentsAndUsers(List<Equipment> equipments, List<List<User>> users) {

        if (mEquipmentExpandableListView.getVisibility() == View.GONE) {
            mEquipmentExpandableListView.setVisibility(View.VISIBLE);
            mLoadingLayout.setVisibility(View.GONE);
        }

        mAdapter.equipmentList.clear();
        mAdapter.mapList.clear();
        mAdapter.equipmentList.addAll(equipments);
        mAdapter.mapList.addAll(users);
        mAdapter.viewLruCache.evictAll();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void collapseGroup(int position) {
        mEquipmentExpandableListView.collapseGroupWithAnimation(position);
    }

    @Override
    public void expandGroup(int position) {
        mEquipmentExpandableListView.expandGroupWithAnimation(position);
    }

    @Override
    public boolean isGroupExpanded(int position) {
        return mEquipmentExpandableListView.isGroupExpanded(position);
    }

    @Override
    public void login(String gateway, String userGroupName, User user) {

        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(Util.GATEWAY, gateway);
        intent.putExtra(Util.USER_GROUP_NAME, userGroupName);
        intent.putExtra(Util.USER_NAME, user.getUserName());
        intent.putExtra(Util.USER_UUID, user.getUuid());
        intent.putExtra(Util.USER_BG_COLOR, user.getDefaultAvatarBgColor());

        startActivityForResult(intent, Util.KEY_LOGIN_REQUEST_CODE);
    }

    @Override
    public int getGroupCount() {
        return mAdapter.getGroupCount();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    class EquipmentExpandableAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

        List<Equipment> equipmentList;
        List<List<User>> mapList;
        LruCache<Long, View> viewLruCache;

        EquipmentExpandableAdapter() {
            equipmentList = new ArrayList<>();
            mapList = new ArrayList<>();

            viewLruCache = new LruCache<>(5);
        }

        @Override
        public int getGroupCount() {
            return equipmentList.size();
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {

            List<User> list = mapList.get(groupPosition);

            return list == null ? 0 : list.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return equipmentList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mapList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            GroupViewHolder groupViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.equipment_group_item, parent, false);

                groupViewHolder = new GroupViewHolder(convertView);
                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) convertView.getTag();
            }

            groupViewHolder.refreshView(groupPosition, isExpanded);

            return convertView;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            ChildViewHolder childViewHolder;
            if (convertView == null) {

                Long key = ((long) groupPosition << 32) + childPosition;
                View view = viewLruCache.get(key);
                if (view != null && view.getTag() != null) {

                    convertView = view;
                    childViewHolder = (ChildViewHolder) convertView.getTag();

                } else {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.equipment_child_item, parent, false);
                    childViewHolder = new ChildViewHolder(convertView);
                    convertView.setTag(childViewHolder);

                    viewLruCache.put(key, convertView);
                }

            } else {
                childViewHolder = (ChildViewHolder) convertView.getTag();
            }

            childViewHolder.refreshView(groupPosition, childPosition);

            return convertView;
        }

    }

    class GroupViewHolder {

        @BindView(R.id.arrow)
        ImageView mArrow;

        @BindView(R.id.equipment_group_name)
        TextView mGroupName;
        @BindView(R.id.equipment_ip_tv)
        TextView mEquipmentIpTV;

        GroupViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(int groupPosition, boolean isExpanded) {
            Equipment equipment = mAdapter.equipmentList.get(groupPosition);
            if (equipment == null) {
                return;
            }

            mGroupName.setText(equipment.getServiceName());

            List<String> hosts = equipment.getHosts();

            String and = getString(R.string.and);

            StringBuilder builder = new StringBuilder();
            for (String host : hosts) {
                builder.append(and);
                builder.append(host);
            }

            mEquipmentIpTV.setText(builder.substring(1));

            if (mStartAnimateArrow) {

                Animator animator;

                Boolean preIsExpanded = (Boolean) mArrow.getTag();
                if (preIsExpanded != null && preIsExpanded == isExpanded) return;

                if (isExpanded) {
                    animator = AnimatorInflater.loadAnimator(mContext, R.animator.ic_back_remote);

                } else {
                    animator = AnimatorInflater.loadAnimator(mContext, R.animator.ic_back_restore);

                }
                animator.setTarget(mArrow);

                animator.start();

                mArrow.setTag(isExpanded);

                Log.d(TAG, "refreshView: groupPosition:" + groupPosition + " preIsExpanded:" + preIsExpanded + " isExpanded:" + isExpanded);
            }

        }
    }

    class ChildViewHolder {

        @BindView(R.id.user_default_portrait)
        TextView mUserDefaultPortrait;
        @BindView(R.id.equipment_child_name)
        TextView mChildName;

        ChildViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(int groupPosition, int childPosition) {

            if (mAdapter.mapList.get(groupPosition) == null || mAdapter.mapList.get(groupPosition).size() == 0)
                return;

            User user = mAdapter.mapList.get(groupPosition).get(childPosition);

            String childName = user.getUserName();

            if (childName.length() > 20) {
                childName = childName.substring(0, 20);
                childName += mContext.getString(R.string.android_ellipsize);
            }

            mChildName.setText(childName);

            String firstLetter = Util.getUserNameFirstLetter(childName);
            mUserDefaultPortrait.setText(firstLetter);

            if (user.getDefaultAvatarBgColor() == 0) {
                user.setDefaultAvatarBgColor(new Random().nextInt(3) + 1);
            }

            mUserDefaultPortrait.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());

        }
    }


}
