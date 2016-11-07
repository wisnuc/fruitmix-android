package com.winsun.fruitmix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class EquipmentSearchActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = EquipmentSearchActivity.class.getSimpleName();

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.equipment_expandablelist)
    ExpandableListView mEquipmentExpandableListView;
    @BindView(R.id.loading_layout)
    LinearLayout mLoadingLayout;

    private Context mContext;

    private EquipmentExpandableAdapter mAdapter;

    private List<Equipment> mEquipments;

    private NsdManager.DiscoveryListener mListener;

    private NsdManager mManager;

    private List<List<Map<String, String>>> mUserExpandableLists;

    private CustomHandler mHandler;

    private static final int DATA_CHANGE = 0x0001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_search);

        ButterKnife.bind(this);

        mContext = this;

        mUserExpandableLists = new ArrayList<>();
        mEquipments = new ArrayList<>();

        Equipment equipment = new Equipment("Winsuc Appliction 141 By Wu", "192.168.5.141", 6666);
        getUserList(equipment);

        mHandler = new CustomHandler(this, getMainLooper());

        mAdapter = new EquipmentExpandableAdapter();
        mEquipmentExpandableListView.setAdapter(mAdapter);

        mEquipmentExpandableListView.setGroupIndicator(null);

        mEquipmentExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                Map<String, String> userMap = mUserExpandableLists.get(groupPosition).get(childPosition);

                Intent intent = new Intent(mContext, LoginActivity.class);
                intent.putExtra(Util.GATEWAY, "http://" + mEquipments.get(groupPosition).getHost());
                intent.putExtra(Util.EQUIPMENT_GROUP_NAME, mEquipments.get(groupPosition).getServiceName());
                intent.putExtra(Util.EQUIPMENT_CHILD_NAME, userMap.get("username"));
                intent.putExtra(Util.USER_UUID, userMap.get("uuid"));
                startActivityForResult(intent, Util.KEY_LOGIN_REQUEST_CODE);

                return false;
            }
        });

        mBack.setOnClickListener(this);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    @Override
    protected void onResume() {
        super.onResume();

        discoverService(mContext);
    }

    @Override
    protected void onPause() {

        super.onPause();

        try {
            stopDiscoverServices(mContext, mListener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Util.KEY_LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            default:
        }
    }


    class EquipmentExpandableAdapter extends BaseExpandableListAdapter {

        List<Equipment> equipmentList;
        List<List<Map<String, String>>> mapList;

        EquipmentExpandableAdapter() {
            equipmentList = new ArrayList<>();
            mapList = new ArrayList<>();
        }

        @Override
        public int getGroupCount() {
            return equipmentList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mapList.get(groupPosition).size();
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
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            GroupViewHolder groupViewHolder;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.equipment_group_item, null);
                groupViewHolder = new GroupViewHolder(convertView);
                convertView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) convertView.getTag();
            }

            groupViewHolder.refreshView(groupPosition, isExpanded);


            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            ChildViewHolder childViewHolder;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.equipment_child_item, null);
                childViewHolder = new ChildViewHolder(convertView);
                convertView.setTag(childViewHolder);
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
            mGroupName.setText(mAdapter.equipmentList.get(groupPosition).getServiceName());
            mEquipmentIpTV.setText(mAdapter.equipmentList.get(groupPosition).getHost());
            if (isExpanded) {
                mArrow.setBackgroundResource(R.drawable.arrow_down);
            } else {
                mArrow.setBackgroundResource(R.drawable.arrow);
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

            String childName = mAdapter.mapList.get(groupPosition).get(childPosition).get("username");
            mChildName.setText(childName);

            StringBuilder stringBuilder = new StringBuilder();
            String[] splitStrings = childName.split(" ");
            for (String splitString : splitStrings) {
                stringBuilder.append(splitString.substring(0, 1).toUpperCase());
            }
            mUserDefaultPortrait.setText(stringBuilder.toString());
            int color = (int) (Math.random() * 3);
            switch (color) {
                case 0:
                    mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_blue);
                    break;
                case 1:
                    mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_green);
                    break;
                case 2:
                    mUserDefaultPortrait.setBackgroundResource(R.drawable.user_portrait_bg_yellow);
                    break;
            }

        }
    }


    private void discoverService(final Context context) {

        if (mManager == null)
            mManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        NsdManager.DiscoveryListener nsDicListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {

            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {

            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {

                Log.i(TAG, "onServiceLost");

            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {

                Log.i(TAG, "Service resolved: " + serviceInfo);

                if (serviceInfo.getServiceName().toLowerCase().contains("wisnuc")) {
                    resolveService(serviceInfo);
                }

            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "onDiscoveryStopped");

            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.i(TAG, "onDiscoveryStarted");
            }
        };
        mManager.discoverServices("_http._tcp", NsdManager.PROTOCOL_DNS_SD, nsDicListener);

        mListener = nsDicListener;
    }

    private void resolveService(NsdServiceInfo serviceInfo) {
        mManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

                resolveService(serviceInfo);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "onServiceResolved Service info:" + serviceInfo);

                for (Equipment equipment : mEquipments) {
                    if (equipment == null || serviceInfo.getServiceName().equals(equipment.getServiceName()) || (serviceInfo.getHost().getHostAddress().equals(equipment.getHost()))) {
                        return;
                    }
                }

                Equipment equipment = new Equipment();
                equipment.setServiceName(serviceInfo.getServiceName());
                Log.i(TAG, "host address:" + serviceInfo.getHost().getHostAddress());
                equipment.setHost(serviceInfo.getHost().getHostAddress());
                equipment.setPort(serviceInfo.getPort());

                getUserList(equipment);
            }
        });
    }

    private void stopDiscoverServices(Context context, NsdManager.DiscoveryListener listener) {

        if (mManager == null)
            mManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        if (listener != null) {
            mManager.stopServiceDiscovery(listener);
        }
    }

    private void getUserList(final Equipment equipment) {

        //get user list;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Map<String, String> item;
                List<Map<String, String>> itemList;
                JSONObject itemRaw;
                JSONArray json;
                HttpURLConnection conn;
                String str;

                try {
                    Log.i(TAG, "login url:" + Util.HTTP + equipment.getHost() + ":" + FNAS.PORT + Util.LOGIN_PARAMETER);
                    conn = (HttpURLConnection) (new URL(Util.HTTP + equipment.getHost() + ":"  + FNAS.PORT + Util.LOGIN_PARAMETER)).openConnection();
                    Log.i(TAG, "response code" + conn.getResponseCode() + "");
                    str = FNAS.ReadFull(conn.getInputStream());
                    json = new JSONArray(str);
                    itemList = new ArrayList<>();
                    for (int i = 0; i < json.length(); i++) {
                        itemRaw = json.getJSONObject(i);
                        item = new HashMap<>();
                        item.put("username", itemRaw.getString("username"));
                        item.put("uuid", itemRaw.getString("uuid"));
                        item.put("avatar", itemRaw.getString("avatar"));
                        itemList.add(item);
                    }

                    for (Equipment equipment1 : mEquipments) {
                        if (equipment1.getHost().equals(equipment.getHost()))
                            return;
                    }

                    mEquipments.add(equipment);
                    mUserExpandableLists.add(itemList);

                    Log.i(TAG, "EquipmentSearch: " + mUserExpandableLists.toString());

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

                    mAdapter.equipmentList.clear();
                    mAdapter.mapList.clear();
                    mAdapter.equipmentList.addAll(mEquipments);
                    mAdapter.mapList.addAll(mUserExpandableLists);
                    weakReference.get().mAdapter.notifyDataSetChanged();
                    break;
                default:
            }
        }
    }

}
