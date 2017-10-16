package com.winsun.fruitmix.http;

import android.util.Patterns;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.model.operationResult.OperationIOException;
import com.winsun.fruitmix.model.operationResult.OperationJSONException;
import com.winsun.fruitmix.model.operationResult.OperationMalformedUrlException;
import com.winsun.fruitmix.model.operationResult.OperationNetworkException;
import com.winsun.fruitmix.model.operationResult.OperationSocketTimeoutException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
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

    public BaseHttpCallWrapper(IHttpUtil iHttpUtil) {
        this.iHttpUtil = iHttpUtil;
    }

    public <T> void operateCall(HttpRequest httpRequest, BaseOperateDataCallback<T> callback, RemoteDataParser<T> parser) {
        operateCall(httpRequest, callback, parser, 0);
    }

    public <T> void operateCall(HttpRequest httpRequest, BaseOperateDataCallback<T> callback, RemoteDataParser<T> parser, int operationID) {

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

        if(!Patterns.WEB_URL.matcher(httpRequest.getUrl()).matches()){
            callback.onFail(new OperationMalformedUrlException());
            return;
        }

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


}
