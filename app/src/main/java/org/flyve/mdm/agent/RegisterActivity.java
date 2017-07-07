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

package org.flyve.mdm.agent;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flyvemdm.inventory.categories.Hardware;

import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.security.AndroidCryptoProvider;
import org.flyve.mdm.agent.utils.ConnectionHTTP;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.Routes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Register the agent to the platform
 */
public class RegisterActivity extends Activity {

    private ProgressBar pb;
    private Routes routes;
    private DataStorage cache;
    private TextView tvMsg;
    private LinearLayout lyUserData;
    private boolean certifiedX509Available = false;
    private EditText txtName;
    private EditText txtLastName;
    private EditText txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        pb = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();
        Uri data = intent.getData();

        String deepLinkData = Helpers.base64decode(data.getQueryParameter("data"));

        cache = new DataStorage( RegisterActivity.this );

        String broker = cache.getBroker();
        if(broker != null) {
            openMain();
        }

        try {
            JSONObject jsonLink = new JSONObject(deepLinkData);

            String url = jsonLink.getString("url");
            String userToken = jsonLink.getString("user_token");
            String invitationToken = jsonLink.getString("invitation_token");

            cache.setUrl(url);
            cache.setUserToken( userToken );
            cache.setInvitationToken( invitationToken );

        } catch (Exception ex) {
            FlyveLog.e( ex.getMessage() );
        }

        routes = new Routes( RegisterActivity.this );

        tvMsg = (TextView) findViewById(R.id.tvMsg);
        lyUserData = (LinearLayout) findViewById(R.id.user_data);

        txtName = (EditText) findViewById(R.id.txtName);
        txtLastName = (EditText) findViewById(R.id.txtLastName);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtEmail.setImeActionLabel("Done", KeyEvent.KEYCODE_ENTER);

