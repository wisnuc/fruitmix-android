package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.SystemMessageTextCommentBinding;
import com.winsun.fruitmix.group.data.model.SystemMessageTextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SystemMessageTextCommentView extends UserCommentView {

    private SystemMessageTextCommentBinding mSystemMessageTextCommentBinding;

    @Override
    public ViewDataBinding getViewDataBinding(Context context, ViewGroup parent) {

        mSystemMessageTextCommentBinding = SystemMessageTextCommentBinding.inflate(LayoutInflater.from(context), parent, false);

        return mSystemMessageTextCommentBinding;
    }

    @Override
    protected View generateContentView(Context context, ViewGroup parent) {
        return null;
    }

    @Override
    protected void refreshContent(Context context, View toolbar, UserComment data, boolean isLeftModel) {

    }

    @Override
    public void refreshCommentView(Context context, View toolbar, UserCommentShowStrategy strategy, UserComment data) {

        SystemMessageTextComment systemMessageTextComment = (SystemMessageTextComment) data;

        formatSystemMessageTextComment(systemMessageTextComment, context);

    }


    private void formatSystemMessageTextComment(SystemMessageTextComment systemMessageTextComment, Context context) {


        String formattedMessage = "";

        GroupRepository groupRepository = InjectGroupDataSource.provideGroupRepository(context);

        List<User> users = groupRepository.getGroupFromMemory(systemMessageTextComment.getGroupUUID()).getUsers();

        try {

            JSONObject jsonObject = new JSONObject(systemMessageTextComment.getText());

            String op = jsonObject.optString("op");

            JSONArray value = jsonObject.optJSONArray("value");

            String creatorUserName = systemMessageTextComment.getCreateUserName(context);

            switch (op) {

                case "changeBoxName":

                    String oldName = value.getString(0);
                    String newName = value.getString(1);


                    mSystemMessageTextCommentBinding.getRoot().setVisibility(View.VISIBLE);

                    formattedMessage = context.getString(R.string.change_group_name_message, creatorUserName, newName);

                    mSystemMessageTextCommentBinding.systemMessageTextview.setText(formattedMessage);

                    break;

                case "addUser":

                    mSystemMessageTextCommentBinding.getRoot().setVisibility(View.VISIBLE);

                    StringBuilder addUserNameBuilder = new StringBuilder();

                    for (int i = 0; i < value.length(); i++) {

                        String userGUID = value.getString(i);

                        for (User user : users) {
                            if (user.getAssociatedWeChatGUID().equals(userGUID)) {
                                addUserNameBuilder.append(user.getUserName());
                                addUserNameBuilder.append(",");
                            }
                        }

                    }

                    int length = addUserNameBuilder.length();

                    String addUserName = addUserNameBuilder.toString().substring(0, length - 1);

                    formattedMessage = context.getString(R.string.add_user_message, creatorUserName, addUserName);

                    mSystemMessageTextCommentBinding.systemMessageTextview.setText(formattedMessage);

                    break;

                case "deleteUser":

                    mSystemMessageTextCommentBinding.getRoot().setVisibility(View.GONE);

                    break;
                case "createBox":

                    mSystemMessageTextCommentBinding.getRoot().setVisibility(View.VISIBLE);

                    mSystemMessageTextCommentBinding.systemMessageTextview.setText(context.getString(R.string.create_group_message));


                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
