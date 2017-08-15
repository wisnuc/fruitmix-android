package com.winsun.fruitmix.group.usecase;

/**
 * Created by Administrator on 2017/8/14.
 */

public interface RecordAudioUseCase {

    void startAudioRecord();

    void stopAudioRecord();

    String getAudioRecordFilePath();

    long getAudioRecordTime();
}
