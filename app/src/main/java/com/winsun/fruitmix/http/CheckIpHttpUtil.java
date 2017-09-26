package com.winsun.fruitmix.http;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.winsun.fruitmix.http.factory.HttpRequestFactory;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.equipment.data.EquipmentSearchManager;
import com.winsun.fruitmix.util.Util;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

/**
 * Created by Administrator on 2017/5/16.
 */

public class CheckIpHttpUtil implements IHttpUtil {

    public static final String TAG = CheckIpHttpUtil.class.getSimpleName();

    private HttpRequestFactory httpRequestFactory;

    private IHttpUtil mIHttpUtil;

    private String mCurrentEquipmentName;
    private EquipmentSearchManager mEquipmentSearchManager;

    private final Object threadControlObject;

    private CustomHandler mHandler;

    private RetryStrategy mRetryStrategy;

    private static final int MSG_DISCOVERY_TIMEOUT = 0x1001;

    private static class CustomHandler extends Handler {

        WeakReference<CheckIpHttpUtil> weakReference;

        CustomHandler(CheckIpHttpUtil checkIpHttpUtil) {
            weakReference = new WeakReference<>(checkIpHttpUtil);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            CheckIpHttpUtil checkIpHttpUtil = weakReference.get();
            checkIpHttpUtil.mEquipmentSearchManager.stopDiscovery();
            checkIpHttpUtil.threadControlObject.notify();

            Log.d(TAG, "handleMessage: stop discovery and notify thread");
        }
    }

    private static CheckIpHttpUtil instance;

    public static CheckIpHttpUtil getInstance(IHttpUtil iHttpUtil, EquipmentSearchManager equipmentSearchManager) {

        if (instance == null)
            instance = new CheckIpHttpUtil(iHttpUtil, equipmentSearchManager);

        return instance;
    }

    private CheckIpHttpUtil(IHttpUtil iHttpUtil, EquipmentSearchManager equipmentSearchManager) {

        mIHttpUtil = iHttpUtil;

        mCurrentEquipmentName = "";
        mEquipmentSearchManager = equipmentSearchManager;

        threadControlObject = new Object();

        mRetryStrategy = new DefaultRetryStrategy();

    }

    public void setCurrentEquipmentName(String currentEquipmentName) {

        if (currentEquipmentName != null && !mCurrentEquipmentName.equals(currentEquipmentName))
            mCurrentEquipmentName = currentEquipmentName;

    }

    public void setHttpRequestFactory(HttpRequestFactory httpRequestFactory) {
        this.httpRequestFactory = httpRequestFactory;
    }

    private boolean checkIp(String url) {

        if (url.length() < 7)
            return false;

        if (url.indexOf(":", 7) == -1)
            return false;

        String ip = url.substring(7, url.indexOf(":", 7));

        Log.d(TAG, "checkIp: " + ip);

        return Util.checkIP(ip);

    }

    private HttpResponse handleRequest(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException {

        while (true) {

            if (checkIp(httpRequest.getUrl())) {

                try {
                    return mIHttpUtil.remoteCall(httpRequest);

                } catch (IOException e) {
                    e.printStackTrace();

                    retryRemoteCall(httpRequest);
                }

            } else {

                retryRemoteCall(httpRequest);

            }

        }


    }

    private void retryRemoteCall(HttpRequest httpRequest) throws IOException {

        if (mRetryStrategy.needRetry()) {

            Log.d(TAG, "retryRemoteCall: search ip");

            searchIpSync(httpRequest);

        } else {

            Log.d(TAG, "retryRemoteCall: throw io exception");

            throw new IOException();
        }

    }


    private void searchIpSync(final HttpRequest httpRequest) {

        if (mCurrentEquipmentName.isEmpty())
            return;

        synchronized (threadControlObject) {

            searchIp(httpRequest);

            try {
                threadControlObject.wait(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

                mEquipmentSearchManager.stopDiscovery();
            }

        }

    }


    private void searchIp(final HttpRequest httpRequest) {

        mEquipmentSearchManager.startDiscovery(new EquipmentSearchManager.IEquipmentDiscoveryListener() {
            @Override
            public void call(Equipment equipment) {

                handleSearchResultSync(equipment, httpRequest);
            }
        });

    }

    private void handleSearchResultSync(Equipment equipment, HttpRequest httpRequest) {

        synchronized (threadControlObject) {

            handleSearchResult(equipment, httpRequest);

        }

    }

    private void handleSearchResult(Equipment equipment, HttpRequest httpRequest) {

        if (equipment.getEquipmentName().equals(mCurrentEquipmentName)) {
            String ip = equipment.getHosts().get(0);

            mEquipmentSearchManager.stopDiscovery();

            String gateway = Util.HTTP + ip;

            if (httpRequestFactory != null)
                httpRequestFactory.setGateway(gateway);

//            FNAS.Gateway = Util.HTTP + ip;

            String url = httpRequest.getUrl();

            url = gateway + url.substring(url.indexOf("/", 7));

            httpRequest.setUrl(url);

            threadControlObject.notify();

        }
    }


    @Override
    public HttpResponse remoteCall(HttpRequest httpRequest) throws MalformedURLException, IOException, SocketTimeoutException {

        return handleRequest(httpRequest);

    }

}
