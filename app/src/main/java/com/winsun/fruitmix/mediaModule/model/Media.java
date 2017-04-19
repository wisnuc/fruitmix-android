package com.winsun.fruitmix.mediaModule.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2016/7/28.
 */
public class Media implements Parcelable {

    private static final String TAG = Media.class.getSimpleName();

    private String uuid;
    private String thumb;
    private String time;
    private String width;
    private String height;
    private boolean selected;
    private boolean local;
    private String date;
    private boolean loaded;
    private String belongingMediaShareUUID;
    private String uploadedDeviceIDs;
    private boolean sharing;
    private int orientationNumber;
    private String type;
    private String miniThumbPath;
    private String originalPhotoPath;

    public Media() {
        uuid = "";
        orientationNumber = 1;
        belongingMediaShareUUID = "";
        width = "200";
        height = "200";
        thumb = "";
        type = "JPEG";
        date = "";
        belongingMediaShareUUID = "";
        local = false;
        miniThumbPath = "";
        originalPhotoPath = "";
    }

    protected Media(Parcel in) {
        uuid = in.readString();
        thumb = in.readString();
        time = in.readString();
        width = in.readString();
        height = in.readString();
        selected = in.readByte() != 0;
        local = in.readByte() != 0;
        date = in.readString();
        loaded = in.readByte() != 0;
        belongingMediaShareUUID = in.readString();
        uploadedDeviceIDs = in.readString();
        orientationNumber = in.readInt();
        type = in.readString();
        miniThumbPath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(thumb);
        dest.writeString(time);
        dest.writeString(width);
        dest.writeString(height);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeByte((byte) (local ? 1 : 0));
        dest.writeString(date);
        dest.writeByte((byte) (loaded ? 1 : 0));
        dest.writeString(belongingMediaShareUUID);
        dest.writeString(uploadedDeviceIDs);
        dest.writeInt(orientationNumber);
        dest.writeString(type);
        dest.writeString(miniThumbPath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    public String getUuid() {
        return uuid == null ? "" : uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTime() {
        return time == null ? "" : time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getDate() {
        return date == null ? "" : date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getThumb() {
        return thumb == null ? "" : thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getBelongingMediaShareUUID() {
        return belongingMediaShareUUID == null ? "" : belongingMediaShareUUID;
    }

    public void setBelongingMediaShareUUID(String belongingMediaShareUUID) {
        this.belongingMediaShareUUID = belongingMediaShareUUID;
    }

    public String getUploadedDeviceIDs() {
        return uploadedDeviceIDs == null ? "" : uploadedDeviceIDs;
    }

    public void setUploadedDeviceIDs(String uploadedDeviceIDs) {
        this.uploadedDeviceIDs = uploadedDeviceIDs;
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

    public synchronized boolean uploadIfNotDone(DBUtils dbUtils) {

        boolean uploaded;

        if (LocalCache.RemoteMediaMapKeyIsUUID.containsKey(getUuid())) {

            Log.d(TAG, "upload file is already uploaded");
            uploaded = true;

        } else {
            uploaded = FNAS.UploadFile(originalPhotoPath);
        }

        if (!uploaded) return false;

        if (getUploadedDeviceIDs().isEmpty()) {
            setUploadedDeviceIDs(LocalCache.DeviceID);
        } else if (!getUploadedDeviceIDs().contains(LocalCache.DeviceID)) {
            setUploadedDeviceIDs(getUploadedDeviceIDs() + "," + LocalCache.DeviceID);
        }
        dbUtils.updateLocalMedia(this);

        return true;
    }

    public String getImageSmallThumbUrl(Context context) {

        String imageUrl;
        if (isLocal()) {
            imageUrl = getMiniThumbPath();

            if (imageUrl.isEmpty())
                imageUrl = getThumb();

            if(imageUrl.isEmpty()){
                imageUrl = getOriginalPhotoPath();
            }

        } else {

//            int[] result = Util.formatPhotoWidthHeight(width, height);

            imageUrl = String.format(context.getString(R.string.android_thumb_photo_url), FNAS.Gateway + ":" + FNAS.PORT + Util.MEDIA_PARAMETER + "/" + getUuid(),
                    String.valueOf(64), String.valueOf(64));

        }
        return imageUrl;

    }

    public String getImageThumbUrl(Context context) {

        String imageUrl;
        if (isLocal()) {
            imageUrl = getThumb();

            if(imageUrl.isEmpty()){
                imageUrl = getOriginalPhotoPath();
            }

        } else {

//            int[] result = Util.formatPhotoWidthHeight(width, height);

            imageUrl = String.format(context.getString(R.string.android_thumb_photo_url), FNAS.Gateway + ":" + FNAS.PORT + Util.MEDIA_PARAMETER + "/" + getUuid(),
                    String.valueOf(200), String.valueOf(200));

        }
        return imageUrl;

    }

    public String getImageOriginalUrl(Context context) {

        String imageUrl;
        if (isLocal()) {
            imageUrl = getOriginalPhotoPath();
        } else {
            imageUrl = String.format(context.getString(R.string.android_original_photo_url), FNAS.Gateway + ":" + FNAS.PORT + Util.MEDIA_PARAMETER + "/" + getUuid());
        }
        return imageUrl;
    }

    public String getKey() {
        if (isLocal())
            return getOriginalPhotoPath();
        else
            return getUuid();
    }
}
