package com.winsun.fruitmix;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.winsun.fruitmix.util.LocalCache;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserManageActivity extends Activity implements View.OnClickListener {

    @BindView(R.id.back)
    ImageView mBack;

    @BindView(R.id.user_list)
    ListView mUserListView;

    @BindView(R.id.user_list_empty)
    TextView mUserListEmpty;

    private List<Map<String, String>> mUserMapList;

    private Context mContext;

    private UserListAdapter mUserListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        ButterKnife.bind(this);

        mContext = this;

        if (LocalCache.UsersMap != null && !LocalCache.UsersMap.isEmpty()) {

            mUserListEmpty.setVisibility(View.GONE);
            mUserListView.setVisibility(View.VISIBLE);

            mUserMapList = new ArrayList<>();

            for (Map.Entry<String, Map<String, String>> map : LocalCache.UsersMap.entrySet()) {
                mUserMapList.add(map.getValue());
            }

            Collections.sort(mUserMapList, new Comparator<Map<String, String>>() {
                @Override
                public int compare(Map<String, String> lhs, Map<String, String> rhs) {
                    return Collator.getInstance(Locale.CHINESE).compare(lhs.get("name"), (rhs.get("name")));
                }
            });

            mUserListAdapter = new UserListAdapter();
            mUserListView.setAdapter(mUserListAdapter);

        } else {
            mUserListView.setVisibility(View.GONE);
            mUserListEmpty.setVisibility(View.VISIBLE);
        }

        mBack.setOnClickListener(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
            return mUserMapList == null ? 0 : mUserMapList.size();
        }

        @Override
        public Object getItem(int position) {
            return mUserListEmpty == null ? null : mUserMapList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.user_manage_item, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.refreshView(mUserMapList.get(position));

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
        ImageView mDelUser;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public void refreshView(Map<String, String> userMap) {
            mUserName.setText(userMap.get("name"));
            if (userMap.containsKey("email")) {
                mUserEmail.setVisibility(View.VISIBLE);
                mUserEmail.setText(userMap.get("email"));
            } else {
                mUserEmail.setVisibility(View.GONE);
            }

            StringBuilder stringBuilder = new StringBuilder();
            String[] splitStrings = userMap.get("name").split(" ");
            for (String splitString : splitStrings) {
                stringBuilder.append(splitString.substring(0, 1));
            }
            mUserDefaultPortrait.setText(stringBuilder.toString());
        }

        public void setDelUserVisibility(int visibility) {
            mDelUser.setVisibility(visibility);
        }

        public int getDelUserVisibility() {
            return mDelUser.getVisibility();
        }

    }

}
