package com.winsun.fruitmix.file.data.download.param;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2018/1/26.
 */

public class FileFromBoxDownloadParam extends FileDownloadParam {

    private String boxUUID;

    private String stationID;

    private String fileHash;

    private String cloudToken;

    public FileFromBoxDownloadParam(String boxUUID,String stationID, String fileHash, String cloudToken) {
        this.boxUUID = boxUUID;
        this.stationID = stationID;
        this.fileHash = fileHash;
        this.cloudToken = cloudToken;
    }

    public String getStationID() {
        return stationID;
    }

    public String getCloudToken() {
        return cloudToken;
    }

    @Override
    public String getFileDownloadPath() throws UnsupportedEncodingException {

        return "/boxes/" + boxUUID + "/files/" + fileHash;

    }
}
