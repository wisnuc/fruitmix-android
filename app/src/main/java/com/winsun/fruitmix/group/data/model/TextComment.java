package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/7/20.
 */

public class TextComment extends UserComment {

    private String text;

    protected TextComment(String uuid,User creator, long time,String groupUUID,String stationID) {
        super(uuid,creator, time,groupUUID,stationID);
    }

    public TextComment(String uuid,User creator, long time, String groupUUID,String stationID,String text) {
        super(uuid,creator, time,groupUUID,stationID);
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