        Button btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pluginFlyvemdmAgent();
            }
        });

        // start creating a certificated
        createX509cert();

        // start enrollment process
        initSession();
    }

    /**
     * STEP 1 get session token
     */
    private void initSession() {
        try {
            pb.setVisibility(View.VISIBLE);
            tvMsg.setText("Init Session");
            ConnectionHTTP.getWebData(
                    routes.initSession( cache.getUserToken() ),
                    "GET" ,
                    new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {

                    try {
                        JSONObject jsonSession = new JSONObject(data);
                        cache.setSessionToken( jsonSession.getString("session_token") );
                        tvMsg.setText("get Full Session");
                        getFullSession();

                    } catch (Exception ex) {
                        tvMsg.setText("ERROR JSON: initSession");
                        pb.setVisibility(View.GONE);
                        FlyveLog.e( ex.getMessage() );
                    }
                }
            });
        }
        catch (Exception ex) {
            pb.setVisibility(View.GONE);
            tvMsg.setText("ERROR: initSession");
            FlyveLog.e( ex.getMessage() );
        }
    }

    /**
     * STEP 2 get full session information
     */
    private void getFullSession() {
        try {
            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.getFullSession());

            ConnectionHTTP.getWebData(routes.getFullSession(), "GET", header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {

                    tvMsg.setText("changeActiveProfile");

                    try {
                        JSONObject jsonFullSession = new JSONObject(data);

                        JSONObject jsonSession = jsonFullSession.getJSONObject("session");

                        JSONObject jsonActiveProfile = jsonSession.getJSONObject("glpiactiveprofile");

                        String profileId = jsonActiveProfile.getString("id");
                        cache.setProfileId( profileId );

                        changeActiveProfile();

                    } catch (Exception ex) {
                        pb.setVisibility(View.GONE);
                        tvMsg.setText("ERROR JSON: getFullSession");
                        FlyveLog.e( ex.getMessage() );
                    }

                    changeActiveProfile();

                }
            });
        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            tvMsg.setText("ERROR: getFullSession");
            FlyveLog.e( ex.getMessage() );
        }
    }

    /**
     * STEP 3 Activated the profile
     */
    private void changeActiveProfile() {

        try {

            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.getFullSession());

            ConnectionHTTP.getWebData(routes.changeActiveProfile(cache.getProfileId()), "GET", header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {
                    pb.setVisibility(View.GONE);
                    tvMsg.setText("changeActiveProfile Ok!");
                    lyUserData.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            tvMsg.setText("ERROR: changeActiveProfile");
            FlyveLog.e( ex.getMessage() );
        }

    }

    /**
     * STEP 4 create X509 certificate
     */
    private void createX509cert() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    AndroidCryptoProvider createCertif = new AndroidCryptoProvider(getBaseContext());
                    createCertif.generateRequest();
                    createCertif.loadCsr();
                    certifiedX509Available = true;
                } catch (Exception ex) {
                    certifiedX509Available = false;
                    tvMsg.setText("ERROR: Creating Certificate X509");
                    FlyveLog.e(ex.getMessage());
                }
            }
        }).start();
    }

    /**
     * STEP 5 Send the payload to register the agent
     */
    private void pluginFlyvemdmAgent() {

        if(!certifiedX509Available) {
            tvMsg.setText("The certified is not available");
            return;
        }

        tvMsg.setText("Register Agent");
        try {

            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");

            JSONObject payload = new JSONObject();
            JSONObject input = new JSONObject();

            AndroidCryptoProvider csr = new AndroidCryptoProvider(RegisterActivity.this.getBaseContext());
            String requestCSR = "";
            if( csr.getlCsr() != null ) {
                requestCSR = URLEncoder.encode(csr.getlCsr(), "UTF-8");
            }

            try {
                payload.put("_email", txtEmail.getText());
                payload.put("_invitation_token", cache.getInvitationToken());
                payload.put("_serial", Helpers.getDeviceSerial());
                //payload.put("csr", requestCSR);
                payload.put("csr", "");
                payload.put("uuid", new Hardware(RegisterActivity.this).getUUID());
                payload.put("firstname", txtName.getText());
                payload.put("lastname", txtLastName.getText());
                payload.put("version", BuildConfig.VERSION_NAME);
                input.put("input", payload);
            } catch (JSONException ex) {
                pb.setVisibility(View.GONE);
                tvMsg.setText( "ERROR pluginFlyvemdmAgent JSON" );
                FlyveLog.e( ex.getMessage() );
            }

            ConnectionHTTP.getWebData(routes.pluginFlyvemdmAgent(), input, header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {
                    tvMsg.setText("Register Agent");

                    if(data.contains("ERROR")){
                        pb.setVisibility(View.GONE);

                        try {
                            JSONArray jsonArr = new JSONArray(data);
                            String msgError = jsonArr.get(1).toString();

                            tvMsg.setText("ERROR pluginFlyvemdmAgent HTTP " + msgError);
                            FlyveLog.e(data);
                        } catch (Exception ex) {
                            FlyveLog.e(ex.getCause().getMessage());
                            tvMsg.setText("Unknow error");
                        }
                    } else {
                        try {
                            JSONObject jsonAgent = new JSONObject(data);
                            cache.setAgentId(jsonAgent.getString("id"));

                            getDataPluginFlyvemdmAgent();
                        } catch (Exception ex) {
                            pb.setVisibility(View.GONE);
                            tvMsg.setText("ERROR pluginFlyvemdmAgent HTTP " + data);
                            FlyveLog.e(ex.getMessage());
                        }
                    }
                }
            });

        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            tvMsg.setText( "ERROR pluginFlyvemdmAgent" );
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * STEP 6 get all the information of the agent and store this info on cache
     */
    private void getDataPluginFlyvemdmAgent() {

        try {
            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.pluginFlyvemdmAgent());

            ConnectionHTTP.getWebData(routes.pluginFlyvemdmAgent(cache.getAgentId()), "GET", header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {

                pb.setVisibility(View.GONE);

                try {
                    JSONObject jsonObject = new JSONObject(data);

                    String mbroker = jsonObject.getString("broker");
                    String mport = jsonObject.getString("port");
                    String mssl = jsonObject.getString("tls");
                    String mtopic = jsonObject.getString("topic");
                    String mpassword = jsonObject.getString("mqttpasswd");
                    String mcert = jsonObject.getString("certificate");
                    String mNameEmail = jsonObject.getString("name");
                    int mComputersId = jsonObject.getInt("computers_id");
                    int mId = jsonObject.getInt("id");
                    int mEntitiesId = jsonObject.getInt("entities_id");
                    int mFleetId = jsonObject.getInt("plugin_flyvemdm_fleets_id");

                    cache.setBroker( mbroker );
                    cache.setPort( mport );
                    cache.setTls( mssl );
                    cache.setTopic( mtopic );
                    cache.setMqttuser( Helpers.getDeviceSerial() );
                    cache.setMqttpasswd( mpassword );
                    cache.setCertificate( mcert );
                    cache.setName( mNameEmail );
                    cache.setComputersId( String.valueOf(mComputersId) );
                    cache.setEntitiesId( String.valueOf(mEntitiesId) );
                    cache.setPluginFlyvemdmFleetsId( String.valueOf(mFleetId) );

                    openMain();

                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                }
                }
            });

        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            tvMsg.setText( "ERROR getDataPluginFlyvemdmAgent" );
            FlyveLog.e(ex.getMessage());
        }
    }

    /**
     * Open the main activity
     */
    private void openMain() {
        Intent miIntent = new Intent(RegisterActivity.this, MainActivity.class);
        RegisterActivity.this.startActivity(miIntent);
        RegisterActivity.this.finish();
    }
}
