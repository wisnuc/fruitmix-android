package com.winsun.fruitmix.group.usecase;

import android.media.MediaRecorder;

import com.winsun.fruitmix.util.FileUtil;

import java.io.File;
import java.io.IOException;


/**
 * Created by Administrator on 2017/8/14.
 */

public class RecordAudioUseCaseImpl implements RecordAudioUseCase {

    private MediaRecorder mediaRecorder;

    public static RecordAudioUseCase instance;

    private String filePath;

    private long startTime;
    private long endTime;

    public static RecordAudioUseCase getInstance() {
        if (instance == null)
            instance = new RecordAudioUseCaseImpl();
        return instance;
    }

    private RecordAudioUseCaseImpl() {
    }

    @Override
    public void startAudioRecord() {

        if (mediaRecorder == null)
            mediaRecorder = new MediaRecorder();

        startTime = System.currentTimeMillis();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        filePath = FileUtil.getAudioRecordFolderPath() + File.separator + System.currentTimeMillis() + ".3gp";

        mediaRecorder.setOutputFile(filePath);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();

    }

    @Override
    public void stopAudioRecord() {

        if (mediaRecorder == null)
            return;

        endTime = System.currentTimeMillis();

        mediaRecorder.stop();
        mediaRecorder.release();

        mediaRecorder = null;

    }

    @Override
    public String getAudioRecordFilePath() {
        return filePath;
    }

    @Override
    public long getAudioRecordTime() {
        return endTime - startTime;
    }
}
