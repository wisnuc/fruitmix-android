package com.winsun.fruitmix.upload.media.uploadMediaState;

/**
 * Created by Administrator on 2017/9/7.
 */

public class UploadMediaState {

    public static final int START_GET_UPLOAD_MEDIA_COUNT = 0x1001;
    public static final int UPLOAD_MEDIA_COUNT_CHANGED = 0x1002;
    public static final int GET_FOLDER_FAIL = 0x1003;
    public static final int CREATE_FOLDER_FAIL = 0x1004;
    public static final int UPLOAD_MEDIA_FAIL = 0x1005;
    public static final int GET_MEDIA_COUNT_FAIL = 0x1006;

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
