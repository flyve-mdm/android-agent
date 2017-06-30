/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * This file is part of flyve-mdm-android-agent
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
 * @copyright Copyright © ${YEAR} Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package com.teclib.flyvemdm;


import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.teclib.data.DataStorage;

/**
 * All the application configuration
 */
public class FlyveMDMApp extends Application {

    private DataStorage cache;
    private static FlyveMDMApp instance;
    private static Boolean isDebuggable;

    @Override
    public void onCreate() {
        super.onCreate();
        cache = new DataStorage(this);

        instance = this;
        Logger.addLogAdapter(new AndroidLogAdapter());
        isDebuggable =  ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
        Log.d("test", "is debug: " + isDebuggable );
    }

    /**
     * Get the cache class
     * @return DataStorage
     */
    public DataStorage getCache() {
        return cache;
    }

    /**
     * Get application instance
     * @return FlyveMDMApp object
     */
    public static FlyveMDMApp getInstance(){
        return instance;
    }

    /**
     * Get if the app is debuggable or not
     * @return Boolean
     */
    public static Boolean getIsDebuggable() {
        return isDebuggable;
    }
}
