package com.winsun.fruitmix.model;

/**
 * Created by Administrator on 2016/7/16.
 */
public class Share {

    private int id;
    private String uuid;
    private String creator;
    private String mTime;
    private String title;
    private String desc;
    private String digest;
    private String viewer;
    private String maintainer;
    private boolean isAlbum;

    public Share(int id, String uuid, String creator, String mTime, String title, String desc, String digest, String viewer, String maintainer, boolean isAlbum) {
        this.id = id;
        this.uuid = uuid;
        this.creator = creator;
        this.mTime = mTime;
        this.title = title;
        this.desc = desc;
        this.digest = digest;
        this.viewer = viewer;
        this.maintainer = maintainer;
        this.isAlbum = isAlbum;
    }

    public Share() {
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

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
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
                ", mTime='" + mTime + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", digest='" + digest + '\'' +
                ", viewer='" + viewer + '\'' +
                ", maintainer='" + maintainer + '\'' +
                ", isAlbum=" + isAlbum +
                '}';
    }
}
