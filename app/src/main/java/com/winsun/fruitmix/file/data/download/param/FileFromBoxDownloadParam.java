package com.winsun.fruitmix.file.data.download.param;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2018/1/26.
 */

public class FileFromBoxDownloadParam extends FileDownloadParam {

    private String boxUUID;

    private String stationID;

    private String fileHash;

    public FileFromBoxDownloadParam(String boxUUID,String stationID, String fileHash) {
        this.boxUUID = boxUUID;
        this.stationID = stationID;
        this.fileHash = fileHash;
    }

    public String getStationID() {
        return stationID;
    }

    @Override
    public String getFileDownloadPath() throws UnsupportedEncodingException {

        return "/boxes/" + boxUUID + "/files/" + fileHash;

    }

}
