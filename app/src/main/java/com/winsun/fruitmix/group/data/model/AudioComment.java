package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/8/14.
 */

public class AudioComment extends TextComment {

    private String audioRecordFilePath;
    private long audioRecordTime;

    public AudioComment(String uuid, User creator, long time,String groupUUID,String stationID, String audioRecordFilePath, long audioRecordTime) {
        super(uuid,creator, time,groupUUID,stationID);
        this.audioRecordFilePath = audioRecordFilePath;
        this.audioRecordTime = audioRecordTime;
    }

    public long getAudioRecordTime() {
        return audioRecordTime;
    }

    public String getAudioRecordFilePath() {
        return audioRecordFilePath;
    }
}
