package com.winsun.fruitmix.token.param;

/**
 * Created by Administrator on 2018/3/1.
 */

public class SCloudTokenParam implements TokenParam {

    private String currentUserGUID;

    public SCloudTokenParam(String currentUserGUID) {
        this.currentUserGUID = currentUserGUID;
    }

    public String getCurrentUserGUID() {
        return currentUserGUID;
    }
}
