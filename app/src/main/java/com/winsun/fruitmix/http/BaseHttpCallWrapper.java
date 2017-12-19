package com.winsun.fruitmix.http;

import android.util.Patterns;

import com.winsun.fruitmix.callback.BaseDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.equipment.search.data.EquipmentBootInfo;
import com.winsun.fruitmix.file.data.model.AbstractRemoteFile;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithEquipmentBootInfo;
import com.winsun.fruitmix.model.operationResult.OperationSuccessWithFile;
import com.winsun.fruitmix.parser.RemoteDataParser;
import com.winsun.fruitmix.parser.RemoteDatasParser;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * Created by Administrator on 2017/7/13.
 */

public class BaseHttpCallWrapper {

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

        if (!checkPreCondition(httpRequest, callback)) return;

        operateCall(httpRequest, callback, parser, 0);
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

    public <T> void loadCall(HttpRequest httpRequest, BaseLoadDataCallback<T> callback, RemoteDatasParser<T> parser, int operationID) {

        try {

            HttpResponse httpResponse = iHttpUtil.remoteCall(httpRequest);

            if (httpResponse.getResponseCode() == 200) {

                List<T> data = parser.parse(httpResponse.getResponseData());

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

    public OperationResult loadEquipmentBootInfo(HttpRequest httpRequest, RemoteDatasParser<EquipmentBootInfo> parser) {


        if (!checkUrl(httpRequest.getUrl()))
            return new OperationMalformedUrlException();

        try {

            HttpResponse httpResponse = iHttpUtil.remoteCall(httpRequest);

            if (httpResponse.getResponseCode() == 200) {

                List<EquipmentBootInfo> equipmentBootInfos = parser.parse(httpResponse.getResponseData());

                return new OperationSuccessWithEquipmentBootInfo(equipmentBootInfos);

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
