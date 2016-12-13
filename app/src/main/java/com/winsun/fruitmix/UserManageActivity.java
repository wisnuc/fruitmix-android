package com.winsun.fruitmix;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.LocalCache;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserManageActivity extends Activity implements View.OnClickListener {

    @BindView(R.id.back)
    ImageView mBack;

    @BindView(R.id.user_list)
    ListView mUserListView;

    @BindView(R.id.user_list_empty)
    TextView mUserListEmpty;

    private List<User> mUserList;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        ButterKnife.bind(this);

        mContext = this;

        if (LocalCache.RemoteUserMapKeyIsUUID != null && !LocalCache.RemoteUserMapKeyIsUUID.isEmpty()) {

            mUserListEmpty.setVisibility(View.GONE);
            mUserListView.setVisibility(View.VISIBLE);

            mUserList = new ArrayList<>();

            for (User user : LocalCache.RemoteUserMapKeyIsUUID.values()) {
                mUserList.add(user);
            }

            Collections.sort(mUserList, new Comparator<User>() {
                @Override
                public int compare(User lhs, User rhs) {
                    return Collator.getInstance(Locale.CHINESE).compare(lhs.getUserName(), (rhs.getUserName()));
                }
            });

            UserListAdapter mUserListAdapter = new UserListAdapter();
            mUserListView.setAdapter(mUserListAdapter);

        } else {
            mUserListView.setVisibility(View.GONE);
            mUserListEmpty.setVisibility(View.VISIBLE);
        }

        mBack.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext = null;
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.user_manage_item,parent,false);

                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.refreshView(mUserList.get(position));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewHolder.getDelUserVisibility() != View.VISIBLE) {
                        viewHolder.setDelUserVisibility(View.VISIBLE);
                    } else {
                        viewHolder.setDelUserVisibility(View.INVISIBLE);
                    }
                }
            });

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
            mUserName.setText(user.getUserName());
            if (user.getEmail().length() > 0) {
                mUserEmail.setVisibility(View.VISIBLE);
                mUserEmail.setText(user.getEmail());
            } else {
                mUserEmail.setVisibility(View.GONE);
            }

            StringBuilder stringBuilder = new StringBuilder();
            String[] splitStrings = user.getUserName().split(" ");
            for (String splitString : splitStrings) {
                stringBuilder.append(splitString.substring(0, 1));
            }
            mUserDefaultPortrait.setText(stringBuilder.toString());
        }

        void setDelUserVisibility(int visibility) {
            mDelUser.setVisibility(visibility);
        }

        int getDelUserVisibility() {
            return mDelUser.getVisibility();
        }

    }

}
