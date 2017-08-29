package com.winsun.fruitmix.upload.media;

/**
 * Created by Administrator on 2017/8/23.
 */

public interface UploadMediaCountChangeListener {

    void onUploadMediaCountChanged(int uploadedMediaCount, int totalCount);

    void onUploadMediaFail(int httpErrorCode);

    void onGetUploadMediaCountFail(int httpErrorCode);

    void onCreateFolderFail(int httpErrorCode);

    void onGetFolderFail(int httpErrorCode);

}
