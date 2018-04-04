package com.winsun.fruitmix.mediaModule.model;

import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.source.GroupRequestParam;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2016/7/28.
 */
public class Media extends AbstractFile {

    private static final String TAG = Media.class.getSimpleName();

    private static final String THUMB_PHOTO_FORMAT_CODE = "%1$s?alt=thumbnail&width=%2$s&height=%3$s&autoOrient=true&modifier=caret";
    private static final String THUMB_PHOTO_FORMAT_CODE_WITHOUT_WIDTH = "%1$s?alt=thumbnail&height=%2$s&autoOrient=true&modifier=caret";
    private static final String THUMB_PHOTO_FORMAT_CODE_WITHOUT_HEIGHT = "%1$s?alt=thumbnail&width=%2$s&autoOrient=true&modifier=caret";
    public static final String ALT_DATA = "?alt=data";
    public static final String BOX_UUID = "&boxUUID=";

    private String uuid;
    private String thumb;
    private String formattedTime;
    private String width;
    private String height;
    private boolean local;
    private String dateWithoutHourMinSec;
    private String belongingMediaShareUUID;
    private String uploadedUserUUIDs;
    private boolean sharing;
    private int orientationNumber;
    private String type;
    private String miniThumbPath;
    private String originalPhotoPath;

    private String longitude;
    private String latitude;

    private Address address;

    private long size;

    public Media() {
        uuid = "";
        orientationNumber = 1;
        belongingMediaShareUUID = "";
        width = "";
        height = "";
        thumb = "";
        type = "JPEG";
        dateWithoutHourMinSec = "";
        local = false;
        miniThumbPath = "";
        originalPhotoPath = "";

        longitude = "";
        latitude = "";

        setFileTypeResID(R.drawable.file_icon);

    }

