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

    public String getDate(Context context) {

        return formatTime(context, time);

    }

    private String formatTime(Context context, long time) {

        if (date == null) {

            date = Util.formatTime(context, time);

            return date;
        } else
            return date;

    }

}
