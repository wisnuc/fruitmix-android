package com.winsun.fruitmix.mqtt;

import android.content.Context;

/**
 * Created by Administrator on 2018/2/9.
 */

public class InjectMqttUseCase {

    public static MqttUseCase provideInstance(Context context){
        return MqttUseCase.getInstance();
    }

}