    public String getUuid() {
        return uuid == null ? "" : uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFormattedTime() {
        return formattedTime == null ? "" : formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public String getWidth() {
        return width == null ? "" : width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height == null ? "" : height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getDateWithoutHourMinSec() {
        return dateWithoutHourMinSec == null ? "" : dateWithoutHourMinSec;
    }

    public void setDateWithoutHourMinSec(String date) {
        this.dateWithoutHourMinSec = date;
    }

    public String getThumb() {
        return thumb == null ? "" : thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getBelongingMediaShareUUID() {
        return belongingMediaShareUUID == null ? "" : belongingMediaShareUUID;
    }

    public void setBelongingMediaShareUUID(String belongingMediaShareUUID) {
        this.belongingMediaShareUUID = belongingMediaShareUUID;
    }

    public String getUploadedUserUUIDs() {
        return uploadedUserUUIDs == null ? "" : uploadedUserUUIDs;
    }

    public void setUploadedUserUUIDs(String uploadedUserUUIDs) {
        this.uploadedUserUUIDs = uploadedUserUUIDs;
    }

    public boolean isSharing() {
        return sharing;
    }

    public void setSharing(boolean sharing) {
        this.sharing = sharing;
    }

    public int getOrientationNumber() {
        return orientationNumber;
    }

    public void setOrientationNumber(int orientationNumber) {
        this.orientationNumber = orientationNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMiniThumbPath() {
        return miniThumbPath == null ? "" : miniThumbPath;
    }

    public void setMiniThumbPath(String miniThumbPath) {
        this.miniThumbPath = miniThumbPath;
    }

    public String getOriginalPhotoPath() {

        return originalPhotoPath == null ? "" : originalPhotoPath;

    }

    public void setOriginalPhotoPath(String originalPhotoPath) {
        if (originalPhotoPath == null)
            this.originalPhotoPath = "";
        else
            this.originalPhotoPath = originalPhotoPath;
    }

    public String getLatitude() {
        return latitude == null ? "" : latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude == null ? "" : longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public HttpRequest getImageSmallThumbUrl(HttpRequestFactory httpRequestFactory) {

        return actualGetImageSmallThumbUrl(httpRequestFactory, null, "");

    }

    public HttpRequest getImageSmallThumbUrl(HttpRequestFactory httpRequestFactory, GroupRequestParam groupRequestParam, String sCloudToken) {

        HttpRequest httpRequest = actualGetImageSmallThumbUrl(httpRequestFactory, groupRequestParam, sCloudToken);

        return addGroupUUIDToUrl(groupRequestParam.getGroupUUID(), httpRequest);

    }

    private HttpRequest actualGetImageSmallThumbUrl(HttpRequestFactory httpRequestFactory, GroupRequestParam groupRequestParam, String sCloudToken) {

        String imageUrl;

        HttpRequest httpRequest;

        if (isLocal()) {
            imageUrl = getMiniThumbPath();

            if (imageUrl.isEmpty())
                imageUrl = getThumb();

            if (imageUrl.isEmpty()) {
                imageUrl = getOriginalPhotoPath();
            }


            httpRequest = httpRequestFactory.createHttpGetRequestForLocalMedia(imageUrl);

        } else {

//            int[] result = Util.formatPhotoWidthHeight(width, height);

            String httpPath = getRemoteMediaThumbHttpPath(64, 64);

            if (groupRequestParam == null)
                httpRequest = generateUrl(httpRequestFactory, httpPath);
            else
                httpRequest = generateUrl(httpRequestFactory, httpPath, groupRequestParam, sCloudToken);

            Log.d(TAG, "media uuid: " + getUuid() + " getImageSmallThumbUrl: " + httpRequest.getUrl());

        }
        return httpRequest;

    }


    public HttpRequest getImageThumbUrl(HttpRequestFactory httpRequestFactory, GroupRequestParam groupRequestParam, String sCloudToken) {

        return getImageThumbUrl(httpRequestFactory, 200, 200, groupRequestParam, sCloudToken);

    }

    public HttpRequest getImageThumbUrl(HttpRequestFactory httpRequestFactory, int width, int height, GroupRequestParam groupRequestParam, String sCloudToken) {

        HttpRequest httpRequest = actualGetImageThumbUrl(httpRequestFactory, width, height, groupRequestParam, sCloudToken);

        return addGroupUUIDToUrl(groupRequestParam.getGroupUUID(), httpRequest);

    }

    public HttpRequest getImageThumbUrl(HttpRequestFactory httpRequestFactory) {

        return getImageThumbUrl(httpRequestFactory, 200, 200);

    }

    public HttpRequest getImageThumbUrl(HttpRequestFactory httpRequestFactory, int width, int height) {

        return actualGetImageThumbUrl(httpRequestFactory, width, height, null, "");

    }

    private HttpRequest actualGetImageThumbUrl(HttpRequestFactory httpRequestFactory, int width, int height,
                                               GroupRequestParam groupRequestParam, String sCloudToken) {

        String imageUrl;

        HttpRequest httpRequest;

        if (isLocal()) {
            imageUrl = getThumb();

            if (imageUrl.isEmpty()) {
                imageUrl = getOriginalPhotoPath();
            }

            httpRequest = httpRequestFactory.createHttpGetRequestForLocalMedia(imageUrl);

        } else {

//            int[] result = Util.formatPhotoWidthHeight(width, height);

            String httpPath = getRemoteMediaThumbHttpPath(width, height);

            if (groupRequestParam == null)
                httpRequest = generateUrl(httpRequestFactory, httpPath);
            else
                httpRequest = generateUrl(httpRequestFactory, httpPath, groupRequestParam, sCloudToken);

            Log.d(TAG, "media uuid: " + getUuid() + " getImageThumbUrl: " + httpRequest.getUrl());

        }

        return httpRequest;

    }


    private HttpRequest generateUrl(HttpRequestFactory httpRequestFactory, String req) {

        return httpRequestFactory.createHttpGetFileRequest(req);

    }

    private HttpRequest generateUrl(HttpRequestFactory httpRequestFactory, String req,
                                    GroupRequestParam groupRequestParam, String sCloudToken) {

        return httpRequestFactory.createHttpGetFileRequest(req, groupRequestParam.getGroupUUID(),
                groupRequestParam.getStationID());

    }


    private String getRemoteMediaThumbHttpPath(int width, int height) {

        String httpPath;

        if (width == -1) {
            httpPath = String.format(THUMB_PHOTO_FORMAT_CODE_WITHOUT_WIDTH, Util.MEDIA_PARAMETER + "/" + getUuid(),
                    String.valueOf(height));
        } else if (height == -1) {
            httpPath = String.format(THUMB_PHOTO_FORMAT_CODE_WITHOUT_HEIGHT, Util.MEDIA_PARAMETER + "/" + getUuid(),
                    String.valueOf(width));
        } else {
            httpPath = String.format(THUMB_PHOTO_FORMAT_CODE, Util.MEDIA_PARAMETER + "/" + getUuid(),
                    String.valueOf(width), String.valueOf(height));
        }

        return httpPath;

    }

    public HttpRequest getImageOriginalUrl(HttpRequestFactory httpRequestFactory) {

        return actualGetImageOriginalUrl(httpRequestFactory, null, "");
    }

    public HttpRequest getImageOriginalUrl(HttpRequestFactory httpRequestFactory, GroupRequestParam groupRequestParam, String sCloudToken) {

        HttpRequest httpRequest = actualGetImageOriginalUrl(httpRequestFactory, groupRequestParam, sCloudToken);

        return addGroupUUIDToUrl(groupRequestParam.getGroupUUID(), httpRequest);
    }

    private HttpRequest actualGetImageOriginalUrl(HttpRequestFactory httpRequestFactory, GroupRequestParam groupRequestParam, String sCloudToken) {

        String imageUrl;

        HttpRequest httpRequest;

        if (isLocal()) {
            imageUrl = getOriginalPhotoPath();

            httpRequest = httpRequestFactory.createHttpGetRequestForLocalMedia(imageUrl);

        } else {

            if (groupRequestParam == null)
                httpRequest = generateUrl(httpRequestFactory, getRemoteMediaRequestPath());
            else
                httpRequest = generateUrl(httpRequestFactory, getRemoteMediaRequestPath(), groupRequestParam, sCloudToken);
        }

        Log.d(TAG, "media uuid: " + getUuid() + " getImageOriginalUrl: " + httpRequest.getUrl());

        return httpRequest;

    }

    private String getRemoteMediaRequestPath() {
        return Util.MEDIA_PARAMETER + "/" + getUuid() + ALT_DATA;
    }


    private HttpRequest addGroupUUIDToUrl(String groupUUID, HttpRequest httpRequest) {
        if (!isLocal() && groupUUID.length() > 1)
            httpRequest.setUrl(httpRequest.getUrl() + BOX_UUID + groupUUID);

        Log.d(TAG, "addGroupUUIDToUrl: url: " + httpRequest.getUrl());

        return httpRequest;

    }

    public String getKey() {
        if (isLocal())
            return getOriginalPhotoPath();
        else
            return getUuid();
    }

    @Override
    public String toString() {
        return "uuid: " + getUuid() + " path: " + getOriginalPhotoPath() + " width: " + getWidth()
                + " height: " + getHeight() + " formattedTime: " + getFormattedTime() + " orientationNumber: " + getOrientationNumber();
    }

    @Override
    public boolean isFolder() {
        return false;
    }
}
