package com.winsun.fruitmix.model;

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
    private String title;
    private boolean loaded;
    private String belongingMediaShareUUID;
    private boolean uploaded;
    private boolean sharing;
    private int orientationNumber;

    public Media() {

    }

    protected Media(Parcel in) {
        uuid = in.readString();
        thumb = in.readString();
        time = in.readString();
        width = in.readString();
        height = in.readString();
        selected = in.readByte() != 0;
        local = in.readByte() != 0;
        title = in.readString();
        loaded = in.readByte() != 0;
        belongingMediaShareUUID = in.readString();
        uploaded = in.readByte() != 0;
        orientationNumber = in.readInt();
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
        dest.writeString(title);
        dest.writeByte((byte) (loaded ? 1 : 0));
        dest.writeString(belongingMediaShareUUID);
        dest.writeByte((byte) (uploaded ? 1 : 0));
        dest.writeInt(orientationNumber);
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
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
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

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void restoreUploadState() {
        uploaded = false;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
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

    public synchronized boolean uploadIfNotDone(Context context) {

        DBUtils dbUtils = DBUtils.getInstance(context);

        if (LocalCache.RemoteMediaMapKeyIsUUID.containsKey(getUuid())) {

            Log.i(TAG, "upload file is already uploaded");

            uploaded = true;

            dbUtils.updateLocalMedia(this);
        }

        if (!uploaded) {
            uploaded = FNAS.UploadFile(thumb);

            Log.i(TAG, "upload file:" + thumb + "result:" + uploaded);

            if (uploaded) {
                dbUtils.updateLocalMedia(this);
            }

        }

        return uploaded;
    }

    public String getImageThumbUrl(Context context) {

        String imageUrl;
        if (isLocal()) {
            imageUrl = getThumb();
        } else {

            int width = Integer.parseInt(getWidth());
            int height = Integer.parseInt(getHeight());

            int[] result = Util.formatPhotoWidthHeight(width, height);

            imageUrl = String.format(context.getString(R.string.thumb_photo_url), FNAS.Gateway + ":" + FNAS.PORT + Util.MEDIA_PARAMETER + "/" + getUuid(),
                    String.valueOf(result[0]), String.valueOf(result[1]));
        }
        return imageUrl;
    }

    public String getImageOriginalUrl(Context context) {

        String imageUrl;
        if (isLocal()) {
            imageUrl = getThumb();
        } else {
            imageUrl = String.format(context.getString(R.string.original_photo_url), FNAS.Gateway + ":" + FNAS.PORT + Util.MEDIA_PARAMETER + "/" + getUuid());
        }
        return imageUrl;
    }
}
