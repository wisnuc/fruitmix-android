package com.winsun.fruitmix.group.data.model;

import android.content.Context;
import android.view.View;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class SystemMessageTextComment extends TextComment {

    public enum SystemMessageType {

        CREATE_GROUP, CHANGE_GROUP_NAME, ADD_USER, DELETE_USER, DEFAULT_MESSAGE

    }

    private SystemMessageType mSystemMessageType = SystemMessageType.DEFAULT_MESSAGE;

    private List<String> values = new ArrayList<>();

    private List<User> addOrDeleteUsers = new ArrayList<>();

    protected SystemMessageTextComment(String uuid, User creator, long time, String groupUUID, String stationID) {
        super(uuid, creator, time, groupUUID, stationID);
    }

    public SystemMessageTextComment(String uuid, User creator, long time, String groupUUID, String stationID, String text) {
        super(uuid, creator, time, groupUUID, stationID, text);

        formatText();

    }

    private void formatText() {

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(getText());

            String op = jsonObject.optString("op");

            JSONArray valueArray = jsonObject.optJSONArray("value");

            switch (op) {

                case "changeBoxName":

                    mSystemMessageType = SystemMessageType.CHANGE_GROUP_NAME;

                    break;

                case "addUser":

                    mSystemMessageType = SystemMessageType.ADD_USER;

                    break;

                case "deleteUser":

                    mSystemMessageType = SystemMessageType.DELETE_USER;

                    break;
                case "createBox":

                    mSystemMessageType = SystemMessageType.CREATE_GROUP;

                    break;
            }

            for (int i = 0; i < valueArray.length(); i++) {

                String value = valueArray.getString(i);

                values.add(value);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public SystemMessageType getSystemMessageType() {
        return mSystemMessageType;
    }

    public List<String> getValues() {
        return values;
    }

    public void fillAddOrDeleteUser(List<User> users) {

        for (String guid : values) {

            for (User user : users) {
                if (user.getAssociatedWeChatGUID().equals(guid)) {
                    addOrDeleteUsers.add(user);
                }
            }

        }

    }

    private List<User> getAddOrDeleteUsers() {
        return addOrDeleteUsers;
    }

    public boolean showMessage(){

        if (getSystemMessageType() != SystemMessageTextComment.SystemMessageType.DELETE_USER) {

            if (getSystemMessageType() == SystemMessageTextComment.SystemMessageType.ADD_USER) {

                List<User> users = getAddOrDeleteUsers();

                return users.size() == getValues().size();

            } else
                return true;

        }else
            return false;

    }


    public String getFormatMessage(Context context) {

        String creatorUserName = getCreateUserName(context);

        switch (getSystemMessageType()) {

            case CHANGE_GROUP_NAME:

                String oldName = values.get(0);
                String newName = values.get(1);

                return context.getString(R.string.change_group_name_message, creatorUserName, newName);


            case ADD_USER:

                StringBuilder addUserNameBuilder = new StringBuilder();

                for (User user : getAddOrDeleteUsers()) {
                    addUserNameBuilder.append(user.getUserName());
                    addUserNameBuilder.append(",");
                }

                int length = addUserNameBuilder.length();

                if (length == 0) {

                    return "";

                }

                String addUserName = addUserNameBuilder.toString().substring(0, length - 1);

                return context.getString(R.string.add_user_message, creatorUserName, addUserName);


            case DELETE_USER:

                return "";


            case CREATE_GROUP:

                return context.getString(R.string.create_group_message);

            default:
                return "";

        }

    }
}