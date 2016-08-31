package com.winsun.fruitmix.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/7/16.
 */
public class Share implements Parcelable{

    private int id;
    private String uuid;
    private String creator;
    private String time;
    private String title;
    private String desc;
    private String digest;
    private String viewer;
    private String maintainer;
    private boolean isAlbum;

    public Share(int id, String uuid, String creator, String time, String title, String desc, String digest, String viewer, String maintainer, boolean isAlbum) {
        this.id = id;
        this.uuid = uuid;
        this.creator = creator;
        this.time = time;
        this.title = title;
        this.desc = desc;
        this.digest = digest;
        this.viewer = viewer;
        this.maintainer = maintainer;
        this.isAlbum = isAlbum;
    }

    public Share() {
    }

    protected Share(Parcel in) {
        id = in.readInt();
        uuid = in.readString();
        creator = in.readString();
        time = in.readString();
        title = in.readString();
        desc = in.readString();
        digest = in.readString();
        viewer = in.readString();
        maintainer = in.readString();
        isAlbum = in.readByte() != 0;
    }

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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getViewer() {
        return viewer;
    }

    public void setViewer(String viewer) {
        this.viewer = viewer;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public boolean isAlbum() {
        return isAlbum;
    }

    public void setAlbum(boolean album) {
        isAlbum = album;
    }

    @Override
    public String toString() {
        return "Share{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", creator='" + creator + '\'' +
                ", time='" + time + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", digest='" + digest + '\'' +
                ", viewer='" + viewer + '\'' +
                ", maintainer='" + maintainer + '\'' +
                ", isAlbum=" + isAlbum +
                '}';
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(uuid);
        dest.writeString(creator);
        dest.writeString(time);
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeString(digest);
        dest.writeString(viewer);
        dest.writeString(maintainer);
        dest.writeByte((byte) (isAlbum ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Share> CREATOR = new Creator<Share>() {
        @Override
        public Share createFromParcel(Parcel in) {
            return new Share(in);
        }

        @Override
        public Share[] newArray(int size) {
            return new Share[size];
        }
    };
}
