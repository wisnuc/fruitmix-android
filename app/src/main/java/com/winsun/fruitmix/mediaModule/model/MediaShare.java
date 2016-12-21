package com.winsun.fruitmix.mediaModule.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.winsun.fruitmix.util.FNAS;
import com.winsun.fruitmix.util.LocalCache;
import com.winsun.fruitmix.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2016/7/16.
 */
public class MediaShare implements Parcelable {

    private int id;
    private String uuid;
    private String creatorUUID;
    private String time;
    private String title;
    private String desc;
    private List<MediaShareContent> mediaShareContents;
    private List<String> viewers;
    private List<String> maintainers;
    private boolean isAlbum;
    private boolean isArchived;
    private String date;
    private String coverImageKey;
    private boolean isLocal;
    private String shareDigest;
    private boolean isSticky;

    public MediaShare() {
        mediaShareContents = new ArrayList<>();
        viewers = new ArrayList<>();
        maintainers = new ArrayList<>();

        shareDigest = "";
    }

    protected MediaShare(Parcel in) {
        id = in.readInt();
        uuid = in.readString();
        creatorUUID = in.readString();
        time = in.readString();
        title = in.readString();
        desc = in.readString();
        mediaShareContents = in.createTypedArrayList(MediaShareContent.CREATOR);
        viewers = in.createStringArrayList();
        maintainers = in.createStringArrayList();
        isAlbum = in.readByte() != 0;
        isArchived = in.readByte() != 0;
        date = in.readString();
        coverImageKey = in.readString();
        isLocal = in.readByte() != 0;
        shareDigest = in.readString();
        isSticky = in.readByte() != 0;
    }

    public String createToggleShareStateRequestData() {
        String requestData;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");

        if (getViewersListSize() == 0) {

            for (String userUUID : LocalCache.RemoteUserMapKeyIsUUID.keySet()) {
                addViewer(userUUID);
            }

            stringBuilder.append(createStringOperateViewersInMediaShare(Util.ADD));

        } else {

            stringBuilder.append(createStringOperateViewersInMediaShare(Util.DELETE));

            clearViewers();
        }

        stringBuilder.append("]");
        requestData = stringBuilder.toString();
        return requestData;
    }

    public String createStringOperateViewersInMediaShare(String op) {

        String returnValue;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"op\":\"");
        stringBuilder.append(op);
        stringBuilder.append("\",\"path\":\"");
        stringBuilder.append("viewers");
        stringBuilder.append("\",\"value\":[");
        for (String value : viewers) {
            stringBuilder.append("\"");
            stringBuilder.append(value);
            stringBuilder.append("\",");
        }

        if (viewers.size() > 0) {
            returnValue = stringBuilder.substring(0, stringBuilder.length() - 1);
        } else {
            returnValue = stringBuilder.toString();
        }

        returnValue += "]}";

