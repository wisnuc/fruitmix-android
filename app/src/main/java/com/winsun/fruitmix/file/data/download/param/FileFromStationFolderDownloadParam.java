package com.winsun.fruitmix.file.data.download.param;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Administrator on 2018/1/26.
 */

public class FileFromStationFolderDownloadParam extends FileDownloadParam {

    private static final String ROOT_DRIVE_PARAMETER = "/drives";

    private static final String DIRS = "/dirs/";

    private String fileParentFolderUUID;

    private String driveUUID;

    private String fileName;

    private String fileUUID;

    public FileFromStationFolderDownloadParam(String fileUUID, String fileParentFolderUUID, String driveUUID, String fileName) {
        this.fileUUID = fileUUID;
        this.fileParentFolderUUID = fileParentFolderUUID;
        this.driveUUID = driveUUID;
        this.fileName = fileName;
    }

    @Override
    public String getFileDownloadPath() throws UnsupportedEncodingException {

        String encodedFileName = URLEncoder.encode(fileName, "UTF-8");

        return ROOT_DRIVE_PARAMETER + "/"
                + driveUUID + DIRS + fileParentFolderUUID
                + "/entries/" + fileUUID + "?name=" + encodedFileName;

    }

}
