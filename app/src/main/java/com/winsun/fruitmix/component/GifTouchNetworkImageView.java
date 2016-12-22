/**
 * Copyright (C) 2013 The Android Open Source Project
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.winsun.fruitmix.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

import com.android.volley.VolleyError;
import com.android.volley.orientation.OrientationOperation;
import com.android.volley.orientation.OrientationOperationFactory;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.IImageLoadListener;
import com.winsun.fruitmix.gif.GifLoader;
import com.winsun.fruitmix.gif.GifLoader.GifContainer;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
public class GifTouchNetworkImageView extends GifTouchImageView {

    public static final String TAG = GifTouchNetworkImageView.class.getSimpleName();

    /**
     * The URL of the network image to load
     */
    private String mUrl;

    /**
     * Resource ID of the image to be used as a placeholder until the network image is loaded.
     */
    private int mDefaultImageId;

    /**
     * Resource ID of the image to be used if the network response fails.
     */
    private int mErrorImageId;

    private GifLoader mGifLoader;

    private ImageLoader mImageLoader;

    private int orientationNumber;

    /**
     * Current ImageContainer. (either in-flight or finished)
     */
    private GifContainer mGifContainer;

    private ImageLoader.ImageContainer mImageContainer;

    private IImageLoadListener mImageLoadListener;

    private boolean isLoadGif = false;

    public GifTouchNetworkImageView(Context context) {
        this(context, null);
    }

    public GifTouchNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifTouchNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets URL of the image that should be loaded into this view. Note that calling this will
     * immediately either set the cached image (if available) or the default image specified by
     * {@link GifTouchNetworkImageView#setDefaultImageResId(int)} on the view.
     * <p/>
     * NOTE: If applicable, {@link GifTouchNetworkImageView#setDefaultImageResId(int)} and
     * {@link GifTouchNetworkImageView#setErrorImageResId(int)} should be called prior to calling
     * this function.
     *
     * @param url       The URL that should be loaded into this ImageView.
     * @param gifLoader ImageLoader that will be used to make the request.
     */
    public void setGifUrl(String url, GifLoader gifLoader) {
        mUrl = url;
        mGifLoader = gifLoader;

        isLoadGif = true;

        loadGifIfNecessary(false);
    }

    public void setImageUrl(String url, ImageLoader imageLoader) {
        mUrl = url;
        mImageLoader = imageLoader;

        isLoadGif = false;

        loadImageIfNecessary(false);
    }

    /**
     * Sets the default image resource ID to be used for this view until the attempt to load it
     * completes.
     */
    public void setDefaultImageResId(int defaultImage) {
        mDefaultImageId = defaultImage;
    }

    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     */
    public void setErrorImageResId(int errorImage) {
        mErrorImageId = errorImage;
    }

    public void registerImageLoadListener(IImageLoadListener loadListener) {
        mImageLoadListener = loadListener;
    }

    public void unregisterImageLoadListener() {
        mImageLoadListener = null;
    }

    public void setOrientationNumber(int orientationNumber) {
        this.orientationNumber = orientationNumber;
    }

    /**
     * Loads the image for the view if it isn't already loaded.
     *
     * @param isInLayoutPass True if this was invoked from a layout pass, false otherwise.
     */
    void loadGifIfNecessary(final boolean isInLayoutPass) {

        int width = getWidth();
        int height = getHeight();

        boolean wrapWidth = false, wrapHeight = false;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == LayoutParams.WRAP_CONTENT;
            wrapHeight = getLayoutParams().height == LayoutParams.WRAP_CONTENT;
        }

        // if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
        // view, hold off on loading the image.
        boolean isFullyWrapContent = wrapWidth && wrapHeight;

        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            if (mGifContainer != null) {
                mGifContainer.cancelRequest();
                mGifContainer = null;
            }
            setDefaultImageOrNull();
            return;
        }

        // if there was an old request in this view, check if it needs to be canceled.
        if (mGifContainer != null && mGifContainer.getRequestUrl() != null) {
            if (mGifContainer.getRequestUrl().equals(mUrl)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                mGifContainer.cancelRequest();

            }
        }

        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        GifContainer newContainer = mGifLoader.get(mUrl,
                new GifLoader.GifListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mErrorImageId != 0) {
                            setImageResource(mErrorImageId);
                        }

                        deliverImageLoadFinish();

                    }

                    @Override
                    public void onResponse(final GifContainer response, boolean isImmediate) {
                        // If this was an immediate response that was delivered inside of a layout
                        // pass do not set the image immediately as it will trigger a requestLayout
                        // inside of a layout. Instead, defer setting the image by posting back to
                        // the main thread.
                        if (isImmediate && isInLayoutPass) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                            return;
                        }

                        byte[] data = response.getData();

                        if (data != null && getTag().equals(mUrl)) {

                            try {

                                GifDrawable gifDrawable = new GifDrawable(data);

                                setImageDrawable(gifDrawable);

                            } catch (IOException e) {
                                e.printStackTrace();

                                setImageResource(mDefaultImageId);
                            }

                            deliverImageLoadFinish();

                        }
                    }
                });

        // update the ImageContainer to be the new bitmap container.
        mGifContainer = newContainer;
    }

    void loadImageIfNecessary(final boolean isInLayoutPass) {

        int width = getWidth();
        int height = getHeight();
        ScaleType scaleType = getScaleType();

        boolean wrapWidth = false, wrapHeight = false;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == LayoutParams.WRAP_CONTENT;
            wrapHeight = getLayoutParams().height == LayoutParams.WRAP_CONTENT;
        }

        // if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
        // view, hold off on loading the image.
        boolean isFullyWrapContent = wrapWidth && wrapHeight;

        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            if (mImageContainer != null) {
                mImageContainer.cancelRequest();
                mImageContainer = null;
            }
            setDefaultImageOrNull();
            return;
        }

        // if there was an old request in this view, check if it needs to be canceled.
        if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
            if (mImageContainer.getRequestUrl().equals(mUrl)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                mImageContainer.cancelRequest();

            }
        }

        // Calculate the max image width / height to use while ignoring WRAP_CONTENT dimens.
        int maxWidth = wrapWidth ? 0 : width;
        int maxHeight = wrapHeight ? 0 : height;

        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        ImageLoader.ImageContainer newContainer = mImageLoader.get(mUrl,
                new ImageLoader.ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mErrorImageId != 0) {
                            setImageResource(mErrorImageId);
                        }

                        deliverImageLoadFinish();
                    }

                    @Override
                    public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                        // If this was an immediate response that was delivered inside of a layout
                        // pass do not set the image immediately as it will trigger a requestLayout
                        // inside of a layout. Instead, defer setting the image by posting back to
                        // the main thread.
                        if (isImmediate && isInLayoutPass) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                            return;
                        }

                        if (response.getBitmap() != null && getTag().equals(mUrl)) {

                            Bitmap bitmap;

                            if (orientationNumber >= 1 && orientationNumber <= 8) {
                                OrientationOperation orientationOperation = OrientationOperationFactory.createOrientationOperation(orientationNumber);
                                bitmap = orientationOperation.handleOrientationOperate(response.getBitmap());
                            } else {
                                bitmap = response.getBitmap();
                            }

                            setImageBitmap(bitmap);

                            deliverImageLoadFinish();

                        } else if (mDefaultImageId != 0) {
                            setImageResource(mDefaultImageId);
                        }
                    }
                }, maxWidth, maxHeight, scaleType);

        // update the ImageContainer to be the new bitmap container.
        mImageContainer = newContainer;
    }

    private void deliverImageLoadFinish() {
        post(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, "onResponse Url:" + mUrl);
                if (mImageLoadListener != null) {
                    mImageLoadListener.onImageLoadFinish(mUrl, GifTouchNetworkImageView.this);
                }
            }
        });
    }

    private void setDefaultImageOrNull() {
        if (mDefaultImageId != 0) {
            setImageResource(mDefaultImageId);
        } else {
            setImageBitmap(null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (isLoadGif) {
            loadGifIfNecessary(true);
        } else {
            loadImageIfNecessary(true);
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        if (mGifContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            mGifContainer.cancelRequest();
            setImageDrawable(null);
            // also clear out the container so we can reload the image if necessary.
            mGifContainer = null;
        } else if (mImageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            mImageContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the container so we can reload the image if necessary.
            mImageContainer = null;
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}
