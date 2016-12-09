package com.winsun.fruitmix.gif;

/**
 * Created by Administrator on 2016/12/6.
 */

import android.os.Handler;
import android.os.Looper;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Helper that handles loading and caching images from remote URLs.
 * <p>
 * The simple way to use this class is to call {@link GifLoader#get(String requestUrl, GifListener fileListener, boolean mastRunMainThread)}
 * and to pass in the default image listener provided by
 * {@link GifLoader#getGifListener()}. Note that all function calls to
 * this class must be made from the main thead, and all responses will be delivered to the main
 * thread as well.
 */
public class GifLoader {
    /**
     * RequestQueue for dispatching ImageRequests onto.
     */
    private final RequestQueue mRequestQueue;

    /**
     * Amount of time to wait after first response arrives before delivering all responses.
     */
    private int mBatchResponseDelayMs = 100;

    /**
     * The cache implementation to be used as an L1 cache before calling into volley.
     */
    private final GifCache mCache;

    /**
     * HashMap of Cache keys -> BatchedGifRequest used to track in-flight requests so
     * that we can coalesce multiple requests to the same URL into a single network request.
     */
    private final HashMap<String, BatchedGifRequest> mInFlightRequests =
            new HashMap<String, BatchedGifRequest>();

    /**
     * HashMap of the currently pending responses (waiting to be delivered).
     */
    private final HashMap<String, BatchedGifRequest> mBatchedResponses =
            new HashMap<String, BatchedGifRequest>();

    /**
     * Handler to the main thread.
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * Runnable for in-flight response delivery.
     */
    private Runnable mRunnable;

    /**
     * callback Listener
     */
    private GifListener mGifListener;

    /**
     * if need control UI,this is must be true
     */
    private boolean mMastRunMainThread;

    /**
     * use cache if this is true
     */
    private boolean useCache = true;

    private Map<String, String> mHeaders;

    private Object mTag;

    private boolean mShouldCache = true;

    /**
     * Simple cache adapter interface. If provided to the GifLoader, it
     * will be used as an L1 cache before dispatch to Volley. Implementations
     * must not block. Implementation with an LruCache is recommended.
     */
    public interface GifCache {
        byte[] getData(String url);

        void putData(String url, byte[] data);
    }

    /**
     * Constructs a new GifLoader.
     *
     * @param queue    The RequestQueue to use for making file requests.
     * @param gifCache The cache to use as an L1 cache.
     */
    public GifLoader(RequestQueue queue, GifCache gifCache) {
        mRequestQueue = queue;
        mCache = gifCache;
    }

    /**
     * Constructs a new GifLoader.
     *
     * @param queue             The RequestQueue to use for making file requests.
     * @param gifCache          The cache to use as an L1 cache.
     * @param mastRunMainThread if need control UI,this is must be true
     */
    public GifLoader(RequestQueue queue, GifCache gifCache, boolean mastRunMainThread) {
        this(queue, gifCache);
        this.mMastRunMainThread = mastRunMainThread;
    }

    public GifListener getGifListener() {
        return mGifListener;
    }

    public void setGifListener(GifListener mGifListener) {
        this.mGifListener = mGifListener;
    }

    public boolean ismMastRunMainThread() {
        return mMastRunMainThread;
    }

    public void setmMastRunMainThread(boolean mMastRunMainThread) {
        this.mMastRunMainThread = mMastRunMainThread;
    }

    /**
     * Interface for the response handlers on image requests.
     * <p>
     * The call flow is this:
     * 1. Upon being  attached to a request, onResponse(response, true) will
     * be invoked to reflect any cached data that was already available. If the
     * data was available, response.getBitmap() will be non-null.
     * <p>
     * 2. After a network response returns, only one of the following cases will happen:
     * - onResponse(response, false) will be called if the image was loaded.
     * or
     * - onErrorResponse will be called if there was an error loading the image.
     */
    public interface GifListener extends ErrorListener {
        /**
         * Listens for non-error changes to the loading of the image request.
         *
         * @param gifContainer Holds all information pertaining to the request, as well
         *                     as the bitmap (if it is loaded).
         * @param isImmediate  True if this was called during GifLoader.get() variants.
         *                     This can be used to differentiate between a cached image loading and a network
         *                     loading in order to, for example, run an animation to fade in network loaded
         *                     images.
         */
        public void onResponse(GifContainer gifContainer, boolean isImmediate);
    }

    /**
     * Issues a bitmap request with the given URL if that image is not available
     * in the cache, and returns a bitmap container that contains all of the data
     * relating to the request (as well as the default image if the requested
     * image is not available).
     *
     * @param requestUrl        The url of the remote image
     * @param gifListener       The listener to call when the remote image is loaded
     * @param mastRunMainThread need run in main thread
     * @return A container object that contains all of the properties of the request, as well as
     * the currently available image (default if remote is not loaded).
     */

    public GifContainer get(String requestUrl, GifListener gifListener, boolean mastRunMainThread) {
        // only fulfill requests that were initiated from the main thread.
        if (mMastRunMainThread) {
            throwIfNotOnMainThread();
        }
        this.mMastRunMainThread = mastRunMainThread;
        mGifListener = gifListener;
        if (useCache) {
            // Try to look up the request in the cache of remote images.
            byte[] cachedData = mCache.getData(requestUrl);
            if (cachedData != null) {
                // Return the cached bitmap.
                GifContainer container = new GifContainer(cachedData, requestUrl, null, null);
                gifListener.onResponse(container, true);
                return container;
            }
        }
        // The data did not exist in the cache, fetch it!
        GifContainer gifContainer = new GifContainer(null, requestUrl, requestUrl, gifListener);

        // Update the caller to let them know that they should use the default bitmap.
//        fileListener.onResponse(fileContainer, true);

        // Check to see if a request is already in-flight.
        BatchedGifRequest request = mInFlightRequests.get(requestUrl);
        if (request != null) {
            // If it is, add this request to the list of listeners.
            request.addContainer(gifContainer);
            return gifContainer;
        }
        // The request is not already in flight. Send the new request to the network and track it.
        Request<byte[]> newRequest = makeGifRequest(requestUrl, requestUrl);

        if (mTag != null) {
            newRequest.setTag(mTag);
        }

        newRequest.setShouldCache(mShouldCache);

        mRequestQueue.add(newRequest);
        mInFlightRequests.put(requestUrl, new BatchedGifRequest(newRequest, gifContainer));

        return gifContainer;
    }

    /**
     * Issues a bitmap request with the given URL if that image is not available
     * in the cache, and returns a bitmap container that contains all of the data
     * relating to the request (as well as the default image if the requested
     * image is not available).
     *
     * @param requestUrl  The url of the remote image
     * @param gifListener The listener to call when the remote image is loaded
     * @return A container object that contains all of the properties of the request, as well as
     * the currently available image (default if remote is not loaded).
     */
    public GifContainer get(String requestUrl, GifListener gifListener) {
        return get(requestUrl, gifListener, false);
    }

    public GifContainer get(String requestUrl) {
        return get(requestUrl, mGifListener);
    }

    protected Request<byte[]> makeGifRequest(String requestUrl, final String cacheKey) {
        return new GifRequest(requestUrl, new Listener<byte[]>() {
            @Override
            public void onResponse(byte[] data) {
                onGetGifSuccess(cacheKey, data);
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                onGetGifError(cacheKey, volleyError);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (mHeaders == null) {
                    return super.getHeaders();
                } else {
                    return mHeaders;
                }
            }
        };
    }

    /**
     * add by liang.wu for add header in ImageRequest
     *
     * @param headers the headers for request
     */
    public void setHeaders(Map<String, String> headers) {
        mHeaders = headers;
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public void setShouldCache(boolean shouldCache) {
        mShouldCache = shouldCache;
    }

    /**
     * Sets the amount of time to wait after the first response arrives before delivering all
     * responses. Batching can be disabled entirely by passing in 0.
     *
     * @param newBatchedResponseDelayMs The time in milliseconds to wait.
     */
    public void setBatchedResponseDelay(int newBatchedResponseDelayMs) {
        mBatchResponseDelayMs = newBatchedResponseDelayMs;
    }

    /**
     * Handler for when an image was successfully loaded.
     *
     * @param cacheKey The cache key that is associated with the image request.
     * @param data     The data that was returned from the network.
     */
    protected void onGetGifSuccess(String cacheKey, byte[] data) {
        // cache the image that was fetched.
        if (useCache) {
            mCache.putData(cacheKey, data);
        }

        // remove the request from the list of in-flight requests.
        BatchedGifRequest request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            // Update the response bitmap.
            request.mResponseData = data;

            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Handler for when an image failed to load.
     *
     * @param cacheKey The cache key that is associated with the image request.
     */
    protected void onGetGifError(String cacheKey, VolleyError error) {
        // Notify the requesters that something failed via a null result.
        // Remove this request from the list of in-flight requests.
        BatchedGifRequest request = mInFlightRequests.remove(cacheKey);

        if (request != null) {
            // Set the error for this request
            request.setError(error);

            // Send the batched response
            batchResponse(cacheKey, request);
        }
    }

    /**
     * Container object for all of the data surrounding an image request.
     */
    public class GifContainer {
        /**
         * The most relevant bitmap for the container. If the image was in cache, the
         * Holder to use for the final bitmap (the one that pairs to the requested URL).
         */
        private byte[] mData = null;

        private final GifListener mListener;

        /**
         * The cache key that was ass ociated with the request
         */
        private final String mCacheKey;

        /**
         * The request URL that was specified
         */
        private final String mRequestUrl;

        /**
         * Constructs a BitmapContainer object.
         *
         * @param data       The final bitmap (if it exists).
         * @param requestUrl The requested URL for this container.
         * @param cacheKey   The cache key that identifies the requested URL for this container.
         */
        public GifContainer(byte[] data, String requestUrl,
                            String cacheKey, GifListener listener) {
            this.mData = data;
            mRequestUrl = requestUrl;
            mCacheKey = cacheKey;
            mListener = listener;
        }

        /**
         * Releases interest in the in-flight request (and cancels it if no one else is listening).
         */
        public void cancelRequest() {
            if (mListener == null) {
                return;
            }

            BatchedGifRequest request = mInFlightRequests.get(mCacheKey);
            if (request != null) {
                boolean canceled = request.removeContainerAndCancelIfNecessary(this);
                if (canceled) {
                    mInFlightRequests.remove(mCacheKey);
                }
            } else {
                // check to see if it is already batched for delivery.
                request = mBatchedResponses.get(mCacheKey);
                if (request != null) {
                    request.removeContainerAndCancelIfNecessary(this);
                    if (request.mContainers.size() == 0) {
                        mBatchedResponses.remove(mCacheKey);
                    }
                }
            }
        }

        /**
         * Returns the bitmap associated with the request URL if it has been loaded, null otherwise.
         */
        public byte[] getData() {
            return mData;
        }

        /**
         * Returns the requested URL for this container.
         */
        public String getRequestUrl() {
            return mRequestUrl;
        }
    }

    /**
     * Wrapper class used to map a Request to the set of active GifContainer objects that are
     * interested in its results.
     */
    private class BatchedGifRequest {
        /**
         * The request being tracked
         */
        private final Request<?> mRequest;

        /**
         * The result of the request being tracked by this item
         */
        private byte[] mResponseData;

        /**
         * Error if one occurred for this response
         */
        private VolleyError mError;

        /**
         * List of all of the active GifContainers that are interested in the request
         */
        private final LinkedList<GifContainer> mContainers = new LinkedList<GifContainer>();

        /**
         * Constructs a new BatchedGifRequest object
         *
         * @param request   The request being tracked
         * @param container The GifContainer of the person who initiated the request.
         */
        public BatchedGifRequest(Request<?> request, GifContainer container) {
            mRequest = request;
            mContainers.add(container);
        }

        /**
         * Set the error for this response
         */
        public void setError(VolleyError error) {
            mError = error;
        }

        /**
         * Get the error for this response
         */
        public VolleyError getError() {
            return mError;
        }

        /**
         * Adds another GifContainer to the list of those interested in the results of
         * the request.
         */
        public void addContainer(GifContainer container) {
            mContainers.add(container);
        }

        /**
         * Detatches the data container from the request and cancels the request if no one is
         * left listening.
         *
         * @param container The container to remove from the list
         * @return True if the request was canceled, false otherwise.
         */
        public boolean removeContainerAndCancelIfNecessary(GifContainer container) {
            mContainers.remove(container);
            if (mContainers.size() == 0) {
                mRequest.cancel();
                return true;
            }
            return false;
        }
    }

    /**
     * Starts the runnable for batched delivery of responses if it is not already started.
     *
     * @param cacheKey The cacheKey of the response being delivered.
     * @param request  The BatchedGifRequest to be delivered.
     */
    private void batchResponse(String cacheKey, BatchedGifRequest request) {
        mBatchedResponses.put(cacheKey, request);
        // If we don't already have a batch delivery runnable in flight, make a new one.
        // Note that this will be used to deliver responses to all callers in mBatchedResponses.
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    for (BatchedGifRequest bir : mBatchedResponses.values()) {
                        for (GifContainer container : bir.mContainers) {
                            // If one of the callers in the batched request canceled the request
                            // after the response was received but before it was delivered,
                            // skip them.
                            if (container.mListener == null) {
                                continue;
                            }
                            if (bir.getError() == null) {
                                container.mData = bir.mResponseData;
                                container.mListener.onResponse(container, false);
                            } else {
                                container.mListener.onErrorResponse(bir.getError());
                            }
                        }
                    }
                    mBatchedResponses.clear();
                    mRunnable = null;
                }

            };
            // Post the runnable.
            mHandler.postDelayed(mRunnable, mBatchResponseDelayMs);
        }
    }

    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("GifLoader must be invoked from the main thread.");
        }
    }

}