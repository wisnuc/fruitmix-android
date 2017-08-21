package com.winsun.fruitmix.upload.media;

import com.winsun.fruitmix.mediaModule.model.Media;

/**
 * Created by Administrator on 2017/8/18.
 */

public class CheckMediaIsUploadStrategy {

    private String currentUserUUID;

    private static CheckMediaIsUploadStrategy instance;

    public static CheckMediaIsUploadStrategy getInstance() {
        if (instance == null)
            instance = new CheckMediaIsUploadStrategy();
        return instance;
    }

    public void setCurrentUserUUID(String currentUserUUID) {
        this.currentUserUUID = currentUserUUID;
    }

    public boolean isMediaUploaded(Media media) {

        return currentUserUUID != null && media.getUploadedUserUUIDs().contains(currentUserUUID);

    }


}
