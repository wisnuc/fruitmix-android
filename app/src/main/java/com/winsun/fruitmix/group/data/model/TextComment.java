package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/7/20.
 */

public class TextComment extends UserComment {

    private String text;

    public TextComment(String uuid,User creator, long time) {
        super(uuid,creator, time);
    }

    public TextComment(String uuid,User creator, long time, String text) {
        super(uuid,creator, time);
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
