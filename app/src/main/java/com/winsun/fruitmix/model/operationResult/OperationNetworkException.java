package com.winsun.fruitmix.model.operationResult;

import android.content.Context;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.http.HttpResponse;
import com.winsun.fruitmix.model.OperationResultType;

/**
 * Created by Administrator on 2016/11/23.
 */

public class OperationNetworkException extends OperationResult {

    private HttpResponse httpResponse;

    public OperationNetworkException(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    @Override
    public String getResultMessage(Context context) {

        String resultMessage;

        if (httpResponse.getResponseCode() == 401) {
            resultMessage = context.getString(R.string.password_error);
        } else {
            resultMessage = String.format(context.getString(R.string.network_exception), "http " + httpResponse.getResponseCode());
        }

        return resultMessage;
    }

    public String getHttpResponseData(){
        return httpResponse.getResponseData();
    }

    public int getHttpResponseCode(){
        return httpResponse.getResponseCode();
    }

    @Override
    public OperationResultType getOperationResultType() {
        return OperationResultType.NETWORK_EXCEPTION;
    }
}
