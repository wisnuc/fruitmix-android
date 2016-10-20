package com.winsun.fruitmix.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2016/10/12.
 */

public class MediaShareContent implements Parcelable{

    private String id;
    private String author;
    private String digest;
    private String time;

    public MediaShareContent(){

    }

    private MediaShareContent(Parcel in) {
        author = in.readString();
        digest = in.readString();
        time = in.readString();
        id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(digest);
        dest.writeString(time);
        dest.writeString(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaShareContent> CREATOR = new Creator<MediaShareContent>() {
        @Override
        public MediaShareContent createFromParcel(Parcel in) {
            return new MediaShareContent(in);
        }

        @Override
        public MediaShareContent[] newArray(int size) {
            return new MediaShareContent[size];
        }
    };

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {

        if(this == o)
            return true;

        if(o instanceof MediaShareContent){
            return ((MediaShareContent) o).getDigest().equals(this.getDigest());
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return getDigest().hashCode();
    }
}
