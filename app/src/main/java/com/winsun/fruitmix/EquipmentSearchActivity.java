package com.winsun.fruitmix;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.github.druk.rxdnssd.BonjourService;
import com.github.druk.rxdnssd.RxDnssd;
import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.component.AnimatedExpandableListView;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.model.LoggedInUser;
import com.winsun.fruitmix.model.LoginType;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.services.ButlerService;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class EquipmentSearchActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "EquipmentSearchActivity";

    @BindView(R.id.title)
    TextView mTitleTextView;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.equipment_expandablelist)
    AnimatedExpandableListView mEquipmentExpandableListView;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;

    private Context mContext;

    private EquipmentExpandableAdapter mAdapter;

    private List<Equipment> mUserLoadedEquipments;

    private List<Equipment> mFoundedEquipments;

    private static final String SERVICE_PORT = "_http._tcp";
    private static final String DEMAIN = "local.";

    private List<List<User>> mUserExpandableLists;

    private CustomHandler mHandler;

    private static final int DATA_CHANGE = 0x0001;

    private static final String SYSTEM_PORT = "3000";
    private static final String IPALIASING = "/system/ipaliasing";

    private boolean mStartAnimateArrow = false;

    private RxDnssd mRxDnssd;
    private Subscription mSubscription;

    private Random random;

    private int preAvatarBgColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_search);

        ButterKnife.bind(this);

        mContext = this;

        random = new Random();

        Util.loginType = LoginType.LOGIN;

        mUserExpandableLists = new ArrayList<>();
        mUserLoadedEquipments = new ArrayList<>();

        mFoundedEquipments = new ArrayList<>();

        mHandler = new CustomHandler(this, getMainLooper());

        mAdapter = new EquipmentExpandableAdapter();
        mEquipmentExpandableListView.setAdapter(mAdapter);

        mEquipmentExpandableListView.setGroupIndicator(null);

        mEquipmentExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                User user = mUserExpandableLists.get(groupPosition).get(childPosition);

                for (LoggedInUser loggedInUser : LocalCache.LocalLoggedInUsers) {

                    if (loggedInUser.getUser().getUuid().equals(user.getUuid())) {

                        Util.loginType = LoginType.SPLASH_SCREEN;

                        FNAS.Gateway = "http://" + mUserLoadedEquipments.get(groupPosition).getHosts().get(0);
                        FNAS.userUUID = loggedInUser.getUser().getUuid();
                        FNAS.JWT = loggedInUser.getToken();
                        LocalCache.DeviceID = loggedInUser.getDeviceID();

                        LocalCache.saveToken(mContext, FNAS.JWT);
                        LocalCache.saveGateway(mContext, FNAS.Gateway);
                        LocalCache.saveUserUUID(mContext, FNAS.userUUID);
                        LocalCache.SetGlobalData(mContext, Util.DEVICE_ID_MAP_NAME, LocalCache.DeviceID);

                        checkAutoUpload();

                        FNAS.retrieveUser(mContext);
                        startActivity(new Intent(mContext, NavPagerActivity.class));
                        setResult(RESULT_OK);
                        finish();

                        return true;
                    }

                }

                Equipment equipment = mUserLoadedEquipments.get(groupPosition);

                Intent intent = new Intent(mContext, LoginActivity.class);
                intent.putExtra(Util.GATEWAY, "http://" + equipment.getHosts().get(0));
                intent.putExtra(Util.USER_GROUP_NAME, Util.getEquipmentName(equipment));
                intent.putExtra(Util.USER_NAME, user.getUserName());
                intent.putExtra(Util.USER_UUID, user.getUuid());
                intent.putExtra(Util.USER_BG_COLOR, user.getDefaultAvatarBgColor());

                startActivityForResult(intent, Util.KEY_LOGIN_REQUEST_CODE);

                return false;
            }
        });

        mEquipmentExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                mStartAnimateArrow = true;

                int count = mAdapter.getGroupCount();
                for (int i = 0; i < count; i++) {


                    if (i == groupPosition) {

                        if (mEquipmentExpandableListView.isGroupExpanded(i)) {
                            mEquipmentExpandableListView.collapseGroupWithAnimation(i);
                        } else {
                            mEquipmentExpandableListView.expandGroup(i, true);
                        }

                    } else {

                        if (mEquipmentExpandableListView.isGroupExpanded(i)) {
                            mEquipmentExpandableListView.collapseGroupWithAnimation(i);
                        }

                    }
                }

                return true;
            }
        });

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getBooleanExtra(Util.KEY_SHOULD_STOP_SERVICE, true))
                    ButlerService.stopButlerService(mContext);

                finish();
            }
        });

        mTitleTextView.setText(getString(R.string.search_equipment));

        mRxDnssd = CustomApplication.getRxDnssd(mContext);
    }

    @Override
    protected void onResume() {
        super.onResume();

        startDiscovery();

//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopDiscovery();

//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
    }

    private void checkAutoUpload() {
        for (LoggedInUser loggedInUser : LocalCache.LocalLoggedInUsers) {

            if (loggedInUser.getUser().getUuid().equals(FNAS.userUUID)) {
                if (!LocalCache.getCurrentUploadDeviceID(mContext).equals(LocalCache.DeviceID)) {
                    LocalCache.setAutoUploadOrNot(mContext, false);
                    Toast.makeText(mContext, getString(R.string.photo_auto_upload_already_close), Toast.LENGTH_SHORT).show();
                } else {
                    LocalCache.setAutoUploadOrNot(mContext, true);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.KEY_LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        } else if (requestCode == Util.KEY_MANUAL_INPUT_IP_REQUEST_CODE && resultCode == RESULT_OK) {

            String ip = data.getStringExtra(Util.KEY_MANUAL_INPUT_IP);

            List<String> hosts = new ArrayList<>();
            hosts.add(ip);

            Equipment equipment = new Equipment("Winsuc Appliction " + ip, hosts, 6666);
            getUserList(equipment);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

        if (getIntent().getBooleanExtra(Util.KEY_SHOULD_STOP_SERVICE, true))
            ButlerService.stopButlerService(mContext);

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

            mGroupName.setText(equipment.getModel() + "-" + equipment.getSerialNumber());

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

                int avatarBgColor = random.nextInt(3) + 1;

                if (preAvatarBgColor != 0) {

                    if (avatarBgColor == preAvatarBgColor) {
                        if (avatarBgColor == 3) {
                            avatarBgColor--;
                        } else if (avatarBgColor == 1) {
                            avatarBgColor++;
                        } else {

                            if (random.nextBoolean()) {
                                avatarBgColor++;
                            } else {
                                avatarBgColor--;
                            }

                        }
                    }

                    preAvatarBgColor = avatarBgColor;

                } else {
                    preAvatarBgColor = avatarBgColor;
                }


                user.setDefaultAvatarBgColor(avatarBgColor);
            }

            mUserDefaultPortrait.setBackgroundResource(user.getDefaultAvatarBgColorResourceId());

        }
    }

    private void startDiscovery() {

        mSubscription = mRxDnssd.browse(SERVICE_PORT, DEMAIN)
                .compose(mRxDnssd.resolve())
                .compose(mRxDnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BonjourService>() {
                    @Override
                    public void call(BonjourService bonjourService) {

                        if (bonjourService.isLost()) return;

                        String serviceName = bonjourService.getServiceName();
                        if (!serviceName.toLowerCase().contains("wisnuc")) return;

                        if (bonjourService.getInet4Address() == null) return;

                        String hostAddress = bonjourService.getInet4Address().getHostAddress();

                        String hostName = bonjourService.getHostname();

                        Log.d(TAG, "call: hostName: " + hostName);

                        String model = "";
                        String serialNumber = "";

                        if (hostName != null && hostName.contains("-")) {

                            String[] hostNames = hostName.split("-");

                            model = hostNames[1].toUpperCase();

                            String[] hostNames2 = hostNames[2].split("\\.");

                            serialNumber = hostNames2[0].toUpperCase();

                            Log.d(TAG, "discovery: model:" + model + " serialNumber:" + serialNumber);
                        }

                        for (Equipment equipment : mFoundedEquipments) {
                            if (equipment == null || serviceName.equals(equipment.getServiceName())
                                    || equipment.getHosts().contains(hostAddress)
                                    || serialNumber.equals(equipment.getSerialNumber())) {
                                return;
                            }
                        }

                        Equipment equipment = new Equipment();
                        equipment.setServiceName(serviceName);
                        Log.d(TAG, "host address:" + hostAddress);

                        List<String> hosts = new ArrayList<>();
                        hosts.add(hostAddress);

                        equipment.setHosts(hosts);
                        equipment.setPort(bonjourService.getPort());

                        equipment.setModel(model);
                        equipment.setSerialNumber(serialNumber);

                        mFoundedEquipments.add(equipment);

                        getUserList(equipment);

                    }
                });

    }

    private void stopDiscovery() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    private void getUserList(final Equipment equipment) {

        //get user list;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                User user;
                List<User> itemList;
                JSONObject itemRaw;
                JSONArray json;
                String str;

                try {

                    String url = Util.HTTP + equipment.getHosts().get(0) + ":" + SYSTEM_PORT + IPALIASING;

                    Log.d(TAG, "login retrieve equipment alias:" + url);

                    str = FNAS.RemoteCallWithUrl(url).getResponseData();

                    json = new JSONArray(str);

                    int length = json.length();

                    for (int i = 0; i < length; i++) {
                        itemRaw = json.getJSONObject(i);

                        String ip = itemRaw.getString("ipv4");

                        List<String> hosts = equipment.getHosts();
                        if (!hosts.contains(ip)) {
                            hosts.add(ip);
                        }
                    }

                    url = Util.HTTP + equipment.getHosts().get(0) + ":" + FNAS.PORT + Util.LOGIN_PARAMETER;

                    Log.d(TAG, "login url:" + url);

                    str = FNAS.RemoteCallWithUrl(url).getResponseData();

                    json = new JSONArray(str);
                    itemList = new ArrayList<>();

                    length = json.length();

                    for (int i = 0; i < length; i++) {
                        itemRaw = json.getJSONObject(i);
                        user = new User();
                        user.setUserName(itemRaw.getString("username"));
                        user.setUuid(itemRaw.getString("uuid"));
                        user.setAvatar(itemRaw.getString("avatar"));
                        itemList.add(user);
                    }

                    if (itemList.isEmpty())
                        return;

                    for (Equipment equipment1 : mUserLoadedEquipments) {
                        if (equipment1.getHosts().contains(equipment.getHosts().get(0)))
                            return;
                    }

                    mUserLoadedEquipments.add(equipment);
                    mUserExpandableLists.add(itemList);

                    Log.d(TAG, "EquipmentSearch: " + mUserExpandableLists.toString());

                    //update list
                    mHandler.sendEmptyMessage(DATA_CHANGE);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        ExecutorServiceInstance instance = ExecutorServiceInstance.SINGLE_INSTANCE;
        instance.doOneTaskInCachedThread(runnable);

    }

    private class CustomHandler extends Handler {

        WeakReference<EquipmentSearchActivity> weakReference = null;

        CustomHandler(EquipmentSearchActivity activity, Looper looper) {
            super(looper);
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DATA_CHANGE:

                    if (mEquipmentExpandableListView.getVisibility() == View.GONE) {
                        mEquipmentExpandableListView.setVisibility(View.VISIBLE);
                        mLoadingLayout.setVisibility(View.GONE);
                    }

                    EquipmentExpandableAdapter adapter = weakReference.get().mAdapter;

                    adapter.equipmentList.clear();
                    adapter.mapList.clear();
                    adapter.equipmentList.addAll(mUserLoadedEquipments);
                    adapter.mapList.addAll(mUserExpandableLists);
                    adapter.viewLruCache.evictAll();
                    adapter.notifyDataSetChanged();
                    break;
                default:
            }
        }
    }

}
