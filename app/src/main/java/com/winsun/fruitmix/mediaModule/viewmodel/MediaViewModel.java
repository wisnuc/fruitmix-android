package com.winsun.fruitmix.mediaModule.viewmodel;

import com.winsun.fruitmix.mediaModule.model.Media;

/**
 * Created by Administrator on 2018/1/17.
 */

public class MediaViewModel {

    private Media mMedia;

    private boolean selected;
    private boolean loaded;

    public MediaViewModel(Media media) {
        mMedia = media;
    }

    public Media getMedia() {
        return mMedia;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
