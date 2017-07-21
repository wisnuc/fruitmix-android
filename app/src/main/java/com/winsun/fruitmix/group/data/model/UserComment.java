package com.winsun.fruitmix.group.data.model;

import android.content.Context;
import android.widget.TextView;

import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/7/20.
 */

public class UserComment {

    private User creator;

    private long time;

    private String date;

    public UserComment(User creator, long time) {
        this.creator = creator;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public User getCreator() {
        return creator;
    }

    public String getDate() {

        return formatTime(time);

    }

    private String formatTime(long time) {

        if (date == null) {
            return "刚刚";
        } else
            return date;

    }

}
