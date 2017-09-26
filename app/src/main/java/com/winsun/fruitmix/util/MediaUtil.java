package com.winsun.fruitmix.util;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;

/**
 * Created by Administrator on 2017/9/20.
 */

public class MediaUtil {

    public static void startLoadRemoteImageUrl(String url, NetworkImageView networkImageView, ImageLoader imageLoader){

        networkImageView.setDefaultImageResId(R.drawable.default_place_holder);

        if (url != null && !url.isEmpty()) {

            imageLoader.setShouldCache(true);

            networkImageView.setTag(url);

            networkImageView.setImageUrl(url, imageLoader);
        }

    }

}
