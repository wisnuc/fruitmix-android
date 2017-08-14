package com.winsun.fruitmix.group.data.model;

import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.mediaModule.model.Media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/26.
 */

public class Pin {

    private String uuid;

    private String name;

    private List<AbstractFile> files;

    private List<Media> medias;

    public Pin(String uuid, String name) {
        this.name = name;
        this.uuid = uuid;

        files = new ArrayList<>();

        medias = new ArrayList<>();
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMedias(Collection<Media> newMedias) {
        medias.addAll(newMedias);
    }

    public void addMedia(Media newMedia) {
        medias.add(newMedia);
    }

    public void removeMedia(Media media){
        medias.remove(media);
    }

    public void clearMedia() {
        medias.clear();
    }

    public void addFiles(Collection<AbstractFile> newFiles) {
        files.addAll(newFiles);
    }

    public void addFile(AbstractFile file) {

        files.add(file);

    }

    public void removeFile(AbstractFile file){
        files.remove(file);
    }

    public void clearFile() {
        files.clear();
    }

    public List<Media> getMedias() {
        return medias;
    }

    public List<AbstractFile> getFiles() {
        return files;
    }

    public Pin cloneSelf() {

        Pin pin = new Pin(getUuid(), getName());

        pin.addFiles(getFiles());
        pin.addMedias(getMedias());

        return pin;

    }

}
