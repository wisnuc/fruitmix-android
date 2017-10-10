package com.winsun.fruitmix.http.request.factory;


import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/10/9.
 */

public class StationHttpRequestFactory extends BaseAbsHttpRequestFactory {

    public StationHttpRequestFactory(String gateway, HttpHeader httpHeader) {
        super(httpHeader);

        if(gateway.startsWith(Util.HTTP)){
            gateway = gateway.split(Util.HTTP)[1];
        }

        setGateway(gateway);

        setPort(3000);
    }

}
