package com.winsun.fruitmix.mqtt;

import android.content.Context;
import android.util.Log;

import com.winsun.fruitmix.eventbus.MqttMessageEvent;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2018/2/9.
 */

public class MqttUseCase {

    public static final String TAG = MqttUseCase.class.getSimpleName();

    private MqttAndroidClient mMqttAndroidClient;

    private static final String TEST_SERVER_URI = "tcp://test.siyouqun.com";

    private static final String PRODUCTION_SERVER_URI = "tcp://mqtt.siyouqun.com";

    private String subscriptionTopic = "";

    private final String publishTopic = "presence";

    private static MqttUseCase mMqttUseCase;

    private boolean initMqtt = false;

    private MqttUseCase() {

    }

    public static MqttUseCase getInstance() {

        if (mMqttUseCase == null)
            mMqttUseCase = new MqttUseCase();

        return mMqttUseCase;

    }

    public static void destroyInstance() {

        mMqttUseCase = null;

    }


    public void initMqttClient(Context context, final String currentUserGUID) {

        if (initMqtt)
            return;

        initMqtt = true;

        String clientID = "client_android_" + currentUserGUID;

        Log.d(TAG, "initMqttClient: clientID: " + clientID);

        mMqttAndroidClient = new MqttAndroidClient(context, PRODUCTION_SERVER_URI, clientID);

        mMqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect && mMqttAndroidClient != null) {

                    Log.d(TAG, "connectComplete: reconnect to " + serverURI);

                    subscribeToTopic(currentUserGUID);
                } else {

                    Log.d(TAG, "connectComplete: connect to " + serverURI);
                }

            }

            @Override
            public void connectionLost(Throwable cause) {

                if (cause != null)
                    cause.printStackTrace();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                String messageStr = new String(message.getPayload());

                Log.d(TAG, "callback messageArrived: topic: " + topic + " message: " + messageStr);

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setKeepAliveInterval(3);
        mqttConnectOptions.setConnectionTimeout(10 * 1000);

        try {
            mMqttAndroidClient.connect(mqttConnectOptions, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mMqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic(currentUserGUID);

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    exception.printStackTrace();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    private void subscribeToTopic(String currentUserGUID) {

        subscriptionTopic = "client/user/" + currentUserGUID + "/box";

        try {
            mMqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    String messageStr = new String(message.getPayload());

                    Log.d(TAG, "messageArrived: topic: " + topic + " message: " + messageStr);

                    updateMessage(topic, messageStr);

                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public static final String MQTT_MESSAGE = "mqtt_messgage";

    private void updateMessage(final String topic, final String message) {

        EventBus.getDefault().postSticky(new MqttMessageEvent(MQTT_MESSAGE, new OperationSuccess(), message));

    }

    public void stopMqtt() {

        Log.d(TAG, "stopMqtt: ");

        try {

            if (mMqttAndroidClient != null) {

                mMqttAndroidClient.unregisterResources();

                mMqttAndroidClient.unsubscribe(subscriptionTopic);

                mMqttAndroidClient.disconnect();

                mMqttAndroidClient = null;

            }

        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();

        }

    }


}
