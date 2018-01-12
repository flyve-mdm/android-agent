package org.flyve.mdm.agent.core.user;

import android.app.Activity;
import android.content.Context;

/*
 *   Copyright © 2018 Teclib. All rights reserved.
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
 * @author    Rafael Hernandez
 * @date      4/1/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class UserPresenter implements User.Presenter {

    private User.View view;
    private User.Model model;

    public UserPresenter(User.View view){
        this.view = view;
        model = new UserModel(this);
    }

    @Override
    public void saveSuccess() {
        if(view!=null) {
            view.saveSuccess();
        }
    }

    @Override
    public void showError(String message) {
        if(view!=null) {
            view.showError(message);
        }
    }

    @Override
    public void loadSuccess(UserSchema userSchema) {
        if(view!=null) {
            view.loadSuccess(userSchema);
        }
    }

    @Override
    public void load(Context context) {
        model.load(context);
    }

    @Override
    public void selectPhoto(Activity activity, int requestCamera, int requestFile) {
        model.selectPhoto(activity, requestCamera, requestFile);
    }

    @Override
    public void save(Activity activity, UserSchema userSchema) {
        model.save(activity, userSchema);
    }

}
