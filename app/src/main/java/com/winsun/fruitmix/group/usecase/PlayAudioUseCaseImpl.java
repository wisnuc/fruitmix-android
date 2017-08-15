package com.winsun.fruitmix.group.usecase;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by Administrator on 2017/8/14.
 */

public class PlayAudioUseCaseImpl implements PlayAudioUseCase {

    private MediaPlayer mediaPlayer;

    private static PlayAudioUseCase instance;

    public static PlayAudioUseCase getInstance() {
        if (instance == null)
            instance = new PlayAudioUseCaseImpl();
        return instance;
    }

    private PlayAudioUseCaseImpl() {
    }

    @Override
    public void startPlayAudio(String fileName) {

        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stopPlayAudio() {

        if (mediaPlayer == null)
            return;

        mediaPlayer.release();
        mediaPlayer = null;

    }
}
