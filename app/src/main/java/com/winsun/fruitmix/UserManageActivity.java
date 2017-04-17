package com.winsun.fruitmix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserManageActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "UserManageActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.title)
    TextView mTitleTextView;

    @BindView(R.id.user_list)
    ListView mUserListView;

    @BindView(R.id.user_list_empty)
    TextView mUserListEmpty;

    @BindView(R.id.add_user)
    FloatingActionButton mAddUserBtn;

    private List<User> mUserList;

    private Context mContext;

    private UserListAdapter mUserListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        ButterKnife.bind(this);

        mContext = this;

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitleTextView.setText(getString(R.string.user_manage));

        mAddUserBtn.setOnClickListener(this);

        refreshView();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
    }

    private void refreshView() {

        if (LocalCache.RemoteUserMapKeyIsUUID != null && !LocalCache.RemoteUserMapKeyIsUUID.isEmpty()) {

            mUserListEmpty.setVisibility(View.GONE);
            mUserListView.setVisibility(View.VISIBLE);

            refreshUserList();

            if (mUserListAdapter == null) {
                mUserListAdapter = new UserListAdapter();
                mUserListView.setAdapter(mUserListAdapter);
            } else {
                mUserListAdapter.notifyDataSetChanged();
            }

        } else {
            mUserListView.setVisibility(View.GONE);
            mUserListEmpty.setVisibility(View.VISIBLE);
        }

    }

    private void refreshUserList() {

        if (mUserList == null)
            mUserList = new ArrayList<>();
        else
            mUserList.clear();

        Collection<User> collection = LocalCache.RemoteUserMapKeyIsUUID.values();
        for (User user : collection) {
            mUserList.add(user);
        }

        Collections.sort(mUserList, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return Collator.getInstance(Locale.CHINESE).compare(lhs.getUserName(), (rhs.getUserName()));
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.add_user:

                Intent intent = new Intent(mContext, CreateUserActivity.class);
                startActivityForResult(intent, Util.KEY_CREATE_USER_REQUEST_CODE);

                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Util.KEY_CREATE_USER_REQUEST_CODE && resultCode == RESULT_OK) {
            refreshView();
        }
    }

    private class UserListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mUserList == null ? 0 : mUserList.size();
        }

        @Override
        public Object getItem(int position) {
            return mUserListEmpty == null ? null : mUserList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.user_manage_item, parent, false);

                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.refreshView(mUserList.get(position));

/*            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewHolder.getDelUserVisibility() != View.VISIBLE) {
                        viewHolder.setDelUserVisibility(View.VISIBLE);
                    } else {
                        viewHolder.setDelUserVisibility(View.INVISIBLE);
                    }
                }
            });*/

            return convertView;
        }
    }

    class ViewHolder {
        @BindView(R.id.user_default_portrait)
        TextView mUserDefaultPortrait;
        @BindView(R.id.user_name)
        TextView mUserName;
        @BindView(R.id.user_email)
        TextView mUserEmail;
        @BindView(R.id.del_user)
        LinearLayout mDelUser;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(User user) {

            String userName = user.getUserName();

            if (userName.length() > 20) {
                userName = userName.substring(0, 20);
                userName += mContext.getString(R.string.android_ellipsize);
            }

            mUserName.setText(userName);
            if (user.getEmail().length() > 0) {
                mUserEmail.setVisibility(View.VISIBLE);
                mUserEmail.setText(user.getEmail());
            } else {
                mUserEmail.setVisibility(View.GONE);
            }

            mUserDefaultPortrait.setText(Util.getUserNameFirstLetter(user.getUserName()));
        }

        void setDelUserVisibility(int visibility) {
            mDelUser.setVisibility(visibility);
        }

        int getDelUserVisibility() {
            return mDelUser.getVisibility();
        }

    }

}
