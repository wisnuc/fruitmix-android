package com.winsun.fruitmix.util;

import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.mediaModule.model.Media;

/**
 * Created by Administrator on 2017/9/20.
 */

public class MediaUtil {

    public static void startLoadRemoteImageUrl(String url, NetworkImageView networkImageView, ImageLoader imageLoader) {

        networkImageView.setDefaultImageResId(R.drawable.default_place_holder);

        if (url != null && !url.isEmpty()) {

            imageLoader.setShouldCache(true);

            networkImageView.setTag(url);

            networkImageView.setImageUrl(url, imageLoader);
        }

    }

    public static void setMediaImageUrl(Media media, NetworkImageView networkImageView, HttpRequest httpRequest, ImageLoader imageLoader) {

        networkImageView.setDefaultImageResId(R.drawable.default_place_holder);

        imageLoader.setShouldCache(!media.isLocal());

        if (media.isLocal())
            networkImageView.setOrientationNumber(media.getOrientationNumber());

        networkImageView.setTag(httpRequest.getUrl());

        imageLoader.setHeaders(httpRequest.getHeaders());

        networkImageView.setImageUrl(httpRequest.getUrl(), imageLoader);
    }

    public static boolean checkMediaIsGif(Media media) {

        return media.getType().toLowerCase().contains("gif") || media.getOriginalPhotoPath().toLowerCase().contains("gif");

    }


}