        return returnValue;
    }

    public String createStringOperateMaintainersInMediaShare(String op) {

        String returnValue;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"op\":\"");
        stringBuilder.append(op);
        stringBuilder.append("\",\"path\":\"");
        stringBuilder.append("maintainers");
        stringBuilder.append("\",\"value\":[");
        for (String value : maintainers) {
            stringBuilder.append("\"");
            stringBuilder.append(value);
            stringBuilder.append("\",");
        }

        if (maintainers.size() > 0) {
            returnValue = stringBuilder.substring(0, stringBuilder.length() - 1);
        } else {
            returnValue = stringBuilder.toString();
        }

        returnValue += "]}";

        return returnValue;
    }

    public String createStringOperateContentsInMediaShare(String op) {

        String returnValue;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"op\":\"");
        stringBuilder.append(op);
        stringBuilder.append("\",\"path\":\"");
        stringBuilder.append("contents");
        stringBuilder.append("\",\"value\":[");
        for (MediaShareContent value : mediaShareContents) {
            stringBuilder.append("\"");

            String key = value.getKey();
            if (key.contains("/"))
                key = Util.CalcSHA256OfFile(key);

            stringBuilder.append(key);
            stringBuilder.append("\",");
        }

        if (mediaShareContents.size() > 0) {
            returnValue = stringBuilder.substring(0, stringBuilder.length() - 1);
        } else {
            returnValue = stringBuilder.toString();
        }

        returnValue += "]}";

        return returnValue;
    }

    public String createStringReplaceTitleTextAboutMediaShare() {
        return "{\"op\":\"replace\",\"path\":\"album\",\"value\":{" +
                "\"title\":\"" +
                title +
                "\",\"text\":\"" +
                desc +
                "\"}}";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(uuid);
        dest.writeString(creatorUUID);
        dest.writeString(time);
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeTypedList(mediaShareContents);
        dest.writeStringList(viewers);
        dest.writeStringList(maintainers);
        dest.writeByte((byte) (isAlbum ? 1 : 0));
        dest.writeByte((byte) (isArchived ? 1 : 0));
        dest.writeString(date);
        dest.writeString(coverImageKey);
        dest.writeByte((byte) (isLocal ? 1 : 0));
        dest.writeString(shareDigest);
        dest.writeByte((byte) (isSticky ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaShare> CREATOR = new Creator<MediaShare>() {
        @Override
        public MediaShare createFromParcel(Parcel in) {
            return new MediaShare(in);
        }

        @Override
        public MediaShare[] newArray(int size) {
            return new MediaShare[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCreatorUUID() {
        return creatorUUID;
    }

    public void setCreatorUUID(String creatorUUID) {
        this.creatorUUID = creatorUUID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCoverImageKey() {
        return coverImageKey;
    }

    public void setCoverImageKey(String coverImageKey) {
        this.coverImageKey = coverImageKey;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public boolean isAlbum() {
        return isAlbum;
    }

    public void setAlbum(boolean album) {
        isAlbum = album;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getShareDigest() {
        return shareDigest;
    }

    public void setShareDigest(String shareDigest) {
        this.shareDigest = shareDigest;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public void setSticky(boolean sticky) {
        this.isSticky = sticky;
    }

    public MediaShare cloneMyself() {
        MediaShare cloneMediaShare = new MediaShare();
        cloneMediaShare.setUuid(getUuid());
        cloneMediaShare.setCreatorUUID(getCreatorUUID());
        cloneMediaShare.setTime(getTime());
        cloneMediaShare.setTitle(getTitle());
        cloneMediaShare.setDesc(getDesc());
        cloneMediaShare.initMediaShareContents(getMediaShareContents());
        cloneMediaShare.initViewers(getViewers());
        cloneMediaShare.initMaintainers(getMaintainers());
        cloneMediaShare.setAlbum(isAlbum());
        cloneMediaShare.setArchived(isArchived());
        cloneMediaShare.setDate(getDate());
        cloneMediaShare.setCoverImageKey(getCoverImageKey());
        cloneMediaShare.setLocal(isLocal());
        cloneMediaShare.setShareDigest(getShareDigest());
        cloneMediaShare.setSticky(isSticky());

        return cloneMediaShare;
    }

    public int getViewersListSize() {
        return viewers.size();
    }

    public boolean checkMaintainersListContainCurrentUserUUID() {
        return maintainers.contains(FNAS.userUUID);
    }

    public int getMediaContentsListSize() {
        return mediaShareContents.size();
    }

    public String getFirstMediaDigestInMediaContentsList() {
        return mediaShareContents.get(0).getKey();
    }

    public List<String> getViewers() {
        return Collections.unmodifiableList(viewers);
    }

    public List<String> getMaintainers() {
        return Collections.unmodifiableList(maintainers);
    }

    public List<MediaShareContent> getMediaShareContents() {
        return Collections.unmodifiableList(mediaShareContents);
    }

    public List<String> getMediaKeyInMediaShareContents() {
        List<String> mediaKeys = new ArrayList<>(getMediaContentsListSize());
        for (MediaShareContent mediaShareContent : mediaShareContents) {
            mediaKeys.add(mediaShareContent.getKey());
        }

        return mediaKeys;
    }

    public void initMediaShareContents(List<MediaShareContent> mediaShareContents) {
        this.mediaShareContents.addAll(mediaShareContents);
    }

    public void initViewers(List<String> viewers) {
        this.viewers.addAll(viewers);
    }

    public void initMaintainers(List<String> maintainers) {
        this.maintainers.addAll(maintainers);
    }

    public void addViewer(String viewer) {
        viewers.add(viewer);
    }

    public void removeViewer(String viewer) {
        viewers.remove(viewer);
    }

    public void addMaintainer(String maintainer) {
        maintainers.add(maintainer);
    }

    public void removeMaintainer(String maintainer) {
        maintainers.remove(maintainer);
    }

    public void addMediaShareContent(MediaShareContent mediaShareContent) {
        mediaShareContents.add(mediaShareContent);
    }

    public void removeMediaShareContent(MediaShareContent mediaShareContent) {
        mediaShareContents.remove(mediaShareContent);
    }

    public void removeMediaShareContent(int position) {
        mediaShareContents.remove(position);
    }

    public void clearViewers() {
        viewers.clear();
    }

    public void clearMaintainers() {
        maintainers.clear();
    }

    public void clearMediaShareContents() {
        mediaShareContents.clear();
    }

    public List<MediaShareContent> getDifferentMediaShareContentInCurrentMediaShare(MediaShare originalMediaShare) {

        List<MediaShareContent> mediaShareContents = new ArrayList<>(getMediaContentsListSize());

        mediaShareContents.addAll(getMediaShareContents());

        mediaShareContents.removeAll(originalMediaShare.getMediaShareContents());

        return mediaShareContents;
    }

    public void sendModifyMediaShareRequest(Context context, String requestData) {
        if (Util.getNetworkState(context)) {
            FNAS.modifyRemoteMediaShare(context, this, requestData);
        } else {
            FNAS.modifyLocalMediaShare(context, this, requestData);
        }
    }

    public void sendDeleteMediaShareRequest(Context context) {
        if (Util.getNetworkState(context)) {
            FNAS.deleteRemoteMediaShare(context, this);
        } else {
            FNAS.deleteLocalMediaShare(context, this);
        }
    }

    public boolean checkPermissionToOperate() {
        return checkMaintainersListContainCurrentUserUUID() || getCreatorUUID().equals(FNAS.userUUID);
    }

}
