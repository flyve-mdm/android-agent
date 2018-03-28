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

package org.flyve.mdm.agent.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.disclosure.Disclosure;
import org.flyve.mdm.agent.core.disclosure.DisclosurePresenter;
import org.flyve.mdm.agent.utils.Helpers;

public class DisclosureActivity extends AppCompatActivity implements Disclosure.View {

    private Disclosure.Presenter presenter;

    /**
     * This method is called when the activity is starting
     * @param savedInstanceState if the activity is re-initialized, it contains the data it most recently supplied, otherwise null
     *  https://developer.android.com/reference/android/app/Activity.html#onCreate(android.os.Bundle) Documentation of the method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclosure);

        presenter = new DisclosurePresenter(this);

        Button btnAccept = findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.requestDeviceAdmin(DisclosureActivity.this);
            }
        });

        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Called when a launched activity exits, giving the requestCode it started with, the resultCode it returned, and any additional data from it
     * @param requestCode integer request code, it allows to identify who this result came from
     * @param resultCode the integer result code returned by the child activity
     * @param data the intent data , which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        presenter.checkDeviceAdminResult(DisclosureActivity.this, requestCode, resultCode);
    }

    @Override
    public void showError(String message) {
        Helpers.snack(DisclosureActivity.this, message);
    }
}
