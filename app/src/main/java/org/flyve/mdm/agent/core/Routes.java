/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
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
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.core;

import android.content.Context;

import org.flyve.mdm.agent.data.MqttData;

/**
 * Content all the routes of the app
 */
public class Routes {

    private String url;
    private MqttData cache;

    /**
     * Constructor
     * @param context
     */
    public Routes(Context context) {
        cache = new MqttData(context);
        url = cache.getUrl();
    }

    /**
     * initSession url
     * @param userToken String user token
     * @return String with the url
     */
    public String initSession(String userToken) {
        return url + "/initSession?user_token=" + userToken;
    }

    /**
     * getFullSession url
     * @return String with the url
     */
    public String getFullSession() {
        return url + "/getFullSession";
    }

    /**
     * changeActiveProfile url
     * @param profileId String profile id to activate
     * @return String with the url
     */
    public String changeActiveProfile(String profileId) {
        return url + "/changeActiveProfile?profiles_id=" + profileId;
    }

    /**
     * PluginFlyvemdmAgent url
     * @return String with the url
     */
    public String pluginFlyvemdmAgent() {
        return url + "/PluginFlyvemdmAgent";
    }

    /**
     * PluginFlyvemdmAgent url
     * @param agentId String Agent Id
     * @return String with the url
     */
    public String pluginFlyvemdmAgent(String agentId) {
        return url + "/PluginFlyvemdmAgent/" + agentId;
    }

    /**
     * Download files
     * @param fileId String file id
     * @return String url
     */
    public String pluginFlyvemdmFile(String fileId, String sessionToken) {
        return url + "/PluginFlyvemdmFile/" + fileId + "?session_token=" + sessionToken;
    }

    /**
     * Download apk
     * @param fileId String file id
     * @return String url
     */
    public String pluginFlyvemdmPackage(String fileId, String sessionToken) {
        return url + "/PluginFlyvemdmPackage/" + fileId + "?session_token=" + sessionToken;
    }
}
