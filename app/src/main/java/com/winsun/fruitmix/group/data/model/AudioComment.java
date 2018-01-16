package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/8/14.
 */

public class AudioComment extends TextComment {

    private String audioRecordFilePath;
    private long audioRecordTime;

    public AudioComment(User creator, long time, String audioRecordFilePath, long audioRecordTime) {
        super(creator, time);
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
