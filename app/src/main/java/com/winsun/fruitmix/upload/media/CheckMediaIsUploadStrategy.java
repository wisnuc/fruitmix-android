package com.winsun.fruitmix.upload.media;

import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/18.
 */

public class CheckMediaIsUploadStrategy {

    private String currentUserUUID;

    private List<String> uploadedMediaHashs;

    private static CheckMediaIsUploadStrategy instance;

    public static CheckMediaIsUploadStrategy getInstance() {
        if (instance == null)
            instance = new CheckMediaIsUploadStrategy();
        return instance;
    }

    public void setCurrentUserUUID(String currentUserUUID) {
        this.currentUserUUID = currentUserUUID;
    }

    public void setUploadedMediaHashs(List<String> uploadedMediaHashs) {
        this.uploadedMediaHashs = uploadedMediaHashs;
    }

    public boolean isMediaUploaded(Media media) {

        return uploadedMediaHashs != null && currentUserUUID != null && uploadedMediaHashs.contains(media.getUuid());

    }

    public void addUploadedMediaUUID(String mediaUUID){
        uploadedMediaHashs.add(mediaUUID);
    }


}
