package com.winsun.fruitmix.mediaModule.model;

import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.http.request.factory.HttpRequestFactory;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
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

    public HttpRequest getImageSmallThumbUrl(Context context) {

        String imageUrl;

        HttpRequest httpRequest;

        if (isLocal()) {
            imageUrl = getMiniThumbPath();

            if (imageUrl.isEmpty())
                imageUrl = getThumb();

            if (imageUrl.isEmpty()) {
                imageUrl = getOriginalPhotoPath();
            }

            HttpRequestFactory httpRequestFactory = InjectHttp.provideHttpRequestFactory(context);
            httpRequest = httpRequestFactory.createHttpGetRequestForLocalMedia(imageUrl);

        } else {

//            int[] result = Util.formatPhotoWidthHeight(width, height);

            httpRequest = getRemoteMediaThumbUrl(context, 64, 64);

            Log.d(TAG, "media uuid: " + getUuid() + " getImageSmallThumbUrl: " + httpRequest.getUrl());

        }
        return httpRequest;

    }

    public HttpRequest getImageThumbUrl(Context context, String groupUUID) {

        HttpRequest httpRequest = getImageThumbUrl(context);

        return addGroupUUIDToUrl(groupUUID, httpRequest);
    }


    public HttpRequest getImageThumbUrl(Context context) {

        return getImageThumbUrl(context, 200, 200);

    }

    public HttpRequest getImageThumbUrl(Context context, int width, int height) {

        String imageUrl;

        HttpRequest httpRequest;

        if (isLocal()) {
            imageUrl = getThumb();

            if (imageUrl.isEmpty()) {
                imageUrl = getOriginalPhotoPath();
            }

            HttpRequestFactory httpRequestFactory = InjectHttp.provideHttpRequestFactory(context);
            httpRequest = httpRequestFactory.createHttpGetRequestForLocalMedia(imageUrl);

        } else {

//            int[] result = Util.formatPhotoWidthHeight(width, height);

            httpRequest = getRemoteMediaThumbUrl(context, width, height);

            Log.d(TAG, "media uuid: " + getUuid() + " getImageThumbUrl: " + httpRequest.getUrl());

        }
        return httpRequest;

    }


    private HttpRequest generateUrl(Context context, String req) {

        HttpRequestFactory httpRequestFactory = InjectHttp.provideHttpRequestFactory(context);

        return httpRequestFactory.createHttpGetFileRequest(req);

    }

    private HttpRequest getRemoteMediaThumbUrl(Context context, int width, int height) {

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

        return generateUrl(context, httpPath);

    }

    private HttpRequest getRemoteMediaOriginalUrl(Context context) {

        return generateUrl(context, getRemoteMediaRequestPath());

    }

    public String getRemoteMediaRequestPath() {
        return Util.MEDIA_PARAMETER + "/" + getUuid() + ALT_DATA;

    }

    public HttpRequest getImageOriginalUrl(Context context) {

        String imageUrl;

        HttpRequest httpRequest;

        if (isLocal()) {
            imageUrl = getOriginalPhotoPath();

            HttpRequestFactory httpRequestFactory = InjectHttp.provideHttpRequestFactory(context);
            httpRequest = httpRequestFactory.createHttpGetRequestForLocalMedia(imageUrl);

        } else {

            httpRequest = getRemoteMediaOriginalUrl(context);
        }

        Log.d(TAG, "media uuid: " + getUuid() + " getImageOriginalUrl: " + httpRequest.getUrl());

        return httpRequest;

    }

    public HttpRequest getImageOriginalUrl(Context context, String groupUUID) {

        HttpRequest httpRequest = getImageOriginalUrl(context);

        return addGroupUUIDToUrl(groupUUID, httpRequest);
    }

    private HttpRequest addGroupUUIDToUrl(String groupUUID, HttpRequest httpRequest) {
        if (!isLocal() && groupUUID.length() > 1)
            httpRequest.setUrl(httpRequest.getUrl() + BOX_UUID + groupUUID);

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
