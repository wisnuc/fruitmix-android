package com.winsun.fruitmix.http;

import android.util.Log;
import android.util.Patterns;

import com.winsun.fruitmix.callback.BaseDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.search.data.EquipmentBootInfo;
import com.winsun.fruitmix.exception.NetworkException;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSucceedWithData;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithFile;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteDataStreamParser;
import com.winsun.fruitmix.parser.RemoteDatasParser;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2017/7/13.
 */

public class BaseHttpCallWrapper {

    public static final String TAG = BaseHttpCallWrapper.class.getSimpleName();

    private IHttpUtil iHttpUtil;

    BaseHttpCallWrapper(IHttpUtil iHttpUtil) {
        this.iHttpUtil = iHttpUtil;
    }

    public boolean checkPreCondition(HttpRequest httpRequest, BaseDataCallback callback) {
        if (checkUrl(httpRequest.getUrl())) {
            return true;
        } else {
            callback.onFail(new OperationMalformedUrlException());
            return false;
        }

    }

    public boolean checkUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }

    public <T> void operateCall(HttpRequest httpRequest, BaseOperateDataCallback<T> callback, RemoteDataParser<T> parser) {

        operateCall(httpRequest, callback, parser, 0);
    }

    public void operateCall(HttpRequest httpRequest, List<TextFormData> textFormDatas, List<FileFormData> fileFormDatas, BaseOperateCallback callback) {

        if (!checkPreCondition(httpRequest, callback)) return;

        try {

            HttpResponse httpResponse = iHttpUtil.remoteCallRequest(iHttpUtil.createPostRequest(httpRequest,
                    iHttpUtil.createFormDataRequestBody(textFormDatas, fileFormDatas)));

            if (httpResponse != null && httpResponse.getResponseCode() == 200) {

                callback.onSucceed();

            } else {

                callback.onFail(new OperationNetworkException(httpResponse));

            }

        } catch (MalformedURLException e) {

            callback.onFail(new OperationMalformedUrlException());

        } catch (SocketTimeoutException ex) {

            callback.onFail(new OperationSocketTimeoutException());

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());
        }

    }

    public <T> void operateCall(HttpRequest httpRequest, List<TextFormData> textFormDatas, List<FileFormData> fileFormDatas, BaseOperateDataCallback<T> callback, RemoteDataParser<T> parser) {

        if (!checkPreCondition(httpRequest, callback)) return;

        try {

            HttpResponse httpResponse = iHttpUtil.remoteCallRequest(iHttpUtil.createPostRequest(httpRequest,
                    iHttpUtil.createFormDataRequestBody(textFormDatas, fileFormDatas)));

            if (httpResponse.getResponseCode() == 200) {

                T data = parser.parse(httpResponse.getResponseData());

                callback.onSucceed(data, new OperationSuccess());

            } else {

                callback.onFail(new OperationNetworkException(httpResponse));

            }


        } catch (MalformedURLException e) {

            callback.onFail(new OperationMalformedUrlException());

        } catch (SocketTimeoutException ex) {

            callback.onFail(new OperationSocketTimeoutException());

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            callback.onFail(new OperationJSONException());
        }

    }

    public void operateCall(HttpRequest httpRequest, BaseOperateCallback callback){

        if(!checkPreCondition(httpRequest,callback))return;

        try {

            HttpResponse httpResponse = iHttpUtil.remoteCall(httpRequest);

            if (httpResponse.getResponseCode() == 200) {

                callback.onSucceed();

            } else {

                callback.onFail(new OperationNetworkException(httpResponse));

            }


        } catch (MalformedURLException e) {

            callback.onFail(new OperationMalformedUrlException());

        } catch (SocketTimeoutException ex) {

            callback.onFail(new OperationSocketTimeoutException());

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());
        }

    }

    public <T> void operateCall(HttpRequest httpRequest, BaseOperateDataCallback<T> callback, RemoteDataParser<T> parser, int operationID) {

        if (!checkPreCondition(httpRequest, callback)) return;

        try {

            HttpResponse httpResponse = iHttpUtil.remoteCall(httpRequest);

            if (httpResponse.getResponseCode() == 200) {

                T data = parser.parse(httpResponse.getResponseData());

                if (operationID == 0)
                    callback.onSucceed(data, new OperationSuccess());
                else
                    callback.onSucceed(data, new OperationSuccess(operationID));

            } else {

                callback.onFail(new OperationNetworkException(httpResponse));

            }


        } catch (MalformedURLException e) {

            callback.onFail(new OperationMalformedUrlException());

        } catch (SocketTimeoutException ex) {

            callback.onFail(new OperationSocketTimeoutException());

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            callback.onFail(new OperationJSONException());
        }

    }

    public <T> void loadCall(HttpRequest httpRequest, BaseLoadDataCallback<T> callback, RemoteDatasParser<T> parser) {

        if (!checkPreCondition(httpRequest, callback)) return;

        loadCall(httpRequest, callback, parser, 0);
    }

    public <T> void loadCall(HttpRequest httpRequest, BaseLoadDataCallback<T> callback, RemoteDataStreamParser<T> parser) {

        if (!checkPreCondition(httpRequest, callback)) return;

        try {

            Log.d(TAG, "loadCall: start get response body,url:" + httpRequest.getUrl() + "," + Util.getCurrentFormatTime());

            ResponseBody responseBody = iHttpUtil.getHttpResponseBody(httpRequest).getResponseBody();

            Log.d(TAG, "loadCall: get response body,request url:" + httpRequest.getUrl() + "," + Util.getCurrentFormatTime());

            List<T> data = parser.parse(responseBody.byteStream());

            Log.d(TAG, "loadCall: finish parse stream,request url:" + httpRequest.getUrl() + "," + Util.getCurrentFormatTime());

            callback.onSucceed(data, new OperationSuccess());

        } catch (MalformedURLException e) {

            callback.onFail(new OperationMalformedUrlException());

        } catch (SocketTimeoutException ex) {

            callback.onFail(new OperationSocketTimeoutException());

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            callback.onFail(new OperationJSONException());
        } catch (NetworkException e) {
            e.printStackTrace();

            callback.onFail(new OperationNetworkException(e.getHttpResponse()));
        }

    }


    public <T> void loadCall(HttpRequest httpRequest, BaseLoadDataCallback<T> callback, RemoteDatasParser<T> parser, int operationID) {

        try {

            Log.d(TAG, "loadCall: start call http request,url:" + httpRequest.getUrl() + "," + Util.getCurrentFormatTime());

            HttpResponse httpResponse = iHttpUtil.remoteCall(httpRequest);

            Log.d(TAG, "loadCall: get http response,request url:" + httpRequest.getUrl() + "," + Util.getCurrentFormatTime());

            if (httpResponse.getResponseCode() == 200) {

                List<T> data = parser.parse(httpResponse.getResponseData());

                Log.d(TAG, "loadCall: finish parse data,request url:" + httpRequest.getUrl() + "," + Util.getCurrentFormatTime());

                if (operationID == 0)
                    callback.onSucceed(data, new OperationSuccess());
                else
                    callback.onSucceed(data, new OperationSuccess(operationID));

            } else {

                callback.onFail(new OperationNetworkException(httpResponse));
            }


        } catch (MalformedURLException e) {

            callback.onFail(new OperationMalformedUrlException());

        } catch (SocketTimeoutException ex) {

            callback.onFail(new OperationSocketTimeoutException());

        } catch (IOException e) {
            e.printStackTrace();

            callback.onFail(new OperationIOException());
        } catch (JSONException e) {
            e.printStackTrace();

            callback.onFail(new OperationJSONException());
        }

    }


    public OperationResult loadCall(HttpRequest httpRequest, RemoteDatasParser<AbstractRemoteFile> parser) {

        if (!checkUrl(httpRequest.getUrl()))
            return new OperationMalformedUrlException();

        try {

            HttpResponse httpResponse = iHttpUtil.remoteCall(httpRequest);

            if (httpResponse.getResponseCode() == 200) {

                List<AbstractRemoteFile> files = parser.parse(httpResponse.getResponseData());

                return new OperationSuccessWithFile(files);

            } else {

                return new OperationNetworkException(httpResponse);

            }


        } catch (MalformedURLException e) {

            return new OperationMalformedUrlException();

        } catch (SocketTimeoutException ex) {

            return new OperationSocketTimeoutException();

        } catch (IOException e) {
            e.printStackTrace();

            return new OperationIOException();

        } catch (JSONException e) {
            e.printStackTrace();

            return new OperationJSONException();
        }

    }



}
