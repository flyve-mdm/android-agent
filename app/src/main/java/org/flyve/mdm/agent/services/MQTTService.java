/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * this file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @date      02/06/2017
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.MQTTHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;

/**
 * This is the service that get and send message from MQTT
 */

public class MQTTService extends Service implements MqttCallback {

    private static final String TAG = "MQTT";
    private MqttAndroidClient client;
    private DataStorage cache;
    private String mTopic = "";
    private Boolean connected = false;
    private MQTTHelper mqttHelper;

    public MQTTService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        FlyveLog.i("START", "SERVICE MQTT");
        connect();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getApplicationContext().startService(new Intent(getApplicationContext(), MQTTService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This function connect the agent with MQTT server
     */
    public void connect() {
        cache = new DataStorage(this.getApplicationContext());

        String mBroker = cache.getBroker();
        String mPort = cache.getPort();
        String mUser = cache.getMqttuser();
        String mPassword = cache.getMqttpasswd();

        if(mPassword==null) {
            FlyveLog.d("Flyve", "Password can't be null");
            return;
        }

        mTopic = cache.getTopic();

        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Broker", mBroker));
        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Port", mPort));
        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "User", mUser));
        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Topic", mTopic));

        String clientId = MqttClient.generateClientId();
            client = new MqttAndroidClient(this.getApplicationContext(), "ssl://" + mBroker + ":" + mPort,
                clientId);

        client.setCallback( this );
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setPassword(mPassword.toCharArray());
            options.setUserName(mUser);
            options.setCleanSession(true);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setConnectionTimeout(0);

            try {
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);

                options.setSocketFactory(sslContext.getSocketFactory());

                FlyveLog.d("Flyve", "ssl socket factory created from flyve ca");
            } catch (Exception ex) {
                FlyveLog.e("Flyve","error while building ssl mqtt cnx", ex);
            }


            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    // Everything ready waiting for message
                    FlyveLog.d("Success we are online!");
                    broadcastServiceStatus(true);

                    mqttHelper = new MQTTHelper(getApplicationContext(), client);

                    // principal channel
                    String channel = mTopic + "/#";
                    FlyveLog.d("MQTT Channel: " + channel);
                    mqttHelper.suscribe(channel);

                    // send inventory on connect
                    mqttHelper.createInventory();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    FlyveLog.e("onFailure:" + ex.getMessage());
                    broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on connect - client.connect", ex.getMessage()));
                    broadcastServiceStatus(false);
                }
            });
        }
        catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on connect", ex.getMessage()));
        }
    }

    private void reconnect() {
        final Timer timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            public void run() {
                if(!connected) {
                    connect();
                    FlyveLog.d("try to reconnect");
                    broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Reconnect", "Try to reconnect"));
                } else {
                    FlyveLog.d("Timer cancel");
                    timer.cancel();
                    timer.purge();
                }
            }
        };
        timer.schedule(timerTask, 0, 6000); // retry every 600000 10 minutes
    }

    /**
     * If connection fail trigger this function
     * @param cause Throwable error
     */
    @Override
    public void connectionLost(Throwable cause) {
        // send to backend that agent lost connection
        mqttHelper.sendOnlineStatus(false);
        broadcastServiceStatus(false);
        broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error", cause.getMessage()));
        FlyveLog.d(TAG, "Connection fail " + cause.getMessage());

        reconnect();
    }

    /**
     * If delivery of the message was complete
     * @param token get message token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        FlyveLog.d( "deliveryComplete: " + token.toString());
        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Response id", String.valueOf(token.getMessageId())));
    }

    /**
     * When a message from server arrive
     * @param topic String topic where the message from
     * @param message MqttMessage message content
     * @throws Exception error
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        FlyveLog.i("Topic " + topic);
        FlyveLog.i("Message " + new String(message.getPayload()));

        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Topic", topic));
        broadcastReceivedLog(Helpers.broadCastMessage("MQTT", "Message", new String(message.getPayload())));

        String messageBody = new String(message.getPayload());

        try {
            JSONObject jsonObj = new JSONObject(messageBody);

            if (jsonObj.has("query")) {
                // PING request
                if ("Ping".equalsIgnoreCase(jsonObj.getString("query"))) {
                    mqttHelper.sendKeepAlive();
                    return;
                }
                // Inventory Request
                if("Inventory".equalsIgnoreCase(jsonObj.getString("query"))) {
                    mqttHelper.createInventory();
                    return;
                }

                // Geolocation request
                if("Geolocate".equalsIgnoreCase(jsonObj.getString("query"))) {
                    mqttHelper.sendGPS();
                    return;
                }
            }

            // Wipe Request
            if(jsonObj.has("wipe")) {
                if("NOW".equalsIgnoreCase(jsonObj.getString("wipe"))) {
                    mqttHelper.wipe();
                    return;
                }
            }

            // Unenroll Request
            if (jsonObj.has("unenroll")) {
                mqttHelper.unenroll();
                return;
            }

            // Subscribe a new channel in MQTT
            if(jsonObj.has("subscribe")) {
                JSONArray jsonTopics = jsonObj.getJSONArray("subscribe");
                for(int i=0; i<jsonTopics.length();i++) {
                    JSONObject jsonTopic = jsonTopics.getJSONObject(0);

                    // Add new channel
                    mqttHelper.suscribe(jsonTopic.getString("topic")+"/#");
                }
                return;
            }

            // Lock
            if(jsonObj.has("lock")) {
                mqttHelper.lockDevice(jsonObj);
                return;
            }

            // FLEET Camera
            if(jsonObj.has("camera")) {
                mqttHelper.disableCamera(jsonObj);
                return;
            }

            // FLEET connectivity
            if(jsonObj.has("connectivity")) {
                mqttHelper.disableConnectivity(jsonObj);
                return;
            }

            // FLEET encryption
            if(jsonObj.has("encryption")) {
                mqttHelper.storageEncryption(jsonObj);
                return;
            }

            // FLEET policies
            if(jsonObj.has("policies")) {
                mqttHelper.policiesDevice(jsonObj);
                return;
            }

            // Files
            if(jsonObj.has("file")) {
                mqttHelper.filesOnDevices(jsonObj);
                return;
            }

            // Applications
            if(jsonObj.has("application")) {
                mqttHelper.applicationOnDevices(jsonObj);
                return;
            }


        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
            broadcastReceivedLog(Helpers.broadCastMessage("ERROR", "Error on messageArrived", ex.getMessage()));
        }
    }

    /**
     * Send broadcast for log messages from MQTT
     * @param message String to send
     */
    public void broadcastReceivedLog(String message) {
        FlyveLog.i(message);
        Helpers.sendBroadcast(message, Helpers.BROADCAST_LOG, getApplicationContext());
    }

    /**
     * Send broadcast for status of the service
     * @param status boolean status
     */
    private void broadcastServiceStatus(boolean status) {
        //send broadcast
        this.connected = status;
        Helpers.sendBroadcast(status, Helpers.BROADCAST_STATUS, getApplicationContext());
    }
}