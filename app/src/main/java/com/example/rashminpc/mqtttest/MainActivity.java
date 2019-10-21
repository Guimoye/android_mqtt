package com.example.rashminpc.mqtttest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    String topic = "casa/cocina/foco";
    String clientId;
    String msj = "android_envio";
    MqttAndroidClient client;
    TextView tt;
    Button btn_public;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tt          = (TextView) findViewById(R.id.tt);
        btn_public  = (Button) findViewById(R.id.btn_public);
        client=null;
        clientId=null;
        connect();

        btn_public.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish(client,msj);
            }
        });
    }

    public void connect(){

        clientId    = "android_12";
        client      = new MqttAndroidClient(this.getApplicationContext(), "tcp://192.168.1.60:1883",
                        clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
        options.setCleanSession(false);
       // options.setUserName("snyhhyzw");
       // options.setPassword("LpZK32PEBN5q".toCharArray());
        try {
            IMqttToken token = client.connect(options);
            //IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.e("file", "onSuccess");
                    //publish(client,"payloadd");
                    subscribe(client,topic);
                    client.setCallback(new MqttCallback() {


                        @Override
                        public void connectionLost(Throwable cause) {

                        }
                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            Log.e("toString", message.toString());
                          //  Log.e("getPayload", message.getPayload().toString());
                           // Log.e("getQos", message.getQos()+"");

                            if (topic.equals(topic)){
                                tt.setText(message.toString());
                            }
/*
                            if (topic.equals("bmp")){
                                th.setText(message.toString());
                            }*/

                        }
                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                           // Log.e("file", "complete delivere");
                        }
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.e("file", "onFailure");

                }
            });

//            Log.e("msmmm",token.getResponse()+"");
        } catch (MqttException e) {
            Log.e("file", "exception");
            e.printStackTrace();
        }
    }

    public void publish(MqttAndroidClient client, String payload){
       // String topic = "foo/bar";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(MqttAndroidClient client , String topic){
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("subcrito","completoo");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Log.e("subcrito","no_completoo");

                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void unSubscribeMqttChannel(MqttAndroidClient client,String topic) {
        try {
            IMqttToken unsubToken = client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                    Log.e("MQTT","On Mqtt unSubscribed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                    Log.e("MQTT","On Mqtt unSubscribe failure "+exception.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void disconnectMqtt(MqttAndroidClient client) {
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.e("MQTT","On Mqtt disconnected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.e("MQTT","On Mqtt disconnect failure "+exception.getMessage());

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG,"onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");

        if(clientId!=null && client!=null){
            unSubscribeMqttChannel(client,topic);
            disconnectMqtt(client);
            clientId=null;
            client=null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG,"onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
        if(!client.isConnected()){
            connect();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG,"onRestart");
    }
}


