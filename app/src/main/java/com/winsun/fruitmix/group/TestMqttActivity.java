package com.winsun.fruitmix.group;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.util.Util;

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

import java.util.ArrayList;
import java.util.List;

public class TestMqttActivity extends AppCompatActivity {

    public static final String TAG = TestMqttActivity.class.getSimpleName();

    private EditText mEditText;

    private Button mSendBtn;

    private MessageRecyclerViewAdapter mMessageRecyclerViewAdapter;

    private MqttAndroidClient mMqttAndroidClient;

    private static String serverUri = "tcp://test.mosquitto.org";

    private final String subscriptionTopic = "presence";

    private final String publishTopic = "presence";

    private List<String> mMessages;

    private Handler mainThreadHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_mqtt);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        mEditText = findViewById(R.id.editText);

        mSendBtn = findViewById(R.id.send);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mMessageRecyclerViewAdapter = new MessageRecyclerViewAdapter();

        recyclerView.setAdapter(mMessageRecyclerViewAdapter);

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = mEditText.getText().toString();

                publishMessage(message);

            }
        });

        mMessages = new ArrayList<>();

        mainThreadHandler = new Handler(Looper.myLooper());

        initMqttClient();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            mMqttAndroidClient.unsubscribe(subscriptionTopic);

            mMqttAndroidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void initMqttClient() {

        String clientID = Build.MANUFACTURER + "-" + Build.MODEL + Util.createLocalUUid();

        mMqttAndroidClient = new MqttAndroidClient(this, serverUri, clientID);

        mMqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {

                    Log.d(TAG, "connectComplete: reconnect to " + serverURI);

                    subscribeToTopic();
                }

            }

            @Override
            public void connectionLost(Throwable cause) {

                if (cause != null)
                    cause.printStackTrace();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

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
                    subscribeToTopic();

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


    private void subscribeToTopic() {

        try {
            mMqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    String messageStr = new String(message.getPayload());

                    Log.d(TAG, "messageArrived: " + messageStr);

                    updateMessage(topic, messageStr);

                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void updateMessage(final String topic, final String message) {

        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {

                mMessages.add("Incoming message: " + message + " topic: " + topic);

                mMessageRecyclerViewAdapter.setMessages(mMessages);

                int position = mMessages.size() - 1;

                mMessageRecyclerViewAdapter.notifyItemInserted(position);


            }
        });


    }


    private void publishMessage(String message) {

        MqttMessage mqttMessage = new MqttMessage(message.getBytes());

        try {

            mMqttAndroidClient.publish(publishTopic, mqttMessage);

            if (!mMqttAndroidClient.isConnected()) {
                Log.d(TAG, "publishMessage: " + mMqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }

        } catch (MqttException e) {
            e.printStackTrace();
        }

        mEditText.getText().clear();

        Util.hideSoftInput(this);

    }


    private class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewHolder> {

        private List<String> mMessages;

        public MessageRecyclerViewAdapter() {
            mMessages = new ArrayList<>();
        }

        public void setMessages(List<String> messages) {
            mMessages.clear();
            mMessages.addAll(messages);
        }

        @Override
        public MessageRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            TextView textView = new TextView(parent.getContext());

            parent.addView(textView);

            return new MessageRecyclerViewHolder(textView);

        }


        @Override
        public void onBindViewHolder(MessageRecyclerViewHolder holder, int position) {

            holder.refreshView(mMessages.get(position));

        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return mMessages.size();
        }
    }

    private class MessageRecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;

        public MessageRecyclerViewHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView;
        }

        void refreshView(String text) {

            mTextView.setText(text);

        }

    }


}
