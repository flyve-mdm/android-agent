package org.flyve.mdm.agent.security;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android
 *
 * flyve-mdm-android is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @author    rafaelhernandez
 * @date      4/7/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class FlyveDeviceAdminUtils {

    private DevicePolicyManager mDPM;
    ComponentName mDeviceAdmin;

    public FlyveDeviceAdminUtils(Context context) {
        mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(context, FlyveAdminReceiver.class);
    }

    /**
     * Erase all data of the device
     */
    public void wipe() {
        mDPM.wipeData(0);
    }

    /**
     * Lock the device now
     */
    public void lockDevice() {
        mDPM.lockNow();
    }

    /**
     * Disable the possibility to use the camera
     * @param disable boolean true | false
     */
    public void disableCamera(boolean disable) {
        mDPM.setCameraDisabled(mDeviceAdmin, disable);
    }

}
